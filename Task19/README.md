# Jira Deployment on Kubernetes

This README provides instructions for deploying the Jira application with a MySQL database using Kubernetes. The deployment includes configurations for PersistentVolumes and PersistentVolumeClaims, ConfigMaps, Secrets, Deployments, and Services.

## Directory Structure

The Kubernetes manifests are organized into the following files:

```
kubernetes/
├── configmaps/
│   └── mysql-configmap.yml
├── deployments/
│   ├── app-deployment.yml
│   └── mysql-deployment.yml
├── secrets/
│   └── mysql-secret.yml
├── services/
│   ├── app-service.yml
│   └── mysql-service.yml
├── volumes/
│   ├── jira-pv.yml
│   ├── jira-pvc.yml
│   ├── mysql-pv.yml
│   └── mysql-pvc.yml
```

## Files and Their Purpose

### 1. `mysql-configmap.yml`

Defines a ConfigMap for storing the database URL and name used by Jira.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mysql-configmap
data:
  database_url: jdbc:mysql://mysql-service:3306/jira
  database_name: jira
```

### 2. `mysql-secret.yml`

Defines a Secret for storing sensitive MySQL credentials.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysql-secret
type: Opaque
data:
  mysql-username: amlyYQ==
  mysql-password: amlyYQ==
  mysql-root-password: cm9vdA==
```

### 3. `app-deployment.yml`

Deployment configuration for the Jira application.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jira
  labels:
    app: jira
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jira
  template:
    metadata:
      labels:
        app: jira
    spec:
      initContainers:
      - name: wait-for-mysql
        image: busybox
        command: ['sh', '-c', 'until nslookup mysql-service.prod.svc.cluster.local; do echo waiting for mysql; sleep 2; done;']
      containers:
      - name: jira
        image: atlassian/jira-software
        ports:
        - containerPort: 8080
        env:
          - name: ATL_DB_TYPE
            value: "mysql"
          - name: ATL_JDBC_URL
            valueFrom:
              configMapKeyRef:
                name: mysql-configmap
                key: database_url
          - name: ATL_JDBC_USER
            valueFrom:
              secretKeyRef:
                name: mysql-secret
                key: mysql-username
          - name: ATL_JDBC_PASSWORD
            valueFrom:
              secretKeyRef:
                name: mysql-secret
                key: mysql-password
        volumeMounts:
          - name: jira-volume
            mountPath: /var/atlassian/application-data/jira
      volumes:
        - name: jira-volume
          persistentVolumeClaim:
            claimName: jira-pvc
```

### 4. `mysql-deployment.yml`

Deployment configuration for the MySQL database.

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
          valueFrom:
            configMapKeyRef:
              name: mysql-configmap
              key: database_name
        volumeMounts:
        - name: jira-mysql-volume
          mountPath: /var/lib/mysql
      volumes:
        - name: jira-mysql-volume
          persistentVolumeClaim:
            claimName: jira-mysql-pvc
```

### 5. `app-service.yml`

Service configuration for the Jira application, exposed via a LoadBalancer.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: jira-service
spec:
  selector:
    app: jira
  type: LoadBalancer  
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 30003
```

### 6. `mysql-service.yml`

Service configuration for the MySQL database.

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

### 7. `jira-pv.yml`

PersistentVolume configuration for Jira data.

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: jira-pv
spec:
  capacity:
    storage: 1Gi
  volumeMode: Filesystem
  accessModes:
  - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: jira-storage
  local:
    path: /home/docker/volumes/jira
  nodeAffinity:
    required:
      nodeSelectorTerms:
      - matchExpressions:
        - key: kubernetes.io/hostname
          operator: In
          values:
          - minikube
```

### 8. `jira-pvc.yml`

PersistentVolumeClaim to request storage for Jira.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: jira-pvc
  namespace: prod
spec:
  volumeName: jira-pv
  volumeMode: Filesystem
  storageClassName: jira-storage
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

### 9. `mysql-pv.yml`

PersistentVolume configuration for MySQL data.

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: jira-mysql-pv
spec:
  capacity:
    storage: 1Gi
  volumeMode: Filesystem
  accessModes:
  - ReadWriteMany
  persistentVolumeReclaimPolicy: Retain
  storageClassName: local-storage
  local:
    path: /home/docker/volumes/jira-mysql
  nodeAffinity:
    required:
      nodeSelectorTerms:
      - matchExpressions:
        - key: kubernetes.io/hostname
          operator: In
          values:
          - minikube
```

### 10. `mysql-pvc.yml`

PersistentVolumeClaim to request storage for MySQL.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: jira-mysql-pvc
spec:
  volumeName: jira-mysql-pv
  storageClassName: local-storage
  volumeMode: Filesystem
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 1Gi
```

## Deployment Instructions

1. **Install Kubernetes:**
   Ensure Kubernetes is installed and configured on your local machine or cluster.

2. **Apply the Manifests:**
   Apply the manifests in the following order:
   ```bash
   kubectl apply -R -f kubernetes/
   ```
   or
   ```bash
   kubectl apply -f kubernetes/configmaps/mysql-configmap.yml
   kubectl apply -f kubernetes/secrets/mysql-secret.yml
   kubectl apply -f kubernetes/volumes/jira-pv.yml
   kubectl apply -f kubernetes/volumes/jira-pvc.yml
   kubectl apply -f kubernetes/volumes/mysql-pv.yml
   kubectl apply -f kubernetes/volumes/mysql-pvc.yml
   kubectl apply -f kubernetes/deployments/mysql-deployment.yml
   kubectl apply -f kubernetes/deployments/app-deployment.yml
   kubectl apply -f kubernetes/services/mysql-service.yml
   kubectl apply -f kubernetes/services/app-service.yml
   ```

3. **Verify Deployment:**
   Check the status of the deployments and services:

   ```bash
   kubectl get deployments
   kubectl get services
   kubectl get pods
   ```

4. **Access the Application:**
   Use the external IP or NodePort from the `jira-service` to access Jira from your browser.

## Conclusion

This setup provides a complete Kubernetes deployment for Jira with a MySQL database, including necessary persistent storage configurations. For further customization and management, refer to the Kubernetes documentation on [Deployments](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/) and [Services](https://kubernetes.io/docs/concepts/services-networking/service/).
