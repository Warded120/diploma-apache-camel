output "namespace" {
  description = "Kubernetes namespace where all resources are deployed"
  value       = var.namespace
}

output "kafka_brokers" {
  description = "Kafka bootstrap servers address (inside cluster)"
  value       = "${local.kafka_svc}:9092"
}

output "postgresql_host" {
  description = "PostgreSQL service hostname (inside cluster)"
  value       = local.postgresql_svc
}

output "schema_registry_url" {
  description = "Schema Registry URL (inside cluster)"
  value       = "http://${local.schema_registry_svc}:${local.schema_registry_port}"
}

output "inbound_processor_node_port" {
  description = "NodePort for inbound-processor REST API (access via minikube IP)"
  value       = 30081
}

output "inbound_processor_health_node_port" {
  description = "NodePort for inbound-processor REST API (access via minikube IP)"
  value       = 30082
}

output "outbound_processor_node_port" {
  description = "NodePort for outbound-processor (access via minikube IP)"
  value       = 30083
}

output "minikube_access_hint" {
  description = "How to get the inbound-processor URL on minikube"
  value       = "Run: minikube service inbound-processor -n ${var.namespace} --url"
}

