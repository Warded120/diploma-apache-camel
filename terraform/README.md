# Terraform Infrastructure

All infrastructure is managed from this directory. Terraform state is stored locally at `terraform/terraform.tfstate`.

## Prerequisites

| Tool | Version |
|------|---------|
| Terraform | ≥ 1.5 |
| minikube | any recent |
| kubectl | matching cluster |
| helm | ≥ 3.x |
| Docker | for building images |

## Quick Start

### 1. Build and load microservice images into minikube

```powershell
# From the repo root
docker build --target inbound-processor  -t inbound-processor:latest  .
docker build --target outbound-processor -t outbound-processor:latest .

minikube image load inbound-processor:latest
minikube image load outbound-processor:latest
```

> `imagePullPolicy: IfNotPresent` — Kubernetes will use the image already loaded into minikube's registry without trying to pull from a remote registry.

### 2. Apply infrastructure

```powershell
cd terraform
terraform init
terraform plan
terraform apply
```

### 3. Verify

```powershell
kubectl get pods   -n diploma-app
kubectl get svc    -n diploma-app
helm list          -n diploma-app
```

### 4. Access inbound-processor

```powershell
minikube service inbound-processor -n diploma-app --url
# or directly via NodePort:
# http://<minikube-ip>:30081/orders
```

## Variables

Override any variable at apply time:

```powershell
terraform apply `
  -var="release_name=my-diploma" `
  -var="db_password=supersecret" `
  -var="kube_context=minikube"
```

| Variable | Default | Description |
|----------|---------|-------------|
| `kube_context` | `minikube` | kubectl context |
| `namespace` | `diploma-app` | Kubernetes namespace |
| `release_name` | `diploma` | Helm release name prefix |
| `db_name` | `diploma` | PostgreSQL database |
| `db_user` | `diploma` | PostgreSQL user |
| `db_password` | `password` | PostgreSQL password (sensitive) |
| `inbound_image` | `inbound-processor:latest` | Docker image tag |
| `outbound_image` | `outbound-processor:latest` | Docker image tag |

## Deployed Services (inside cluster)

| Service | Address | Port |
|---------|---------|------|
| Kafka | `diploma-kafka:9092` | 9092 |
| PostgreSQL | `diploma-postgresql:5432` | 5432 |
| Schema Registry | `diploma-schema-registry-cp-schema-registry:8081` | 8081 |
| inbound-processor | NodePort | 30081 |
| outbound-processor | NodePort | 30082 |

## Teardown

```powershell
terraform destroy
```

