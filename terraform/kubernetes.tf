# ── Namespace ──────────────────────────────────────────────────────────────────
resource "kubernetes_namespace" "diploma" {
  metadata {
    name = var.namespace
    labels = {
      app = "diploma-camel"
    }
  }

  depends_on = []
}

# ── ConfigMap ──────────────────────────────────────────────────────────────────
resource "kubernetes_config_map" "app_config" {
  metadata {
    name      = "app-config"
    namespace = var.namespace
  }

  data = {
    DB_HOST = local.postgresql_svc
    DB_PORT = "5432"
    DB_NAME = var.db_name
  }

  depends_on = [kubernetes_namespace.diploma]
}

# ── Secret (PostgreSQL credentials) ───────────────────────────────────────────
resource "kubernetes_secret" "postgres" {
  metadata {
    name      = "postgres-secret"
    namespace = var.namespace
  }

  type = "Opaque"

  data = {
    POSTGRES_USER     = var.db_user
    POSTGRES_PASSWORD = var.db_password
    POSTGRES_DB       = var.db_name
  }

  depends_on = [kubernetes_namespace.diploma]
}
