# Task 17: Kubernetes Setup for Spring Petclinic Application with MySQL Database

## Overview

This README provides a detailed guide on deploying the Spring Petclinic application with a MySQL database using Kubernetes. The setup is organized into various directories for ConfigMaps, Secrets, PersistentVolumes, Services, and Deployments.

## Directory Structure

The Kubernetes resource files are organized as follows:

```
kubernetes/
├── configmaps/
│   └── mysql-configmap.yml
├── deployments/
│   ├── mysql-deployment.yml
│   └── app-deployment.yml
├── secrets/
│   └── mysql-secret.yml
├── services/
│   ├── mysql-service.yml
│   └── app-service.yml
└── volumes/
    ├── mysql-pv.yml
    └── mysql-pvc.yml
```

## Files and Their Purpose

### 1. `kubernetes/configmaps/mysql-configmap.yml`

ConfigMap to store the MySQL database URL.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mysql-configmap
data:
  database_url: jdbc:mysql://mysql-service:3306/petclinic
```

### 2. `kubernetes/secrets/mysql-secret.yml`

Secret to securely store MySQL credentials.

```yaml
apiVersion: v1
kind: Secret
metadata:
    name: mysql-secret
type: Opaque
data:
    mysql-username: cGV0Y2xpbmlj
    mysql-password: cGV0Y2xpbmlj
    mysql-root-password: cm9vdA==
```

### 3. `kubernetes/volumes/mysql-pv.yml`

PersistentVolume configuration for MySQL storage.

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

### 4. `kubernetes/volumes/mysql-pvc.yml`

PersistentVolumeClaim to request storage from the PersistentVolume.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: spring-mysql-pvc
spec:
  volumeName: spring-mysql-pv
  storageClassName: local-storage
  volumeMode: Filesystem
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 1Gi
```

### 5. `kubernetes/services/mysql-service.yml`

Service configuration to expose the MySQL database.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
spec:
  selector:
    app: mysql
  ports:
    - protocol: TCP
      port: 3306
      targetPort: 3306
```

### 6. `kubernetes/services/app-service.yml`

Service configuration to expose the Spring Petclinic application.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: spring-petclinic-service
spec:
  selector:
    app: spring-petclinic
  type: LoadBalancer  
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 30000
```

### 7. `kubernetes/deployments/mysql-deployment.yml`

Deployment configuration for MySQL.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql-deployment
  labels:
    app: mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql
        ports:
        - containerPort: 3306
        env:
        - name: MYSQL_USER
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: mysql-username
        - name: MYSQL_PASSWORD
          valueFrom: 
            secretKeyRef:
              name: mysql-secret
              key: mysql-password
        - name: MYSQL_ROOT_PASSWORD
          valueFrom: 
            secretKeyRef:
              name: mysql-secret
              key: mysql-root-password
        - name: MYSQL_DATABASE
          value: petclinic
        volumeMounts:
        - name: spring-mysql-volume
          mountPath: /var/lib/mysql
      volumes:
        - name: spring-mysql-volume
          persistentVolumeClaim:
            claimName: spring-mysql-pvc
```

### 8. `kubernetes/deployments/app-deployment.yml`

Deployment configuration for the Spring Petclinic application.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-petclinic
  labels:
    app: spring-petclinic
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-petclinic
  template:
    metadata:
      labels:
        app: spring-petclinic
    spec:
      containers:
      - name: spring-petclinic
        image: mostafaamansour/spring-petclinic-mysql
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: mysql
        - name: MYSQL_URL
          valueFrom: 
            configMapKeyRef:
              name: mysql-configmap
              key: database_url
```

## Deployment Instructions

1. **Apply Configurations:**
   Apply the Kubernetes resource files using the following commands:

   ```bash
   kubectl apply -f kubernetes/configmaps/mysql-configmap.yml
   kubectl apply -f kubernetes/secrets/mysql-secret.yml
   kubectl apply -f kubernetes/volumes/mysql-pv.yml
   kubectl apply -f kubernetes/volumes/mysql-pvc.yml
   kubectl apply -f kubernetes/services/mysql-service.yml
   kubectl apply -f kubernetes/deployments/mysql-deployment.yml
   kubectl apply -f kubernetes/services/app-service.yml
   kubectl apply -f kubernetes/deployments/app-deployment.yml
   ```

2. **Verify Deployment:**
   Check the status of the deployments and services:

   ```bash
   kubectl get deployments
   kubectl get services
   kubectl get pods
   ```

3. **Access the Application:**
   Use the external IP or NodePort to access the Spring Petclinic application from your browser.

## Conclusion

This setup deploys the Spring Petclinic application with a MySQL database on Kubernetes, including all necessary configurations for services, deployments, persistent storage, and secrets. For further customization and management, refer to the Kubernetes documentation on [Managing Resources](https://kubernetes.io/docs/concepts/overview/working-with-objects/) and [Persistent Volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/).
