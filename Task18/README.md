# Helm Deployment for Spring Petclinic Application with MySQL and PostgreSQL

## Overview

This README provides instructions for deploying the Spring Petclinic application with MySQL and PostgreSQL databases using Helm. The setup includes PersistentVolume and PersistentVolumeClaim configurations for both databases, along with ConfigMaps, Secrets, Deployments, Services, and PersistentVolumeClaims.

## Directory Structure

The Helm chart is organized into the following directories:

```
helm/
├── charts/
│   ├── configmaps/
│   │   └── configmap.yml
│   ├── deployments/
│   │   ├── app-deployment.yml
│   │   └── database-deployment.yml
│   ├── secrets/
│   │   └── secret.yml
│   ├── services/
│   │   ├── app-service.yml
│   │   └── database-service.yml
│   └── volumes/
│       ├── pvc.yml
├── values.yml
├── values-dev.yml
└── values-prod.yml
pv/
├── mysql-pv.yml
└── postgres-pv.yml
```

## Files and Their Purpose

### 1. `charts/configmaps/configmap.yml`

Defines a ConfigMap for storing database URLs and names.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ .Values.configmap.name }}
data:
  database_url: {{ .Values.configmap.data.database_url }}
  database_name: {{ .Values.configmap.data.database_name }}
```

### 2. `charts/secrets/secret.yml`

Defines a Secret for securely storing database credentials.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: {{ .Values.secret.name }}
type: Opaque
data:
  username: {{ .Values.secret.data.username }}
  password: {{ .Values.secret.data.password }}
  root_password: {{ .Values.secret.data.root_password }}
```

### 3. `charts/deployments/app-deployment.yml`

Deployment configuration for the Spring Petclinic application.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Values.appName }}
  labels:
    app: {{ .Values.appName }}
spec:
  replicas: {{ .Values.appDeployment.replicaNumber }}
  selector:
    matchLabels:
      app: {{ .Values.appName }}
  template:
    metadata:
      labels:
        app: {{ .Values.appName }}
    spec:
      initContainers:
      - name: {{ .Values.appDeployment.initName }}
        image: {{ .Values.appDeployment.initImage }}
        command:
          - 'sh'
          - '-c'
          - "until nslookup {{ .Values.databaseService.name }}.{{ .Values.namespace }}.svc.cluster.local; do echo waiting for {{ .Values.database.databaseName }}; sleep 2; done;"
      containers:
      - name: {{ .Values.appName }}
        image: {{ .Values.appDeployment.image }}
        ports:
        - containerPort: {{ .Values.appPort }}
        env:
        - name: {{ .Values.appDeployment.profileName }}
          value: {{ .Values.appDeployment.profileValue }}
        - name: {{ .Values.appDeployment.databaseURL }}
          valueFrom:
            configMapKeyRef:
              name: {{ .Values.configmap.name }}
              key: database_url
```

### 4. `charts/deployments/database-deployment.yml`

Deployment configuration for the MySQL or PostgreSQL database.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Values.database.databaseName }}
  labels:
    app: {{ .Values.database.databaseName }}
spec:
  replicas: {{ .Values.database.replicaNumber }}
  selector:
    matchLabels:
      app: {{ .Values.database.databaseName }}
  template:
    metadata:
      labels:
        app: {{ .Values.database.databaseName }}
    spec:
      containers:
      - name: {{ .Values.database.databaseName }}
        image: {{ .Values.database.image }}
        ports:
        - containerPort: {{ .Values.database.port }}
        env:
        - name: {{ .Values.database.environment.username }}
          valueFrom:
            secretKeyRef:
              name: {{ .Values.secret.name }}
              key: username
        - name: {{ .Values.database.environment.password }}
          valueFrom:
            secretKeyRef:
              name: {{ .Values.secret.name }}
              key: password
        - name: {{ .Values.database.environment.rootPassword }}
          valueFrom:
            secretKeyRef:
              name: {{ .Values.secret.name }}
              key: root_password
        - name: {{ .Values.database.environment.databaseName }}
          valueFrom:
            configMapKeyRef:
              name: {{ .Values.configmap.name }}
              key: database_name
        volumeMounts:
        - name: {{ .Values.database.volume.name }}
          mountPath: {{ .Values.database.volume.mountPath }}
      volumes:
      - name: {{ .Values.database.volume.name }}
        persistentVolumeClaim:
          claimName: {{ .Values.pvc.name }}
```

### 5. `charts/services/app-service.yml`

Service configuration for the Spring Petclinic application.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: {{ .Values.appName }}
spec:
  selector:
    app: {{ .Values.appName }}
  type: LoadBalancer
  ports:
    - protocol: TCP
      port: {{ .Values.appPort }}
      targetPort: {{ .Values.appPort }}
      nodePort: {{ .Values.appService.nodePort }}
```

### 6. `charts/services/database-service.yml`

Service configuration for the MySQL or PostgreSQL database.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: {{ .Values.databaseService.name }}
spec:
  selector:
    app: {{ .Values.database.databaseName }}
  ports:
    - protocol: TCP
      port: {{ .Values.database.port }}
      targetPort: {{ .Values.database.port }}
```

### 7. `charts/volumes/pv/mysql-pv.yml` and `charts/volumes/pv/postgres-pv.yml`

PersistentVolume configurations for MySQL and PostgreSQL storage.

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: spring-mysql-pv
spec:
  capacity:
    storage: 1Gi
  volumeMode: Filesystem
  accessModes:
  - ReadWriteMany
  persistentVolumeReclaimPolicy: Retain
  storageClassName: local-storage
  local:
    path: /home/docker/volumes/spring-mysql
  nodeAffinity:
    required:
      nodeSelectorTerms:
      - matchExpressions:
        - key: kubernetes.io/hostname
          operator: In
          values:
          - minikube
```

### 8. `charts/volumes/pvc.yml`

PersistentVolumeClaim to request storage from the PersistentVolume.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: {{ .Values.pvc.name }}
spec:
  volumeName: {{ .Values.pv.name }}
  storageClassName: local-storage
  volumeMode: Filesystem
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 1Gi
```

## Values Files

### `values.yml`

Contains default values for the application and database configurations.

### `values-dev.yml`

Overrides default values for the development environment.

### `values-prod.yml`

Overrides default values for the production environment, including PostgreSQL configurations.

## Deployment Instructions

1. **Install Helm:**
   Make sure Helm is installed and configured on your local machine.

2. **Deploy the Helm Chart:**
   Install the Helm chart using the appropriate values file:

   ```bash
   helm install spring-petclinic ./helm --values values.yml
   ```

   For development:

   ```bash
   helm install spring-petclinic-dev ./helm --values values-dev.yml
   ```

   For production:

   ```bash
   helm install spring-petclinic-prod ./helm --values values-prod.yml
   ```

3. **Verify Deployment:**
   Check the status of the deployments and services:

   ```bash
   kubectl get deployments
   kubectl get services
   kubectl get pods
   ```

4. **Access the Application:**
   Use the external IP or NodePort to access the Spring Petclinic application from your browser.

## Conclusion

This Helm chart simplifies the deployment of the Spring Petclinic application with MySQL and PostgreSQL databases on Kubernetes. For further customization and management, refer to the Helm documentation on [Using Helm](https://helm.sh/docs/intro/using_helm/) and [Helm Templates](https://helm.sh/docs/chart_template_guide/).
