# diploma-apache-camel – Build & Deploy

## Build Docker Images

```bash
docker build --target inbound-processor -t inbound-processor:diploma .
```

```bash
docker build --target outbound-processor -t outbound-processor:diploma .
```

```bash
minikube image load inbound-processor:diploma
```

```bash
minikube image load outbound-processor:diploma
```

```bash
terraform init
```

```bash
terraform apply -auto-approve
```