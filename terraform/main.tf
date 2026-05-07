# ── Kubernetes provider (minikube context) ─────────────────────────────────────
provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = var.kube_context
}

# ── Helm provider (same minikube context) ─────────────────────────────────────
provider "helm" {
  kubernetes {
    config_path    = "~/.kube/config"
    config_context = var.kube_context
  }
}

