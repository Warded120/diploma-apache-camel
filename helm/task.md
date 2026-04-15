# Lab Work №3
**Subject:** "DevOps Technologies" — Topic: "Kubernetes and Helm"

## Instructions for the Student

---

## 1. Setting Up Minikube and kubectl

Install minikube and kubectl for your OS ([minikube.sigs.k8s.io](https://minikube.sigs.k8s.io)). Start the cluster:

```bash
minikube start --driver=docker
kubectl get nodes   # expected status: Ready
```

> **Common error:** minikube cannot find Docker — make sure Docker is running, or use `--driver=virtualbox`.

---

## 2. Branch `kubectl-only` — Application & Database via kubectl

Create a branch and a folder for manifests:

```bash
git checkout -b kubectl-only
mkdir k8s
```

### Example `k8s/app-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app
spec:
  replicas: 2
  selector:
    matchLabels: { app: myapp }
  template:
    metadata:
      labels: { app: myapp }
    spec:
      containers:
        - name: myapp
          image: myapp:latest
          env:
            - name: DB_HOST
              value: postgres-svc
          readinessProbe:
            httpGet: { path: /health, port: 8080 }
            initialDelaySeconds: 5
            periodSeconds: 10
          livenessProbe:
            httpGet: { path: /health, port: 8080 }
            initialDelaySeconds: 15
            periodSeconds: 20
```

Separately, create `k8s/db-deployment.yaml` with a Deployment and Service for your database.

### Deployment:

```bash
kubectl apply -f k8s/
kubectl get pods -w        # wait for Running status on all pods
kubectl describe pod       # diagnostics on errors
```

> **Common error:** `ImagePullBackOff` — the image is not available in minikube. Run `eval $(minikube docker-env)` and rebuild the image.

```bash
git add k8s/ && git commit -m 'feat: kubectl-only manifests'
git push origin kubectl-only
```

---

## 3. Installing Helm

```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
helm version
```

---

## 4. Branch `helm-deploy` — Custom Chart + Public Chart for the Database

Create a new branch from `kubectl-only`:

```bash
git checkout kubectl-only
git checkout -b helm-deploy
mkdir helm
```

### 4a. Custom Helm Chart for the Application

```bash
helm create helm/myapp-chart
```

Edit `helm/myapp-chart/values.yaml`:

```yaml
image: myapp:latest
replicaCount: 2
service:
  port: 8080
db:
  host: mydb-postgresql   # bitnami chart service name
  port: 5432
```

Remove unnecessary templates (`hpa.yaml`, `ingress.yaml`, etc.), keeping only `deployment.yaml` and `service.yaml`. In `deployment.yaml`, substitute `{{ .Values.image }}`, `{{ .Values.replicaCount }}`, `{{ .Values.db.host }}`.

```bash
helm install myapp ./helm/myapp-chart
kubectl get pods    # application pods Running
helm list           # release myapp is present
```

You should setup kafka deployment similarly to postgres. Use bitnami kafka chart.

> **Common error:** `resource already exists` — delete old resources with `kubectl delete` or use `helm upgrade --install`.

### 4b. Database via Public Bitnami Chart (mandatory — no manual YAML for the DB)

In the `helm-deploy` branch, the database is deployed **exclusively** through the public chart. Manual YAML manifests for the database are **not permitted** in this branch.

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install mydb bitnami/postgresql \
  --set auth.postgresPassword=secret \
  --set auth.database=myapp_db
kubectl get pods    # mydb-postgresql-0 Running
```

Do the same for Kafka

```bash

Make sure the application connects to the database: the `DB_HOST` env variable must match the bitnami service name (`mydb-postgresql`).

> **Common error:** `PVC Pending` — StorageClass is missing. Run:
> ```bash
> minikube addons enable default-storageclass
> minikube addons enable storage-provisioner
> ```

---

## 5. Helm Upgrade and Rollback

Change the replica count via `--set` or in `values.yaml`:

```bash
helm upgrade myapp ./helm/myapp-chart --set replicaCount=4
kubectl get pods        # 4 application pods
helm history myapp      # view revisions
helm rollback myapp 1   # roll back to revision 1
kubectl get pods        # replica count has been restored
```