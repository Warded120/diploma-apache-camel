resource "kubernetes_deployment" "inbound_processor" {
  metadata {
    name      = "inbound-processor"
    namespace = var.namespace
    labels = {
      app = "inbound-processor"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "inbound-processor"
      }
    }

    template {
      metadata {
        labels = {
          app = "inbound-processor"
        }
      }

      spec {
        # Wait for Kafka before starting the main container
        init_container {
          name    = "wait-for-kafka"
          image   = "busybox:1.36"
          command = ["sh", "-c", "until nc -z ${local.kafka_svc} 9092; do echo waiting for kafka; sleep 2; done"]
        }

        container {
          name              = "inbound-processor"
          image             = var.inbound_image
          image_pull_policy = "IfNotPresent"

          port {
            container_port = 8080
          }

          # Sourced from the Kafka Helm release
          env {
            name  = "KAFKA_BROKERS"
            value = "${helm_release.kafka.name}:9092"
          }
          # Sourced from the Schema Registry K8s service
          env {
            name  = "SCHEMA_REGISTRY_URL"
            value = "http://${local.schema_registry_svc}:${local.schema_registry_port}"
          }

          # Inject DB config from ConfigMap
          env_from {
            config_map_ref {
              name = kubernetes_config_map.app_config.metadata[0].name
            }
          }

          readiness_probe {
            http_get {
              path = "/q/health/ready"//TODO: use /observe
              port = 8080
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 5
            failure_threshold     = 3
          }

          liveness_probe {
            http_get {
              path = "/q/health/live"//TODO: use /observe/health
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 30
            timeout_seconds       = 10
            failure_threshold     = 3
          }

          resources {
            requests = {
              memory = "256Mi"
              cpu    = "250m"
            }
            limits = {
              memory = "512Mi"
              cpu    = "500m"
            }
          }
        }
      }
    }
  }

  depends_on = [
    helm_release.kafka,
    kubernetes_config_map.app_config,
    kubernetes_secret.postgres,
  ]
}

resource "kubernetes_service" "inbound_processor" {
  metadata {
    name      = "inbound-processor"
    namespace = var.namespace
    labels = {
      app = "inbound-processor"
    }
  }

  spec {
    type = "NodePort"

    selector = {
      app = "inbound-processor"
    }

    port {
      port        = 8080
      target_port = 8080
      node_port   = 30081
      protocol    = "TCP"
    }
  }

  depends_on = [kubernetes_namespace.diploma]
}
