#!/usr/bin/env pwsh
# Kubernetes Undeployment Script for Diploma Apache Camel Application

param(
    [switch]$SkipWait
)

$ErrorActionPreference = "Stop"
$namespace = "diploma-app"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Undeploying Diploma Apache Camel from K8s" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "Deleting application services..." -ForegroundColor Yellow
kubectl delete -f inbound-processor-service.yaml --ignore-not-found=true
kubectl delete -f inbound-processor-deployment.yaml --ignore-not-found=true
kubectl delete -f outbound-processor-service.yaml --ignore-not-found=true
kubectl delete -f outbound-processor-deployment.yaml --ignore-not-found=true

Write-Host "Deleting infrastructure services..." -ForegroundColor Yellow
kubectl delete -f kafka-service.yaml --ignore-not-found=true
kubectl delete -f kafka-deployment.yaml --ignore-not-found=true
kubectl delete -f postgres-service.yaml --ignore-not-found=true
kubectl delete -f postgres-deployment.yaml --ignore-not-found=true

Write-Host "Deleting persistent volume claims..." -ForegroundColor Yellow
kubectl delete -f postgres-pvc.yaml --ignore-not-found=true
kubectl delete -f kafka-pvc.yaml --ignore-not-found=true

Write-Host "Deleting secrets and configmaps..." -ForegroundColor Yellow
kubectl delete -f postgres-secret.yaml --ignore-not-found=true
kubectl delete -f app-configmap.yaml --ignore-not-found=true

Write-Host "Deleting namespace..." -ForegroundColor Yellow
kubectl delete -f namespace.yaml --ignore-not-found=true

if (-not $SkipWait) {
    Write-Host "Waiting for namespace deletion..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

Write-Host "Undeployment Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

