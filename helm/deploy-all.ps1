# Helm deployment script for diploma-apache-camel
# This script deploys PostgreSQL and Kafka using Bitnami charts,
# then deploys the application components

param(
    [string]$Namespace = "diploma-app",
    [string]$ReleaseName = "diploma"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deploying diploma-apache-camel with Helm" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Create namespace if it doesn't exist
Write-Host "`n[1/5] Creating namespace $Namespace..." -ForegroundColor Yellow
kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -

# Add Bitnami repo
Write-Host "`n[2/5] Adding Bitnami Helm repository..." -ForegroundColor Yellow
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add kubelauncher https://kubelauncher.github.io/charts
helm repo update

# Deploy Kafka using Bitnami chart
Write-Host "`n[3/5] Deploying Kafka..." -ForegroundColor Yellow
helm upgrade --install $ReleaseName-kafka kubelauncher/kafka `
    --namespace $Namespace `
    --set kafka.replicaCount=1 `
    --set zookeeper.replicaCount=1 `
    --set persistence.size=1Gi `
    --wait --timeout 5m

# Deploy PostgreSQL using Bitnami chart
Write-Host "`n[4/5] Deploying PostgreSQL..." -ForegroundColor Yellow
helm upgrade --install $ReleaseName-postgresql bitnami/postgresql `
    --namespace $Namespace `
    --set auth.postgresPassword=password `
    --set auth.username=diploma `
    --set auth.password=password `
    --set auth.database=diploma `
    --set primary.persistence.size=1Gi `
    --wait --timeout 5m

# Deploy application components
Write-Host "`n[5/5] Deploying application components..." -ForegroundColor Yellow

# Deploy inbound-processor
Write-Host "  - Deploying inbound-processor..." -ForegroundColor Green
helm upgrade --install $ReleaseName-inbound ./inbound-processor `
    --namespace $Namespace `
    --set db.host=$ReleaseName-postgresql `
    --set db.secretName=$ReleaseName-postgresql `
    --set kafka.brokers=$ReleaseName-kafka:9092

# Deploy outbound-processor
Write-Host "  - Deploying outbound-processor..." -ForegroundColor Green
helm upgrade --install $ReleaseName-outbound ./outbound-processor `
    --namespace $Namespace `
    --set db.host=$ReleaseName-postgresql `
    --set db.secretName=$ReleaseName-postgresql `
    --set kafka.brokers=$ReleaseName-kafka:9092

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Deployment complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nTo check status:"
Write-Host "  kubectl get pods -n $Namespace"
Write-Host "  helm list -n $Namespace"
Write-Host "`nTo access inbound-processor:"
Write-Host "  minikube service $ReleaseName-inbound-inbound-processor -n $Namespace"
