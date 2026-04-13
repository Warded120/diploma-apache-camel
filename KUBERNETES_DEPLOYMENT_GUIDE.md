# Kubernetes Deployment Guide: kubectl vs Helm

This guide provides a complete walkthrough for deploying the Apache Camel application to Kubernetes using two approaches:
1. **Manual deployment with kubectl** (branch: `kubectl-only`)
2. **Helm-based deployment** (branch: `helm-deploy`)

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Part 1: Manual Deployment with kubectl](#part-1-manual-deployment-with-kubectl)
- [Part 2: What is Helm?](#part-2-what-is-helm)
- [Part 3: Deployment with Helm](#part-3-deployment-with-helm)
- [Part 4: Helm Operations (Upgrade & Rollback)](#part-4-helm-operations-upgrade--rollback)
- [Comparison: kubectl vs Helm](#comparison-kubectl-vs-helm)

---

## Prerequisites

### 1. Install minikube

Minikube creates a local Kubernetes cluster on your machine.

**Windows (using Chocolatey):**
```powershell
choco install minikube
```

**Windows (using winget):**
```powershell
winget install Kubernetes.minikube
```

**Verify installation:**
```powershell
minikube version
```

### 2. Install kubectl

kubectl is the Kubernetes command-line tool.

**Windows (using Chocolatey):**
```powershell
choco install kubernetes-cli
```

**Windows (using winget):**
```powershell
winget install Kubernetes.kubectl
```

**Verify installation:**
```powershell
kubectl version --client
```

### 3. Start Minikube Cluster

```powershell
# Start minikube with sufficient resources
minikube start --memory=4096 --cpus=2

# Verify cluster is running
minikube status

# Check cluster info
kubectl cluster-info
```

### 4. Build and Load Docker Images into Minikube

Since minikube runs in its own VM/container, you need to make your Docker images available to it.

**Option A: Use minikube's Docker daemon (recommended):**
```powershell
# Point your shell to minikube's Docker daemon
minikube docker-env | Invoke-Expression

# Build images (they will be available inside minikube)
docker build -t inbound-processor:latest --target inbound-processor .
docker build -t outbound-processor:latest --target outbound-processor .
```

**Option B: Load pre-built images:**
```powershell
# Build locally first
docker build -t inbound-processor:latest --target inbound-processor .
docker build -t outbound-processor:latest --target outbound-processor .

# Load into minikube
minikube image load inbound-processor:latest
minikube image load outbound-processor:latest
```

---

## Part 1: Manual Deployment with kubectl

> **Branch:** `kubectl-only`  
> **Manifests location:** `k8s/` folder

### Overview

In this approach, we manually create YAML manifest files for each Kubernetes resource:
- **Deployment**: Defines how to run your application (replicas, containers, probes)
- **Service**: Exposes your application within/outside the cluster
- **ConfigMap/Secret**: Store configuration and sensitive data

### Step 1: Create the k8s Directory Structure

```powershell
# Create branch and folder
git checkout -b kubectl-only
mkdir k8s
```

### Step 2: Create Namespace (Optional but Recommended)

**File: `k8s/namespace.yaml`**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: diploma-app
  labels:
    app: diploma-camel
```

**Explanation:**
- Namespaces provide logical isolation for resources
- Helps organize and manage related resources together

**Apply:**
```powershell
kubectl apply -f k8s/namespace.yaml
```

---

### Step 3: PostgreSQL Database Deployment

#### 3.1 Create Secret for Database Credentials

**File: `k8s/postgres-secret.yaml`**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
  namespace: diploma-app
type: Opaque
stringData:
  POSTGRES_USER: diploma
  POSTGRES_PASSWORD: password
  POSTGRES_DB: diploma
```

**Explanation:**
- `Secret` stores sensitive data (passwords, tokens)
- `stringData` allows plain text (Kubernetes encodes it to base64)
- Never commit real secrets to version control in production!

---

#### 3.2 Create PersistentVolumeClaim for Data Storage

**File: `k8s/postgres-pvc.yaml`**
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: diploma-app
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

**Explanation:**
- `PersistentVolumeClaim (PVC)` requests storage from the cluster
- `ReadWriteOnce` - volume can be mounted by a single node
- Data persists even if the pod is deleted

---

#### 3.3 Create PostgreSQL Deployment

**File: `k8s/postgres-deployment.yaml`**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: diploma-app
  labels:
    app: postgres
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16.2
          ports:
            - containerPort: 5432
          envFrom:
            - secretRef:
                name: postgres-secret
          volumeMounts:
            - name: postgres-storage
              mountPath: /var/lib/postgresql/data
          readinessProbe:
            exec:
              command:
                - pg_isready
                - -U
                - diploma
                - -d
                - diploma
            initialDelaySeconds: 10
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 5
          livenessProbe:
            exec:
              command:
                - pg_isready
                - -U
                - diploma
                - -d
                - diploma
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 5
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
      volumes:
        - name: postgres-storage
          persistentVolumeClaim:
            claimName: postgres-pvc
```

**Key Concepts Explained:**

| Field | Description |
|-------|-------------|
| `replicas: 1` | Number of pod instances to run |
| `selector.matchLabels` | How the Deployment finds which Pods to manage |
| `template` | Pod specification template |
| `envFrom.secretRef` | Load all keys from a Secret as environment variables |
| `volumeMounts` | Mount a volume inside the container |
| `readinessProbe` | Kubernetes uses this to know when the pod can receive traffic |
| `livenessProbe` | Kubernetes uses this to know when to restart a container |
| `resources` | CPU and memory requests/limits |

---

#### 3.4 Create PostgreSQL Service

**File: `k8s/postgres-service.yaml`**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: diploma-app
  labels:
    app: postgres
spec:
  type: ClusterIP
  ports:
    - port: 5432
      targetPort: 5432
      protocol: TCP
  selector:
    app: postgres
```

**Explanation:**
- `Service` provides stable networking for pods
- `ClusterIP` - accessible only within the cluster (default type)
- `selector` routes traffic to pods with matching labels
- Other pods connect using `postgres:5432` (service-name:port)

---

### Step 4: Kafka Deployment

#### 4.1 Create Kafka Deployment

**File: `k8s/kafka-deployment.yaml`**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
  namespace: diploma-app
  labels:
    app: kafka
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
        - name: kafka
          image: confluentinc/cp-kafka:8.1.0
          ports:
            - containerPort: 9092
              name: external
            - containerPort: 29092
              name: internal
            - containerPort: 9101
              name: jmx
          env:
            - name: CLUSTER_ID
              value: "FTU3DJFYATCWNT7FJ3M2P0"
            - name: KAFKA_ADVERTISED_LISTENERS
              value: "PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092"
            - name: KAFKA_CONTROLLER_LISTENER_NAMES
              value: "CONTROLLER"
            - name: KAFKA_CONTROLLER_QUORUM_VOTERS
              value: "1@kafka:29093"
            - name: KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS
              value: "0"
            - name: KAFKA_INTER_BROKER_LISTENER_NAME
              value: "PLAINTEXT"
            - name: KAFKA_JMX_HOSTNAME
              value: "localhost"
            - name: KAFKA_JMX_PORT
              value: "9101"
            - name: KAFKA_LISTENERS
              value: "PLAINTEXT://kafka:29092,CONTROLLER://kafka:29093,PLAINTEXT_HOST://0.0.0.0:9092"
            - name: KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
              value: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT"
            - name: KAFKA_NODE_ID
              value: "1"
            - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
              value: "1"
            - name: KAFKA_PROCESS_ROLES
              value: "broker,controller"
            - name: KAFKA_TRANSACTION_STATE_LOG_MIN_ISR
              value: "1"
            - name: KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
              value: "1"
          readinessProbe:
            exec:
              command:
                - kafka-topics
                - --bootstrap-server
                - localhost:9092
                - --list
            initialDelaySeconds: 10
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 5
          livenessProbe:
            exec:
              command:
                - kafka-topics
                - --bootstrap-server
                - localhost:9092
                - --list
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 5
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
```

---

#### 4.2 Create Kafka Service

**File: `k8s/kafka-service.yaml`**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: kafka
  namespace: diploma-app
  labels:
    app: kafka
spec:
  type: ClusterIP
  ports:
    - port: 9092
      targetPort: 9092
      name: external
    - port: 29092
      targetPort: 29092
      name: internal
  selector:
    app: kafka
```

---

### Step 5: Application ConfigMap

**File: `k8s/app-configmap.yaml`**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: diploma-app
data:
  # Database connection
  DB_HOST: "postgres"
  DB_PORT: "5432"
  DB_NAME: "diploma"
  
  # Kafka connection
  KAFKA_BROKERS: "kafka:9092"
  KAFKA_TOPIC: "topic-inbound-processor"
  KAFKA_DLT_TOPIC: "topic-inbound-processor-dlt"
```

**Explanation:**
- `ConfigMap` stores non-sensitive configuration data
- Can be mounted as environment variables or files
- Separates configuration from container images

---

### Step 6: Inbound Processor Deployment

**File: `k8s/inbound-processor-deployment.yaml`**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: inbound-processor
  namespace: diploma-app
  labels:
    app: inbound-processor
spec:
  replicas: 1
  selector:
    matchLabels:
      app: inbound-processor
  template:
    metadata:
      labels:
        app: inbound-processor
    spec:
      containers:
        - name: inbound-processor
          image: inbound-processor:latest
          imagePullPolicy: Never  # Use local image from minikube
          ports:
            - containerPort: 8080
          env:
            - name: APP_PROFILE
              value: "kubernetes"
            # Database credentials from secret
            - name: DB_USER
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: POSTGRES_USER
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: POSTGRES_PASSWORD
          envFrom:
            - configMapRef:
                name: app-config
          # Readiness Probe - HTTP check
          readinessProbe:
            httpGet:
              path: /q/health/ready
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 3
          # Liveness Probe - HTTP check  
          livenessProbe:
            httpGet:
              path: /q/health/live
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
            timeoutSeconds: 10
            failureThreshold: 3
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
      # Wait for dependencies
      initContainers:
        - name: wait-for-postgres
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z postgres 5432; do echo waiting for postgres; sleep 2; done']
        - name: wait-for-kafka
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z kafka 9092; do echo waiting for kafka; sleep 2; done']
```

**Key Concepts Explained:**

| Field | Description |
|-------|-------------|
| `imagePullPolicy: Never` | Use locally loaded image (minikube) |
| `valueFrom.secretKeyRef` | Get single value from a Secret |
| `envFrom.configMapRef` | Load all ConfigMap keys as env vars |
| `httpGet` probe | Health check via HTTP endpoint |
| `initContainers` | Containers that run before main containers |

---

### Step 7: Inbound Processor Service

**File: `k8s/inbound-processor-service.yaml`**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: inbound-processor
  namespace: diploma-app
  labels:
    app: inbound-processor
spec:
  type: NodePort  # Accessible from outside the cluster
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30081  # External port (30000-32767)
      protocol: TCP
  selector:
    app: inbound-processor
```

**Service Types Explained:**

| Type | Description |
|------|-------------|
| `ClusterIP` | Internal only (default) |
| `NodePort` | Exposes on each node's IP at a static port |
| `LoadBalancer` | Exposes externally using cloud load balancer |

---

### Step 8: Outbound Processor Deployment

**File: `k8s/outbound-processor-deployment.yaml`**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: outbound-processor
  namespace: diploma-app
  labels:
    app: outbound-processor
spec:
  replicas: 1
  selector:
    matchLabels:
      app: outbound-processor
  template:
    metadata:
      labels:
        app: outbound-processor
    spec:
      containers:
        - name: outbound-processor
          image: outbound-processor:latest
          imagePullPolicy: Never
          ports:
            - containerPort: 8080
          env:
            - name: APP_PROFILE
              value: "kubernetes"
            - name: KAFKA_GROUP_ID
              value: "outbound-processor-group"
            - name: DB_USER
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: POSTGRES_USER
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: POSTGRES_PASSWORD
          envFrom:
            - configMapRef:
                name: app-config
          readinessProbe:
            httpGet:
              path: /q/health/ready
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 3
          livenessProbe:
            httpGet:
              path: /q/health/live
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
            timeoutSeconds: 10
            failureThreshold: 3
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
      initContainers:
        - name: wait-for-postgres
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z postgres 5432; do echo waiting for postgres; sleep 2; done']
        - name: wait-for-kafka
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z kafka 9092; do echo waiting for kafka; sleep 2; done']
```

---

### Step 9: Outbound Processor Service

**File: `k8s/outbound-processor-service.yaml`**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: outbound-processor
  namespace: diploma-app
  labels:
    app: outbound-processor
spec:
  type: NodePort
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30082
      protocol: TCP
  selector:
    app: outbound-processor
```

---

### Step 10: Deploy Everything

**Apply all manifests in order:**
```powershell
# Create namespace first
kubectl apply -f k8s/namespace.yaml

# Deploy secrets and configmaps
kubectl apply -f k8s/postgres-secret.yaml
kubectl apply -f k8s/app-configmap.yaml

# Deploy storage
kubectl apply -f k8s/postgres-pvc.yaml

# Deploy database
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/postgres-service.yaml

# Wait for PostgreSQL to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n diploma-app --timeout=120s

# Deploy Kafka
kubectl apply -f k8s/kafka-deployment.yaml
kubectl apply -f k8s/kafka-service.yaml

# Wait for Kafka to be ready
kubectl wait --for=condition=ready pod -l app=kafka -n diploma-app --timeout=180s

# Deploy applications
kubectl apply -f k8s/inbound-processor-deployment.yaml
kubectl apply -f k8s/inbound-processor-service.yaml
kubectl apply -f k8s/outbound-processor-deployment.yaml
kubectl apply -f k8s/outbound-processor-service.yaml
```

**Or apply all at once:**
```powershell
kubectl apply -f k8s/
```

---

### Step 11: Verify Deployment

```powershell
# Check all resources in namespace
kubectl get all -n diploma-app

# Check pod status
kubectl get pods -n diploma-app

# Check pod logs
kubectl logs -f deployment/inbound-processor -n diploma-app
kubectl logs -f deployment/outbound-processor -n diploma-app

# Describe pod for troubleshooting
kubectl describe pod -l app=inbound-processor -n diploma-app

# Access the application
minikube service inbound-processor -n diploma-app --url
```

---

### Step 12: Useful kubectl Commands

```powershell
# Scale deployment manually
kubectl scale deployment inbound-processor --replicas=3 -n diploma-app

# View deployment history
kubectl rollout history deployment/inbound-processor -n diploma-app

# Rollback to previous version
kubectl rollout undo deployment/inbound-processor -n diploma-app

# Delete all resources
kubectl delete -f k8s/

# Port forward for debugging
kubectl port-forward svc/inbound-processor 8081:8080 -n diploma-app
```

---

## Part 2: What is Helm?

### Definition

**Helm** is the package manager for Kubernetes. It helps you define, install, and upgrade complex Kubernetes applications.

Think of it as:
- **apt/yum** for Linux packages
- **npm** for Node.js packages
- **Maven** for Java dependencies

But for Kubernetes applications!

### Key Concepts

#### 1. Chart
A **Chart** is a Helm package containing all resource definitions needed to run an application in Kubernetes.

```
mychart/
├── Chart.yaml          # Chart metadata (name, version, description)
├── values.yaml         # Default configuration values
├── charts/             # Dependencies (sub-charts)
└── templates/          # Kubernetes manifest templates
    ├── deployment.yaml
    ├── service.yaml
    ├── _helpers.tpl    # Template helpers
    └── NOTES.txt       # Post-install notes
```

#### 2. Values
**Values** are configuration that can be customized during install/upgrade.

```yaml
# values.yaml
replicaCount: 2
image:
  repository: myapp
  tag: "1.0.0"
```

Override during install:
```powershell
helm install myapp ./mychart --set replicaCount=3
```

#### 3. Release
A **Release** is a running instance of a Chart. You can install the same chart multiple times with different names.

```powershell
helm install prod-app ./mychart
helm install staging-app ./mychart
```

#### 4. Repository
A **Repository** is a place where charts are stored and shared (like Docker Hub for images).

```powershell
# Add Bitnami repository
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

### Why Use Helm?

| Feature | kubectl (Manual) | Helm |
|---------|-----------------|------|
| **Templating** | ❌ Copy-paste for each environment | ✅ Single template, multiple values |
| **Versioning** | ❌ Manual tracking | ✅ Built-in release versioning |
| **Rollback** | ❌ Manual process | ✅ `helm rollback` command |
| **Dependencies** | ❌ Manual management | ✅ Automatic dependency resolution |
| **Upgrades** | ❌ Delete and recreate | ✅ `helm upgrade` with diff |
| **Sharing** | ❌ Share YAML files | ✅ Share via repositories |

### Install Helm

**Windows (Chocolatey):**
```powershell
choco install kubernetes-helm
```

**Windows (Scoop):**
```powershell
scoop install helm
```

**Windows (winget):**
```powershell
winget install Helm.Helm
```

**Verify installation:**
```powershell
helm version
```

---

## Part 3: Deployment with Helm

> **Branch:** `helm-deploy`  
> **Chart location:** `helm/` folder

### Step 1: Create Branch and Setup

```powershell
git checkout -b helm-deploy
mkdir helm
cd helm
```

### Step 2: Create Helm Chart for Application

```powershell
# Create chart scaffold
helm create diploma-app

# This creates:
# diploma-app/
# ├── Chart.yaml
# ├── values.yaml
# ├── charts/
# └── templates/
```

### Step 3: Configure Chart.yaml

**File: `helm/diploma-app/Chart.yaml`**
```yaml
apiVersion: v2
name: diploma-app
description: Apache Camel microservices application
type: application
version: 0.1.0        # Chart version
appVersion: "1.0.0"   # Application version

# Dependencies - Kafka will also be deployed via Bitnami chart
dependencies: []
```

**Explanation:**
- `apiVersion: v2` - Helm 3 format
- `version` - Chart version (for chart updates)
- `appVersion` - Your application version

---

### Step 4: Configure values.yaml

**File: `helm/diploma-app/values.yaml`**
```yaml
# Global settings
global:
  namespace: diploma-app

# Inbound Processor Configuration
inboundProcessor:
  enabled: true
  replicaCount: 1
  
  image:
    repository: inbound-processor
    tag: "latest"
    pullPolicy: Never  # Use local minikube image
  
  service:
    type: NodePort
    port: 8080
    nodePort: 30081
  
  resources:
    requests:
      memory: "256Mi"
      cpu: "250m"
    limits:
      memory: "512Mi"
      cpu: "500m"
  
  # Health probes
  probes:
    readiness:
      path: /q/health/ready
      initialDelaySeconds: 30
      periodSeconds: 10
    liveness:
      path: /q/health/live
      initialDelaySeconds: 60
      periodSeconds: 30

# Outbound Processor Configuration
outboundProcessor:
  enabled: true
  replicaCount: 1
  
  image:
    repository: outbound-processor
    tag: "latest"
    pullPolicy: Never
  
  service:
    type: NodePort
    port: 8080
    nodePort: 30082
  
  resources:
    requests:
      memory: "256Mi"
      cpu: "250m"
    limits:
      memory: "512Mi"
      cpu: "500m"
  
  probes:
    readiness:
      path: /q/health/ready
      initialDelaySeconds: 30
      periodSeconds: 10
    liveness:
      path: /q/health/live
      initialDelaySeconds: 60
      periodSeconds: 30
  
  kafka:
    groupId: "outbound-processor-group"

# Application Configuration
config:
  appProfile: "kubernetes"
  kafka:
    brokers: "kafka-diploma:9092"
    topic: "topic-inbound-processor"
    dltTopic: "topic-inbound-processor-dlt"
  database:
    host: "postgresql-diploma"
    port: "5432"
    name: "diploma"

# Database credentials (should use external secret in production)
secrets:
  database:
    username: "diploma"
    password: "password"
```

**Key Points:**
- All hardcoded values are now parameterized
- Can easily change replicas, images, ports
- Separate sections for each component
- Sensitive data in `secrets` section

---

### Step 5: Create Template Helpers

**File: `helm/diploma-app/templates/_helpers.tpl`**
```yaml
{{/*
Expand the name of the chart.
*/}}
{{- define "diploma-app.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "diploma-app.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "diploma-app.labels" -}}
helm.sh/chart: {{ include "diploma-app.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Chart label
*/}}
{{- define "diploma-app.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Inbound processor labels
*/}}
{{- define "diploma-app.inbound.labels" -}}
{{ include "diploma-app.labels" . }}
app.kubernetes.io/name: inbound-processor
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Inbound processor selector labels
*/}}
{{- define "diploma-app.inbound.selectorLabels" -}}
app.kubernetes.io/name: inbound-processor
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Outbound processor labels
*/}}
{{- define "diploma-app.outbound.labels" -}}
{{ include "diploma-app.labels" . }}
app.kubernetes.io/name: outbound-processor
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Outbound processor selector labels
*/}}
{{- define "diploma-app.outbound.selectorLabels" -}}
app.kubernetes.io/name: outbound-processor
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
```

---

### Step 6: Create Secret Template

**File: `helm/diploma-app/templates/secret.yaml`**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "diploma-app.fullname" . }}-db-secret
  labels:
    {{- include "diploma-app.labels" . | nindent 4 }}
type: Opaque
stringData:
  POSTGRES_USER: {{ .Values.secrets.database.username | quote }}
  POSTGRES_PASSWORD: {{ .Values.secrets.database.password | quote }}
  POSTGRES_DB: {{ .Values.config.database.name | quote }}
```

---

### Step 7: Create ConfigMap Template

**File: `helm/diploma-app/templates/configmap.yaml`**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "diploma-app.fullname" . }}-config
  labels:
    {{- include "diploma-app.labels" . | nindent 4 }}
data:
  DB_HOST: {{ .Values.config.database.host | quote }}
  DB_PORT: {{ .Values.config.database.port | quote }}
  DB_NAME: {{ .Values.config.database.name | quote }}
  KAFKA_BROKERS: {{ .Values.config.kafka.brokers | quote }}
  KAFKA_TOPIC: {{ .Values.config.kafka.topic | quote }}
  KAFKA_DLT_TOPIC: {{ .Values.config.kafka.dltTopic | quote }}
```

---

### Step 8: Create Inbound Processor Deployment Template

**File: `helm/diploma-app/templates/inbound-deployment.yaml`**
```yaml
{{- if .Values.inboundProcessor.enabled }}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "diploma-app.fullname" . }}-inbound
  labels:
    {{- include "diploma-app.inbound.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.inboundProcessor.replicaCount }}
  selector:
    matchLabels:
      {{- include "diploma-app.inbound.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "diploma-app.inbound.selectorLabels" . | nindent 8 }}
    spec:
      initContainers:
        - name: wait-for-postgres
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z {{ .Values.config.database.host }} {{ .Values.config.database.port }}; do echo waiting for postgres; sleep 2; done']
        - name: wait-for-kafka
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z {{ .Values.config.kafka.brokers | replace ":9092" "" }} 9092; do echo waiting for kafka; sleep 2; done']
      containers:
        - name: inbound-processor
          image: "{{ .Values.inboundProcessor.image.repository }}:{{ .Values.inboundProcessor.image.tag }}"
          imagePullPolicy: {{ .Values.inboundProcessor.image.pullPolicy }}
          ports:
            - containerPort: {{ .Values.inboundProcessor.service.port }}
          env:
            - name: APP_PROFILE
              value: {{ .Values.config.appProfile | quote }}
            - name: DB_USER
              valueFrom:
                secretKeyRef:
                  name: {{ include "diploma-app.fullname" . }}-db-secret
                  key: POSTGRES_USER
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: {{ include "diploma-app.fullname" . }}-db-secret
                  key: POSTGRES_PASSWORD
          envFrom:
            - configMapRef:
                name: {{ include "diploma-app.fullname" . }}-config
          readinessProbe:
            httpGet:
              path: {{ .Values.inboundProcessor.probes.readiness.path }}
              port: {{ .Values.inboundProcessor.service.port }}
            initialDelaySeconds: {{ .Values.inboundProcessor.probes.readiness.initialDelaySeconds }}
            periodSeconds: {{ .Values.inboundProcessor.probes.readiness.periodSeconds }}
          livenessProbe:
            httpGet:
              path: {{ .Values.inboundProcessor.probes.liveness.path }}
              port: {{ .Values.inboundProcessor.service.port }}
            initialDelaySeconds: {{ .Values.inboundProcessor.probes.liveness.initialDelaySeconds }}
            periodSeconds: {{ .Values.inboundProcessor.probes.liveness.periodSeconds }}
          resources:
            {{- toYaml .Values.inboundProcessor.resources | nindent 12 }}
{{- end }}
```

---

### Step 9: Create Inbound Processor Service Template

**File: `helm/diploma-app/templates/inbound-service.yaml`**
```yaml
{{- if .Values.inboundProcessor.enabled }}
apiVersion: v1
kind: Service
metadata:
  name: {{ include "diploma-app.fullname" . }}-inbound
  labels:
    {{- include "diploma-app.inbound.labels" . | nindent 4 }}
spec:
  type: {{ .Values.inboundProcessor.service.type }}
  ports:
    - port: {{ .Values.inboundProcessor.service.port }}
      targetPort: {{ .Values.inboundProcessor.service.port }}
      {{- if eq .Values.inboundProcessor.service.type "NodePort" }}
      nodePort: {{ .Values.inboundProcessor.service.nodePort }}
      {{- end }}
      protocol: TCP
      name: http
  selector:
    {{- include "diploma-app.inbound.selectorLabels" . | nindent 4 }}
{{- end }}
```

---

### Step 10: Create Outbound Processor Deployment Template

**File: `helm/diploma-app/templates/outbound-deployment.yaml`**
```yaml
{{- if .Values.outboundProcessor.enabled }}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "diploma-app.fullname" . }}-outbound
  labels:
    {{- include "diploma-app.outbound.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.outboundProcessor.replicaCount }}
  selector:
    matchLabels:
      {{- include "diploma-app.outbound.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "diploma-app.outbound.selectorLabels" . | nindent 8 }}
    spec:
      initContainers:
        - name: wait-for-postgres
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z {{ .Values.config.database.host }} {{ .Values.config.database.port }}; do echo waiting for postgres; sleep 2; done']
        - name: wait-for-kafka
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z {{ .Values.config.kafka.brokers | replace ":9092" "" }} 9092; do echo waiting for kafka; sleep 2; done']
      containers:
        - name: outbound-processor
          image: "{{ .Values.outboundProcessor.image.repository }}:{{ .Values.outboundProcessor.image.tag }}"
          imagePullPolicy: {{ .Values.outboundProcessor.image.pullPolicy }}
          ports:
            - containerPort: {{ .Values.outboundProcessor.service.port }}
          env:
            - name: APP_PROFILE
              value: {{ .Values.config.appProfile | quote }}
            - name: KAFKA_GROUP_ID
              value: {{ .Values.outboundProcessor.kafka.groupId | quote }}
            - name: DB_USER
              valueFrom:
                secretKeyRef:
                  name: {{ include "diploma-app.fullname" . }}-db-secret
                  key: POSTGRES_USER
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: {{ include "diploma-app.fullname" . }}-db-secret
                  key: POSTGRES_PASSWORD
          envFrom:
            - configMapRef:
                name: {{ include "diploma-app.fullname" . }}-config
          readinessProbe:
            httpGet:
              path: {{ .Values.outboundProcessor.probes.readiness.path }}
              port: {{ .Values.outboundProcessor.service.port }}
            initialDelaySeconds: {{ .Values.outboundProcessor.probes.readiness.initialDelaySeconds }}
            periodSeconds: {{ .Values.outboundProcessor.probes.readiness.periodSeconds }}
          livenessProbe:
            httpGet:
              path: {{ .Values.outboundProcessor.probes.liveness.path }}
              port: {{ .Values.outboundProcessor.service.port }}
            initialDelaySeconds: {{ .Values.outboundProcessor.probes.liveness.initialDelaySeconds }}
            periodSeconds: {{ .Values.outboundProcessor.probes.liveness.periodSeconds }}
          resources:
            {{- toYaml .Values.outboundProcessor.resources | nindent 12 }}
{{- end }}
```

---

### Step 11: Create Outbound Processor Service Template

**File: `helm/diploma-app/templates/outbound-service.yaml`**
```yaml
{{- if .Values.outboundProcessor.enabled }}
apiVersion: v1
kind: Service
metadata:
  name: {{ include "diploma-app.fullname" . }}-outbound
  labels:
    {{- include "diploma-app.outbound.labels" . | nindent 4 }}
spec:
  type: {{ .Values.outboundProcessor.service.type }}
  ports:
    - port: {{ .Values.outboundProcessor.service.port }}
      targetPort: {{ .Values.outboundProcessor.service.port }}
      {{- if eq .Values.outboundProcessor.service.type "NodePort" }}
      nodePort: {{ .Values.outboundProcessor.service.nodePort }}
      {{- end }}
      protocol: TCP
      name: http
  selector:
    {{- include "diploma-app.outbound.selectorLabels" . | nindent 4 }}
{{- end }}
```

---

### Step 12: Create NOTES.txt

**File: `helm/diploma-app/templates/NOTES.txt`**
```
=======================================================
  Diploma Apache Camel Application Deployed!
=======================================================

Release Name: {{ .Release.Name }}
Namespace: {{ .Release.Namespace }}

{{- if .Values.inboundProcessor.enabled }}

📥 Inbound Processor:
   {{- if eq .Values.inboundProcessor.service.type "NodePort" }}
   Access via: minikube service {{ include "diploma-app.fullname" . }}-inbound -n {{ .Release.Namespace }} --url
   Or: http://<node-ip>:{{ .Values.inboundProcessor.service.nodePort }}
   {{- else }}
   Internal service: {{ include "diploma-app.fullname" . }}-inbound:{{ .Values.inboundProcessor.service.port }}
   {{- end }}
{{- end }}

{{- if .Values.outboundProcessor.enabled }}

📤 Outbound Processor:
   {{- if eq .Values.outboundProcessor.service.type "NodePort" }}
   Access via: minikube service {{ include "diploma-app.fullname" . }}-outbound -n {{ .Release.Namespace }} --url
   Or: http://<node-ip>:{{ .Values.outboundProcessor.service.nodePort }}
   {{- else }}
   Internal service: {{ include "diploma-app.fullname" . }}-outbound:{{ .Values.outboundProcessor.service.port }}
   {{- end }}
{{- end }}

Useful commands:
  kubectl get pods -n {{ .Release.Namespace }}
  kubectl logs -f deployment/{{ include "diploma-app.fullname" . }}-inbound -n {{ .Release.Namespace }}
  helm status {{ .Release.Name }} -n {{ .Release.Namespace }}
```

---

### Step 13: Deploy PostgreSQL using Bitnami Helm Chart

```powershell
# Add Bitnami repository
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Create namespace
kubectl create namespace diploma-app

# Deploy PostgreSQL using Bitnami chart
helm install postgresql-diploma bitnami/postgresql `
  --namespace diploma-app `
  --set auth.username=diploma `
  --set auth.password=password `
  --set auth.database=diploma `
  --set primary.persistence.size=1Gi
```

**Explanation:**
- `bitnami/postgresql` - Official PostgreSQL chart from Bitnami
- `auth.*` - Database credentials
- `primary.persistence.size` - Storage size for data

**Verify PostgreSQL is running:**
```powershell
kubectl get pods -n diploma-app -l app.kubernetes.io/name=postgresql
```

---

### Step 14: Deploy Kafka using Bitnami Helm Chart

```powershell
# Deploy Kafka using Bitnami chart
helm install kafka-diploma bitnami/kafka `
  --namespace diploma-app `
  --set controller.replicaCount=1 `
  --set listeners.client.protocol=PLAINTEXT `
  --set listeners.controller.protocol=PLAINTEXT `
  --set listeners.interbroker.protocol=PLAINTEXT
```

**Verify Kafka is running:**
```powershell
kubectl get pods -n diploma-app -l app.kubernetes.io/name=kafka
```

---

### Step 15: Deploy Application Chart

```powershell
# Navigate to helm directory
cd helm

# Validate chart syntax
helm lint diploma-app

# See what would be deployed (dry-run)
helm install diploma-release ./diploma-app `
  --namespace diploma-app `
  --dry-run --debug

# Deploy the application
helm install diploma-release ./diploma-app `
  --namespace diploma-app
```

**Verify deployment:**
```powershell
# Check release status
helm status diploma-release -n diploma-app

# List all releases
helm list -n diploma-app

# Check pods
kubectl get pods -n diploma-app
```

---

### Step 16: Access the Application

```powershell
# Get service URLs
minikube service diploma-release-diploma-app-inbound -n diploma-app --url
minikube service diploma-release-diploma-app-outbound -n diploma-app --url

# Or use port-forward
kubectl port-forward svc/diploma-release-diploma-app-inbound 8081:8080 -n diploma-app
```

---

## Part 4: Helm Operations (Upgrade & Rollback)

### Upgrade: Change Number of Replicas

**Method 1: Using --set flag**
```powershell
# Scale inbound-processor to 3 replicas
helm upgrade diploma-release ./diploma-app `
  --namespace diploma-app `
  --set inboundProcessor.replicaCount=3

# Verify scaling
kubectl get pods -n diploma-app -l app.kubernetes.io/name=inbound-processor
```

**Method 2: Using values file**

Create `helm/values-production.yaml`:
```yaml
inboundProcessor:
  replicaCount: 3
  resources:
    requests:
      memory: "512Mi"
      cpu: "500m"
    limits:
      memory: "1Gi"
      cpu: "1000m"

outboundProcessor:
  replicaCount: 2
```

Apply:
```powershell
helm upgrade diploma-release ./diploma-app `
  --namespace diploma-app `
  -f values-production.yaml
```

**Method 3: Edit values.yaml and upgrade**
```powershell
# Edit helm/diploma-app/values.yaml, change replicaCount to 3
helm upgrade diploma-release ./diploma-app --namespace diploma-app
```

---

### View Upgrade History

```powershell
# Show release history
helm history diploma-release -n diploma-app
```

Output:
```
REVISION  UPDATED                   STATUS      CHART             APP VERSION  DESCRIPTION
1         Mon Apr 13 10:00:00 2026  superseded  diploma-app-0.1.0 1.0.0        Install complete
2         Mon Apr 13 10:30:00 2026  deployed    diploma-app-0.1.0 1.0.0        Upgrade complete
```

---

### Rollback to Previous Version

```powershell
# Rollback to revision 1
helm rollback diploma-release 1 -n diploma-app

# Verify rollback
helm history diploma-release -n diploma-app
kubectl get pods -n diploma-app
```

**Rollback explanation:**
- Helm keeps track of all releases
- `helm rollback <release> <revision>` reverts to a previous state
- Creates a new revision (doesn't delete history)

---

### Other Useful Helm Commands

```powershell
# Show current values
helm get values diploma-release -n diploma-app

# Show all computed values (including defaults)
helm get values diploma-release -n diploma-app --all

# Show deployed manifests
helm get manifest diploma-release -n diploma-app

# Uninstall release
helm uninstall diploma-release -n diploma-app

# Uninstall with keeping history
helm uninstall diploma-release -n diploma-app --keep-history
```

---

## Comparison: kubectl vs Helm

### Side-by-Side Summary

| Aspect | kubectl (Manual YAML) | Helm |
|--------|----------------------|------|
| **Files** | Multiple YAML files | Single chart package |
| **Configuration** | Edit YAML directly | `values.yaml` + `--set` |
| **Templating** | None (copy-paste) | Go templates |
| **Versioning** | Manual git tracking | Built-in revisions |
| **Rollback** | `kubectl rollout undo` (limited) | `helm rollback` (full state) |
| **Dependencies** | Manual order | Automatic resolution |
| **Reusability** | Low (hardcoded values) | High (parameterized) |
| **Sharing** | Share YAML files | Share via repositories |
| **Learning Curve** | Lower | Higher |
| **Best For** | Simple apps, learning | Production, complex apps |

### Project Structure Comparison

```
diploma-apache-camel/
├── k8s/                          # Branch: kubectl-only
│   ├── namespace.yaml
│   ├── postgres-secret.yaml
│   ├── postgres-pvc.yaml
│   ├── postgres-deployment.yaml
│   ├── postgres-service.yaml
│   ├── kafka-deployment.yaml
│   ├── kafka-service.yaml
│   ├── app-configmap.yaml
│   ├── inbound-processor-deployment.yaml
│   ├── inbound-processor-service.yaml
│   ├── outbound-processor-deployment.yaml
│   └── outbound-processor-service.yaml
│
└── helm/                         # Branch: helm-deploy
    └── diploma-app/
        ├── Chart.yaml
        ├── values.yaml
        ├── values-production.yaml
        └── templates/
            ├── _helpers.tpl
            ├── configmap.yaml
            ├── secret.yaml
            ├── inbound-deployment.yaml
            ├── inbound-service.yaml
            ├── outbound-deployment.yaml
            ├── outbound-service.yaml
            └── NOTES.txt
```

### Deployment Commands Comparison

**kubectl approach:**
```powershell
# Deploy
kubectl apply -f k8s/

# Scale
kubectl scale deployment inbound-processor --replicas=3 -n diploma-app

# Rollback (limited to deployment only)
kubectl rollout undo deployment/inbound-processor -n diploma-app

# Delete
kubectl delete -f k8s/
```

**Helm approach:**
```powershell
# Deploy dependencies (DB, Kafka)
helm install postgresql-diploma bitnami/postgresql -n diploma-app --set auth.username=diploma
helm install kafka-diploma bitnami/kafka -n diploma-app

# Deploy application
helm install diploma-release ./helm/diploma-app -n diploma-app

# Scale (upgrade)
helm upgrade diploma-release ./helm/diploma-app --set inboundProcessor.replicaCount=3 -n diploma-app

# Rollback (full state including configmaps, secrets)
helm rollback diploma-release 1 -n diploma-app

# Delete
helm uninstall diploma-release -n diploma-app
helm uninstall kafka-diploma -n diploma-app
helm uninstall postgresql-diploma -n diploma-app
```

---

## Quick Reference Commands

### Minikube
```powershell
minikube start                    # Start cluster
minikube stop                     # Stop cluster
minikube delete                   # Delete cluster
minikube dashboard                # Open Kubernetes dashboard
minikube service <svc> --url      # Get service URL
minikube docker-env | iex         # Use minikube's Docker
```

### kubectl
```powershell
kubectl get all -n <ns>           # List all resources
kubectl get pods -n <ns>          # List pods
kubectl logs -f <pod> -n <ns>     # Stream logs
kubectl describe pod <pod>        # Pod details
kubectl exec -it <pod> -- sh      # Shell into pod
kubectl port-forward svc/<s> 8080:80  # Port forward
kubectl apply -f <file>           # Apply manifest
kubectl delete -f <file>          # Delete resources
```

### Helm
```powershell
helm repo add <name> <url>        # Add repository
helm repo update                  # Update repositories
helm search repo <keyword>        # Search charts
helm install <rel> <chart>        # Install release
helm upgrade <rel> <chart>        # Upgrade release
helm rollback <rel> <rev>         # Rollback release
helm uninstall <rel>              # Uninstall release
helm list                         # List releases
helm history <rel>                # Release history
helm get values <rel>             # Get values
```

---

## Troubleshooting

### Common Issues

**1. Pod stuck in Pending:**
```powershell
kubectl describe pod <pod-name> -n diploma-app
# Check Events section for errors (usually resource limits or PVC issues)
```

**2. Image pull errors:**
```powershell
# Ensure images are loaded in minikube
minikube image list | Select-String "inbound"

# If not, load them
minikube image load inbound-processor:latest
```

**3. Connection refused to database:**
```powershell
# Check if PostgreSQL is running
kubectl get pods -n diploma-app -l app.kubernetes.io/name=postgresql

# Check service endpoints
kubectl get endpoints postgresql-diploma -n diploma-app
```

**4. Helm chart syntax errors:**
```powershell
# Validate chart
helm lint ./helm/diploma-app

# Debug template rendering
helm template diploma-release ./helm/diploma-app --debug
```

---

*Document created for Kubernetes deployment comparison: kubectl vs Helm*

