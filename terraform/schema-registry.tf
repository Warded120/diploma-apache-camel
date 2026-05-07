resource "kubernetes_deployment" "schema_registry" {
  metadata {
    name      = "${var.release_name}-schema-registry"
    namespace = var.namespace
    labels    = { app = "schema-registry" }
  }

  spec {
    replicas = 1

    selector {
      match_labels = { app = "schema-registry" }
    }

    template {
      metadata {
        labels = { app = "schema-registry" }
      }

      spec {
        # Wait for Kafka to be reachable before starting
        init_container {
          name    = "wait-for-kafka"
          image   = "busybox:1.36"
          command = ["sh", "-c", "until nc -z ${local.kafka_svc} 9092; do echo waiting for kafka; sleep 2; done"]
        }

        container {
          name              = "schema-registry"
          image             = "confluentinc/cp-schema-registry:7.6.0"
          image_pull_policy = "IfNotPresent"

          port {
            container_port = 8081
          }

          env {
            name  = "SCHEMA_REGISTRY_HOST_NAME"
            value = "${var.release_name}-schema-registry"
          }
          env {
            name  = "SCHEMA_REGISTRY_LISTENERS"
            value = "http://0.0.0.0:8081"
          }
          env {
            name  = "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS"
            value = "PLAINTEXT://${local.kafka_svc}:9092"
          }

          readiness_probe {
            http_get {
              path = "/subjects"
              port = 8081
            }
            initial_delay_seconds = 20
            period_seconds        = 10
            failure_threshold     = 5
          }

          resources {
            requests = {
              memory = "256Mi"
              cpu    = "100m"
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

  depends_on = [helm_release.kafka, kubernetes_namespace.diploma]
}

resource "kubernetes_service" "schema_registry" {
  metadata {
    name      = "${var.release_name}-schema-registry"
    namespace = var.namespace
    labels    = { app = "schema-registry" }
  }

  spec {
    selector = { app = "schema-registry" }

    port {
      port        = local.schema_registry_port
      target_port = 8081
      protocol    = "TCP"
    }
  }

  depends_on = [kubernetes_namespace.diploma]
}
