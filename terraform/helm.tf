locals {
  kafka_svc             = "${var.release_name}-kafka"
  postgresql_svc        = "${var.release_name}-postgresql"
  schema_registry_svc   = "${var.release_name}-schema-registry"
  schema_registry_port  = 8081
}

# ── 1. Kafka ───────────────────────────────────────────────────────────────────
resource "helm_release" "kafka" {
  name             = "${var.release_name}-kafka"
  repository       = "https://kubelauncher.github.io/charts"
  chart            = "kafka"
  namespace        = var.namespace
  create_namespace = false
  wait             = true
  timeout          = 300
  # version          = var.kafka_chart_version != "" ? var.kafka_chart_version : null

  set {
    name  = "kafka.replicaCount"
    value = "1"
  }

  set {
    name  = "zookeeper.replicaCount"
    value = "1"
  }

  set {
    name  = "persistence.size"
    value = "1Gi"
  }

  depends_on = [kubernetes_namespace.diploma]
}

# ── 2. PostgreSQL ──────────────────────────────────────────────────────────────
resource "helm_release" "postgresql" {
  name             = "${var.release_name}-postgresql"
  repository       = "oci://registry-1.docker.io/bitnamicharts"
  chart            = "postgresql"
  namespace        = var.namespace
  create_namespace = false
  wait             = true
  timeout          = 300
  # version          = var.postgresql_chart_version != "" ? var.postgresql_chart_version : null

  set {
    name  = "auth.postgresPassword"
    value = var.db_password
  }

  set {
    name  = "auth.username"
    value = var.db_user
  }

  set {
    name  = "auth.password"
    value = var.db_password
  }

  set {
    name  = "auth.database"
    value = var.db_name
  }

  set {
    name  = "primary.persistence.size"
    value = "1Gi"
  }

  depends_on = [kubernetes_namespace.diploma]
}
