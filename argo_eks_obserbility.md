I'll help you cover those important Kubernetes topics you mentioned. Let me create a comprehensive guide that includes ArgoCD deployments, observability stack, troubleshooting scenarios, and cluster upgrades.I've created a comprehensive Kubernetes guide that covers all the important aspects you mentioned:

------------------------------------------------------------------------------------------------------------------------------
# Complete Kubernetes Guide: ArgoCD, Observability, Troubleshooting & Upgrades

## Table of Contents
1. [ArgoCD Deployment & Configuration](#argocd-deployment--configuration)
2. [Observability Stack (Prometheus, Grafana, EFK, Jaeger)](#observability-stack)
3. [Pod Health Checks & Jobs](#pod-health-checks--jobs)
4. [Real-time Troubleshooting Scenarios](#real-time-troubleshooting-scenarios)
5. [Cluster Upgrades (1.29 → 1.30 → 1.31)](#cluster-upgrades)
6. [Complete YAML Configurations](#complete-yaml-configurations)

---

## ArgoCD Deployment & Configuration

### ArgoCD Installation
```yaml
# argocd-namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: argocd
```

```yaml
# argocd-application.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: sample-app
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/your-org/your-app
    targetRevision: main
    path: k8s/overlays/production
  destination:
    server: https://kubernetes.default.svc
    namespace: production
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
```

### ArgoCD Troubleshooting Commands
```bash
# Check ArgoCD status
kubectl get pods -n argocd
kubectl logs -n argocd deployment/argocd-server
kubectl logs -n argocd deployment/argocd-application-controller

# Sync application manually
argocd app sync sample-app
argocd app get sample-app
argocd app diff sample-app
```

---

## Observability Stack

### Prometheus Configuration
```yaml
# prometheus-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s
    
    rule_files:
      - "*.rules"
    
    scrape_configs:
      - job_name: 'kubernetes-pods'
        kubernetes_sd_configs:
        - role: pod
        relabel_configs:
        - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
          action: keep
          regex: true
        - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
          action: replace
          target_label: __metrics_path__
          regex: (.+)
```

### Grafana Dashboard Configuration
```yaml
# grafana-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: monitoring
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
      - name: grafana
        image: grafana/grafana:latest
        ports:
        - containerPort: 3000
        env:
        - name: GF_SECURITY_ADMIN_PASSWORD
          valueFrom:
            secretKeyRef:
              name: grafana-secret
              key: admin-password
        volumeMounts:
        - name: grafana-storage
          mountPath: /var/lib/grafana
        - name: grafana-datasources
          mountPath: /etc/grafana/provisioning/datasources
        readinessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 30
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 60
          periodSeconds: 30
      volumes:
      - name: grafana-storage
        persistentVolumeClaim:
          claimName: grafana-pvc
      - name: grafana-datasources
        configMap:
          name: grafana-datasources
```

### EFK Stack (Elasticsearch, Fluentd, Kibana)
```yaml
# elasticsearch.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: elasticsearch
  namespace: logging
spec:
  serviceName: elasticsearch
  replicas: 3
  selector:
    matchLabels:
      app: elasticsearch
  template:
    metadata:
      labels:
        app: elasticsearch
    spec:
      containers:
      - name: elasticsearch
        image: elasticsearch:7.17.0
        env:
        - name: discovery.type
          value: single-node
        - name: ES_JAVA_OPTS
          value: "-Xms512m -Xmx512m"
        ports:
        - containerPort: 9200
        - containerPort: 9300
        readinessProbe:
          httpGet:
            path: /_cluster/health
            port: 9200
          initialDelaySeconds: 30
        livenessProbe:
          httpGet:
            path: /_cluster/health
            port: 9200
          initialDelaySeconds: 60
        volumeMounts:
        - name: elasticsearch-data
          mountPath: /usr/share/elasticsearch/data
  volumeClaimTemplates:
  - metadata:
      name: elasticsearch-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 10Gi
```

### Jaeger Tracing
```yaml
# jaeger-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jaeger
  namespace: observability
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jaeger
  template:
    metadata:
      labels:
        app: jaeger
    spec:
      containers:
      - name: jaeger
        image: jaegertracing/all-in-one:latest
        env:
        - name: COLLECTOR_ZIPKIN_HTTP_PORT
          value: "9411"
        ports:
        - containerPort: 16686  # UI
        - containerPort: 14268  # HTTP
        - containerPort: 14250  # gRPC
        readinessProbe:
          httpGet:
            path: /
            port: 16686
          initialDelaySeconds: 30
        livenessProbe:
          httpGet:
            path: /
            port: 16686
          initialDelaySeconds: 60
```

---

## Pod Health Checks & Jobs

### Readiness and Liveness Probes
```yaml
# app-with-probes.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: web-app
  template:
    metadata:
      labels:
        app: web-app
    spec:
      containers:
      - name: web-app
        image: nginx:1.21
        ports:
        - containerPort: 80
        # Readiness Probe - determines if pod is ready to receive traffic
        readinessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 5
          timeoutSeconds: 3
          successThreshold: 1
          failureThreshold: 3
        # Liveness Probe - determines if pod should be restarted
        livenessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        # Startup Probe - for slow starting containers
        startupProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 30
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
```

### Jobs and CronJobs
```yaml
# backup-job.yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: database-backup
spec:
  template:
    spec:
      containers:
      - name: backup
        image: postgres:13
        command: ["sh", "-c"]
        args:
        - |
          pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME > /backup/backup-$(date +%Y%m%d-%H%M%S).sql
        env:
        - name: DB_HOST
          value: "postgres-service"
        - name: DB_USER
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: PGPASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        volumeMounts:
        - name: backup-volume
          mountPath: /backup
      volumes:
      - name: backup-volume
        persistentVolumeClaim:
          claimName: backup-pvc
      restartPolicy: OnFailure
  backoffLimit: 3
```

```yaml
# cronjob.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: cleanup-job
spec:
  schedule: "0 2 * * *"  # Every day at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: cleanup
            image: alpine:latest
            command: ["sh", "-c"]
            args:
            - |
              echo "Cleaning up old files..."
              find /data -type f -mtime +7 -delete
              echo "Cleanup completed"
            volumeMounts:
            - name: data-volume
              mountPath: /data
          volumes:
          - name: data-volume
            persistentVolumeClaim:
              claimName: data-pvc
          restartPolicy: OnFailure
  successfulJobsHistoryLimit: 3
  failedJobsHistoryLimit: 1
```

---

## Real-time Troubleshooting Scenarios

### Scenario 1: Pod Stuck in Pending State
```bash
# Diagnosis commands
kubectl describe pod <pod-name>
kubectl get events --sort-by=.metadata.creationTimestamp
kubectl get nodes -o wide
kubectl describe nodes

# Common causes and solutions:
# 1. Insufficient resources
kubectl top nodes
kubectl top pods

# 2. Node selector issues
kubectl get nodes --show-labels
```

### Scenario 2: Pod CrashLoopBackOff
```bash
# Investigation steps
kubectl logs <pod-name> --previous
kubectl describe pod <pod-name>
kubectl get pod <pod-name> -o yaml

# Check resource limits
kubectl describe pod <pod-name> | grep -A 5 "Limits\|Requests"

# Fix example - update resource limits
kubectl patch deployment <deployment-name> -p '{"spec":{"template":{"spec":{"containers":[{"name":"<container-name>","resources":{"limits":{"memory":"512Mi"},"requests":{"memory":"256Mi"}}}]}}}}'
```

### Scenario 3: Service Not Accessible
```bash
# Check service and endpoints
kubectl get svc
kubectl get endpoints
kubectl describe svc <service-name>

# Test connectivity
kubectl run test-pod --image=busybox --rm -it -- sh
# Inside pod: wget -qO- http://<service-name>:<port>

# Check network policies
kubectl get networkpolicies
kubectl describe networkpolicy <policy-name>
```

### Scenario 4: ArgoCD Sync Issues
```bash
# Check ArgoCD application status
kubectl get applications -n argocd
kubectl describe application <app-name> -n argocd

# Manual sync
argocd app sync <app-name>
argocd app diff <app-name>

# Check ArgoCD controller logs
kubectl logs -n argocd deployment/argocd-application-controller -f
```

### Scenario 5: Monitoring Stack Issues
```bash
# Prometheus not scraping metrics
kubectl logs -n monitoring prometheus-0
kubectl get servicemonitor -n monitoring
kubectl get endpoints -n monitoring

# Grafana dashboard not loading
kubectl logs -n monitoring deployment/grafana
kubectl get configmap grafana-datasources -n monitoring -o yaml

# EFK logs not appearing
kubectl logs -n logging daemonset/fluentd
kubectl get pods -n logging
```

---

## Cluster Upgrades

### Pre-Upgrade Checklist
```bash
# 1. Backup etcd
kubectl get nodes -o wide
kubectl version --short

# Create backup
etcdctl snapshot save backup.db \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key

# 2. Check cluster health
kubectl get cs
kubectl get nodes
kubectl get pods --all-namespaces | grep -v Running | grep -v Completed

# 3. Check deprecated APIs
kubectl api-resources --api-group=extensions
```

### Upgrade from 1.29 to 1.30
```bash
# 1. Upgrade control plane
sudo apt update
sudo apt-cache madison kubeadm | head -5

# Install specific version
sudo apt-mark unhold kubeadm
sudo apt install kubeadm=1.30.0-00
sudo apt-mark hold kubeadm

# Plan upgrade
sudo kubeadm upgrade plan

# Apply upgrade
sudo kubeadm upgrade apply v1.30.0

# 2. Upgrade kubelet and kubectl on master
sudo apt-mark unhold kubelet kubectl
sudo apt install kubelet=1.30.0-00 kubectl=1.30.0-00
sudo apt-mark hold kubelet kubectl
sudo systemctl daemon-reload
sudo systemctl restart kubelet

# 3. Upgrade worker nodes (run on each worker)
kubectl drain <worker-node> --ignore-daemonsets --delete-emptydir-data
sudo apt-mark unhold kubeadm kubelet kubectl
sudo apt install kubeadm=1.30.0-00 kubelet=1.30.0-00 kubectl=1.30.0-00
sudo apt-mark hold kubeadm kubelet kubectl
sudo kubeadm upgrade node
sudo systemctl daemon-reload
sudo systemctl restart kubelet
kubectl uncordon <worker-node>
```

### Upgrade from 1.30 to 1.31
```bash
# Similar process with version 1.31.0-00
sudo kubeadm upgrade plan
sudo kubeadm upgrade apply v1.31.0

# Verify upgrade
kubectl version --short
kubectl get nodes
```

### Post-Upgrade Verification
```bash
# 1. Check cluster status
kubectl cluster-info
kubectl get nodes -o wide
kubectl get pods --all-namespaces

# 2. Verify system components
kubectl get cs
kubectl get pods -n kube-system

# 3. Test applications
kubectl get deployments --all-namespaces
kubectl get services --all-namespaces

# 4. Check ArgoCD after upgrade
kubectl get pods -n argocd
argocd version

# 5. Verify monitoring stack
kubectl get pods -n monitoring
kubectl get pods -n logging
kubectl get pods -n observability
```

### Troubleshooting Upgrade Issues

#### Issue 1: Nodes Not Ready After Upgrade
```bash
# Check node status
kubectl describe node <node-name>
sudo systemctl status kubelet
sudo journalctl -u kubelet -f

# Common fix
sudo systemctl daemon-reload
sudo systemctl restart kubelet
```

#### Issue 2: Pods Stuck in Terminating
```bash
# Force delete
kubectl delete pod <pod-name> --force --grace-period=0

# Check for finalizers
kubectl get pod <pod-name> -o yaml | grep finalizers
kubectl patch pod <pod-name> -p '{"metadata":{"finalizers":null}}'
```

#### Issue 3: API Server Issues
```bash
# Check API server logs
sudo journalctl -u kubelet | grep apiserver
kubectl logs -n kube-system kube-apiserver-<master-node>

# Restart API server (if using static pods)
sudo mv /etc/kubernetes/manifests/kube-apiserver.yaml /tmp/
sleep 10
sudo mv /tmp/kube-apiserver.yaml /etc/kubernetes/manifests/
```

---

## Complete YAML Configurations

### Comprehensive Application with All Features
```yaml
# complete-app.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: production
---
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: production
type: Opaque
data:
  db-password: cGFzc3dvcmQxMjM=  # password123 base64 encoded
  api-key: YWJjZGVmZ2hpams=      # abcdefghijk base64 encoded
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: production
data:
  app.properties: |
    server.port=8080
    logging.level.root=INFO
    spring.datasource.url=jdbc:postgresql://postgres:5432/mydb
  nginx.conf: |
    server {
        listen 80;
        location / {
            proxy_pass http://app-service:8080;
            proxy_set_header Host $host;
        }
    }
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web-application
  namespace: production
  labels:
    app: web-app
    version: v1.0
  annotations:
    deployment.kubernetes.io/revision: "1"
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: web-app
  template:
    metadata:
      labels:
        app: web-app
        version: v1.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/metrics"
    spec:
      serviceAccountName: app-service-account
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 2000
      containers:
      - name: app
        image: myapp:v1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: db-password
        - name: API_KEY
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: api-key
        - name: JAVA_OPTS
          value: "-Xms512m -Xmx1024m"
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 30
      volumes:
      - name: config-volume
        configMap:
          name: app-config
      - name: logs-volume
        emptyDir: {}
      nodeSelector:
        kubernetes.io/os: linux
      tolerations:
      - key: "node-role.kubernetes.io/master"
        operator: "Exists"
        effect: "NoSchedule"
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - web-app
              topologyKey: kubernetes.io/hostname
---
apiVersion: v1
kind: Service
metadata:
  name: app-service
  namespace: production
  labels:
    app: web-app
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
spec:
  selector:
    app: web-app
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  namespace: production
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - app.example.com
    secretName: app-tls
  rules:
  - host: app.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: app-service
            port:
              number: 80
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: app-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: web-application
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 60
```

## Quick Reference Commands

### Essential Troubleshooting Commands
```bash
# Pod debugging
kubectl get pods -o wide
kubectl describe pod <pod-name>
kubectl logs <pod-name> -f --previous
kubectl exec -it <pod-name> -- sh

# Cluster health
kubectl get cs
kubectl cluster-info
kubectl get events --sort-by=.metadata.creationTimestamp
kubectl top nodes
kubectl top pods

# ArgoCD
argocd app list
argocd app get <app-name>
argocd app sync <app-name>
argocd app diff <app-name>

# Monitoring
kubectl port-forward -n monitoring svc/prometheus 9090:9090
kubectl port-forward -n monitoring svc/grafana 3000:3000
kubectl port-forward -n logging svc/kibana 5601:5601
kubectl port-forward -n observability svc/jaeger 16686:16686
```

This comprehensive guide covers all the aspects you mentioned, including real-world troubleshooting scenarios and complete configurations for production environments.
--------------------------------------------------------------------------------------


## Key Features Added:
1. **ArgoCD Deployment & Configuration** - Complete setup with troubleshooting commands
2. **Full Observability Stack** - Prometheus, Grafana, EFK (Elasticsearch, Fluentd, Kibana), and Jaeger configurations
3. **Pod Health Checks** - Detailed readiness, liveness, and startup probes
4. **Jobs & CronJobs** - Complete examples for backup jobs and scheduled tasks
5. **Real-time Troubleshooting Scenarios** - Common issues like CrashLoopBackOff, Pending pods, service connectivity
6. **Cluster Upgrades** - Step-by-step process for upgrading from 1.29 → 1.30 → 1.31
7. **Complete YAML Configurations** - Production-ready manifests with all best practices

## Missing Components Now Included:
- **Readiness & Liveness Probes** - Proper health check configurations
- **Jobs & CronJobs** - Database backups and cleanup tasks
- **Startup Probes** - For slow-starting applications
- **Upgrade troubleshooting** - Common issues and solutions during cluster upgrades
- **Post-upgrade verification** - Steps to ensure everything works after upgrade

The guide includes real-world scenarios you'd encounter in production, with specific commands for diagnosis and resolution. Each section has practical examples you can use directly in your environment.

Would you like me to expand on any specific section or add additional troubleshooting scenarios?
