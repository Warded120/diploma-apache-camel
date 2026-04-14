#!/usr/bin/env pwsh
# Kubernetes Deployment Script for Diploma Apache Camel Application

param(
    [switch]$Clean,
    [switch]$SkipWait
)

$ErrorActionPreference = "Stop"
$namespace = "diploma-app"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deploying Diploma Apache Camel to K8s" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($Clean) {
    Write-Host "`nCleaning up existing deployment..." -ForegroundColor Yellow
    kubectl delete namespace $namespace --ignore-not-found=true
    Write-Host "Waiting for namespace deletion..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

# Step 1: Create Namespace
Write-Host "`n[1/6] Creating namespace..." -ForegroundColor Green
kubectl apply -f namespace.yaml

# Step 2: Create Secrets and ConfigMaps
Write-Host "`n[2/6] Creating secrets and configmaps..." -ForegroundColor Green
kubectl apply -f postgres-secret.yaml
kubectl apply -f app-configmap.yaml

# Step 3: Create Persistent Volume Claim
Write-Host "`n[3/6] Creating persistent volume claim..." -ForegroundColor Green
kubectl apply -f postgres-pvc.yaml
kubectl apply -f kafka-pvc.yaml

# Step 4: Deploy Infrastructure (Kafka & PostgreSQL)
Write-Host "`n[4/6] Deploying infrastructure services..." -ForegroundColor Green
kubectl apply -f kafka-deployment.yaml
kubectl apply -f kafka-service.yaml
kubectl apply -f postgres-deployment.yaml
kubectl apply -f postgres-service.yaml

if (-not $SkipWait) {
    Write-Host "`nWaiting for Kafka to be ready..." -ForegroundColor Yellow
    kubectl wait --for=condition=ready pod -l app=kafka -n $namespace --timeout=300s

    Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Yellow
    kubectl wait --for=condition=ready pod -l app=postgres -n $namespace --timeout=300s
}

# Step 5: Deploy Application Services
Write-Host "`n[5/6] Deploying application services..." -ForegroundColor Green
kubectl apply -f inbound-processor-deployment.yaml
kubectl apply -f inbound-processor-service.yaml
kubectl apply -f outbound-processor-deployment.yaml
kubectl apply -f outbound-processor-service.yaml

# Step 6: Display Status
Write-Host "`n[6/6] Deployment Status" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nPods:" -ForegroundColor Yellow
kubectl get pods -n $namespace

Write-Host "`nServices:" -ForegroundColor Yellow
kubectl get svc -n $namespace

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Deployment Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nUseful Commands:" -ForegroundColor Cyan
Write-Host "  View all resources:    kubectl get all -n $namespace" -ForegroundColor White
Write-Host "  View logs (inbound):   kubectl logs -f deployment/inbound-processor -n $namespace" -ForegroundColor White
Write-Host "  View logs (outbound):  kubectl logs -f deployment/outbound-processor -n $namespace" -ForegroundColor White
Write-Host "  Port-forward inbound:  kubectl port-forward svc/inbound-processor 8080:8080 -n $namespace" -ForegroundColor White
Write-Host "  Delete all:            kubectl delete namespace $namespace" -ForegroundColor White

