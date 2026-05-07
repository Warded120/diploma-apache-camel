variable "kube_context" {
  description = "kubectl context to use (default: minikube)"
  type        = string
  default     = "minikube"
}

variable "namespace" {
  description = "Kubernetes namespace for all resources"
  type        = string
  default     = "diploma-app"
}

variable "release_name" {
  description = "Helm release name prefix (e.g. 'diploma')"
  type        = string
  default     = "diploma"
}

# ── PostgreSQL credentials ─────────────────────────────────────────────────────
variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "diploma"
}

variable "db_user" {
  description = "PostgreSQL user"
  type        = string
  default     = "diploma"
}

variable "db_password" {
  description = "PostgreSQL password"
  type        = string
  default     = "password"
  sensitive   = true
}

# ── Chart versions ─────────────────────────────────────────────────────────────
variable "kafka_chart_version" {
  description = "kubelauncher/kafka chart version (empty = latest)"
  type        = string
  default     = ""
}

variable "postgresql_chart_version" {
  description = "bitnami/postgresql chart version (empty = latest)"
  type        = string
  default     = ""
}

variable "schema_registry_chart_version" {
  description = "cp-helm-charts/cp-schema-registry chart version (empty = latest)"
  type        = string
  default     = ""
}

# ── Application images ─────────────────────────────────────────────────────────
variable "inbound_image" {
  description = "Docker image for inbound-processor"
  type        = string
  default     = "inbound-processor:latest"
}

variable "outbound_image" {
  description = "Docker image for outbound-processor"
  type        = string
  default     = "outbound-processor:latest"
}

