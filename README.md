docker build --target inbound-processor -t inbound-processor:diploma .
docker build --target outbound-processor -t outbound-processor:diploma .

minikube image load inbound-processor:diploma
minikube image load outbound-processor:diploma

terraform init
terraform apply -auto-approve