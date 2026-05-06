# Helm uninstall script for diploma-apache-camel

param(
    [string]$Namespace = "diploma-app",
    [string]$ReleaseName = "diploma"
)

Write-Host "Uninstalling all Helm releases..." -ForegroundColor Yellow

helm uninstall $ReleaseName-inbound -n $Namespace 2>$null
helm uninstall $ReleaseName-outbound -n $Namespace 2>$null
helm uninstall $ReleaseName-kafka -n $Namespace 2>$null
helm uninstall $ReleaseName-postgresql -n $Namespace 2>$null

Write-Host "All releases uninstalled." -ForegroundColor Green
