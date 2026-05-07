resource "kubernetes_deployment" "outbound_processor" {
  metadata {
    name      = "outbound-processor"
    namespace = var.namespace
    labels = {
      app = "outbound-processor"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "outbound-processor"
      }
    }

    template {
      metadata {
        labels = {
          app = "outbound-processor"
        }
      }

      spec {
        # Wait for PostgreSQL then Kafka before starting the main container
        init_container {
          name    = "wait-for-postgres"
          image   = "busybox:1.36"
          command = ["sh", "-c", "until nc -z ${local.postgresql_svc} 5432; do echo waiting for postgres; sleep 2; done"]
        }

        init_container {
          name    = "wait-for-kafka"
          image   = "busybox:1.36"
          command = ["sh", "-c", "until nc -z ${local.kafka_svc} 9092; do echo waiting for kafka; sleep 2; done"]
        }

        container {
          name              = "outbound-processor"
          image             = var.outbound_image
          image_pull_policy = "IfNotPresent"

          port {
            container_port = 8080
          }

          # Inject individual DB keys from secret
          env {
            name = "DB_USER"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.postgres.metadata[0].name
                key  = "POSTGRES_USER"
              }
            }
          }
          env {
            name = "DB_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.postgres.metadata[0].name
                key  = "POSTGRES_PASSWORD"
              }
            }
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
              path = "/q/health/ready"//TODO: use /observe/health
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

resource "kubernetes_service" "outbound_processor" {
  metadata {
    name      = "outbound-processor"
    namespace = var.namespace
    labels = {
      app = "outbound-processor"
    }
  }

  spec {
    type = "NodePort"

    selector = {
      app = "outbound-processor"
    }

    port {
      port        = 8080
      target_port = 8080
      node_port   = 30082
      protocol    = "TCP"
    }
  }

  depends_on = [kubernetes_namespace.diploma]
}
