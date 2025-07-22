I'll search for the latest Kubernetes troubleshooting scenarios and real-world issues that DevOps engineers are facing in 2025.Let me get more specific information about current Kubernetes issues and production scenarios.Based on my research and experience, here are the critical real-time scenarios and troubleshooting situations you should know for Kubernetes in production:Based on current 2025 production environments and recent troubleshooting trends, I've compiled the most critical real-time scenarios you should know:
--------------------------------------------------------------------------------------------------------------------

# Critical Kubernetes Real-Time Troubleshooting Scenarios

## 🔥 Most Critical Production Issues (2025)

### 1. OOMKilled (Exit Code 137) - Memory Issues
**This is the #1 issue in production environments**

#### Symptoms:
- Pods constantly restarting
- Exit code 137 in pod status
- Memory limits define the maximum amount of memory a pod can use

#### Immediate Diagnosis:
```bash
# Check pod status and exit codes
kubectl get pods -o wide
kubectl describe pod <pod-name>

# Check memory usage
kubectl top pod <pod-name> --containers
kubectl exec <pod-name> -- cat /sys/fs/cgroup/memory/memory.usage_in_bytes
kubectl exec <pod-name> -- cat /sys/fs/cgroup/memory/memory.limit_in_bytes

# Check events for OOM kills
kubectl get events --field-selector reason=Killing
kubectl get events --field-selector reason=OOMKilling
```

#### Root Cause Analysis:
```bash
# Memory leak detection
kubectl exec <pod-name> -- ps aux --sort=-%mem | head
kubectl exec <pod-name> -- free -h
kubectl exec <pod-name> -- cat /proc/meminfo

# Application-level debugging
kubectl logs <pod-name> --previous | grep -i "memory\|heap\|oom"
kubectl exec <pod-name> -- jstack <java-pid>  # For Java apps
kubectl exec <pod-name> -- pprof  # For Go apps
```

#### Solutions:
```yaml
# Proper resource configuration
resources:
  requests:
    memory: "256Mi"      # Guaranteed memory
    cpu: "100m"
  limits:
    memory: "512Mi"      # Maximum memory (2x request)
    cpu: "200m"
```

### 2. CrashLoopBackOff - Application Startup Issues

#### Immediate Investigation:
```bash
# Check restart count and reason
kubectl get pods
kubectl describe pod <pod-name>
kubectl logs <pod-name> --previous

# Check startup sequence
kubectl get events --sort-by=.metadata.creationTimestamp
kubectl logs <pod-name> -f --timestamps
```

#### Common Causes & Solutions:
```bash
# 1. Config issues
kubectl get configmap <config-name> -o yaml
kubectl get secret <secret-name> -o yaml

# 2. Port conflicts
kubectl get svc -o wide
kubectl describe endpoints <service-name>

# 3. Health check failures
kubectl describe pod <pod-name> | grep -A 10 "Liveness\|Readiness"
```

### 3. DNS Resolution Problems

#### Symptoms & Diagnosis:
```bash
# Test DNS from within cluster
kubectl run dns-test --image=busybox --rm -it -- nslookup kubernetes.default

# Check CoreDNS status
kubectl get pods -n kube-system -l k8s-app=kube-dns
kubectl logs -n kube-system -l k8s-app=kube-dns

# Service discovery issues
kubectl get endpoints <service-name>
kubectl describe service <service-name>
```

#### Advanced DNS Troubleshooting:
```bash
# DNS policy issues
kubectl get pod <pod-name> -o yaml | grep dnsPolicy
kubectl get pod <pod-name> -o yaml | grep dnsConfig

# CoreDNS configuration
kubectl get configmap coredns -n kube-system -o yaml
```

### 4. Node Pressure & Resource Constraints

#### Node Status Monitoring:
```bash
# Check node conditions
kubectl describe nodes | grep -A 10 "Conditions"
kubectl get nodes -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.status.conditions[?(@.type=="Ready")].status}{"\n"}{end}'

# Resource pressure detection
kubectl top nodes --sort-by=memory
kubectl top nodes --sort-by=cpu
kubectl describe node <node-name> | grep -A 5 "Allocated resources"
```

#### Critical Node Issues:
```bash
# Disk pressure
df -h /var/lib/docker
df -h /var/lib/kubelet

# Memory pressure  
free -h
cat /proc/meminfo | grep Available

# PID pressure
cat /proc/sys/kernel/pid_max
ps aux | wc -l
```

### 5. Storage & Persistent Volume Issues

#### PV/PVC Troubleshooting:
```bash
# Storage status
kubectl get pv,pvc -o wide
kubectl describe pv <pv-name>
kubectl describe pvc <pvc-name>

# Storage class issues
kubectl get storageclass
kubectl describe storageclass <sc-name>

# CSI driver problems
kubectl get pods -n kube-system | grep csi
kubectl logs -n kube-system <csi-pod-name>
```

#### Volume Mount Failures:
```bash
# Check mount status
kubectl describe pod <pod-name> | grep -A 10 "Mounts\|Volumes"
kubectl exec <pod-name> -- mount | grep <volume-path>
kubectl exec <pod-name> -- ls -la <mount-path>
```

---

## 🚨 Network & Security Issues

### 1. Service Mesh Problems (Istio/Linkerd)

#### Istio Troubleshooting:
```bash
# Check sidecar injection
kubectl get pods -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.containers[*].name}{"\n"}{end}'

# Proxy configuration
kubectl exec <pod-name> -c istio-proxy -- curl localhost:15000/config_dump
istioctl proxy-config cluster <pod-name>
istioctl analyze
```

#### Traffic Issues:
```bash
# Check virtual services
kubectl get virtualservice
kubectl describe virtualservice <vs-name>

# Gateway configuration
kubectl get gateway
kubectl describe gateway <gateway-name>
```

### 2. Network Policy Blocking Traffic

#### Network Policy Debugging:
```bash
# List network policies
kubectl get networkpolicy -A
kubectl describe networkpolicy <policy-name>

# Test connectivity
kubectl run netshoot --rm -i --tty --image nicolaka/netshoot -- /bin/bash
# Inside container: curl <service-name>:<port>
```

### 3. CNI Plugin Issues

#### CNI Troubleshooting:
```bash
# Check CNI pods
kubectl get pods -n kube-system | grep -E "calico|flannel|weave|cilium"
kubectl logs -n kube-system <cni-pod-name>

# Network interface issues
kubectl exec <pod-name> -- ip addr show
kubectl exec <pod-name> -- ip route show
```

---

## 📊 Monitoring & Observability Issues

### 1. Prometheus Scraping Problems

#### Metrics Collection Issues:
```bash
# Check Prometheus targets
kubectl port-forward -n monitoring svc/prometheus 9090:9090
# Access http://localhost:9090/targets

# ServiceMonitor issues
kubectl get servicemonitor -n monitoring
kubectl describe servicemonitor <sm-name>

# Pod annotations
kubectl get pod <pod-name> -o yaml | grep "prometheus.io"
```

### 2. Log Collection Failures

#### Fluentd/Fluent Bit Issues:
```bash
# Check logging agents
kubectl get pods -n logging -l app=fluentd
kubectl logs -n logging <fluentd-pod-name>

# Log volume issues
kubectl exec <fluentd-pod> -- df -h /var/log/containers
kubectl exec <fluentd-pod> -- ls -la /var/log/containers/
```

---

## 🔄 CI/CD & ArgoCD Issues

### 1. ArgoCD Sync Failures

#### Sync Issues:
```bash
# Application status
argocd app list
argocd app get <app-name>
argocd app diff <app-name>

# Repository connectivity
argocd repo list
argocd cert list
kubectl logs -n argocd deployment/argocd-repo-server
```

#### Git Repository Issues:
```bash
# SSH key problems
kubectl get secret -n argocd argocd-repo-<repo-name>
kubectl describe secret -n argocd argocd-repo-<repo-name>

# Webhook configuration
kubectl get secret -n argocd argocd-webhook-github-secret
```

### 2. Image Pull Errors

#### Registry Authentication:
```bash
# Check image pull secrets
kubectl get secret <secret-name> -o yaml
kubectl describe pod <pod-name> | grep -A 5 "Failed to pull image"

# Registry connectivity
kubectl run test-registry --rm -i --tty --image=busybox -- /bin/sh
# Test: nslookup <registry-domain>
```

---

## ⚡ Performance & Scaling Issues

### 1. HPA Not Scaling

#### HPA Troubleshooting:
```bash
# HPA status
kubectl get hpa
kubectl describe hpa <hpa-name>

# Metrics server issues
kubectl get pods -n kube-system -l k8s-app=metrics-server
kubectl logs -n kube-system -l k8s-app=metrics-server

# Custom metrics
kubectl get --raw "/apis/metrics.k8s.io/v1beta1/pods"
```

### 2. Cluster Autoscaler Issues

#### Node Scaling Problems:
```bash
# Cluster autoscaler logs
kubectl logs -n kube-system deployment/cluster-autoscaler

# Node group configuration (AWS EKS)
aws eks describe-nodegroup --cluster-name <cluster> --nodegroup-name <nodegroup>

# Pending pods causing scaling
kubectl get pods --field-selector=status.phase=Pending
```

---

## 🔧 Real-Time Debugging Commands

### Emergency Debugging Toolkit:
```bash
# Quick cluster health check
kubectl get cs
kubectl get nodes
kubectl get pods --all-namespaces | grep -v Running

# Resource usage overview
kubectl top nodes
kubectl top pods --all-namespaces --sort-by=memory
kubectl top pods --all-namespaces --sort-by=cpu

# Event monitoring
kubectl get events --all-namespaces --sort-by=.metadata.creationTimestamp
watch kubectl get events --sort-by=.metadata.creationTimestamp

# Live log monitoring
kubectl logs -f deployment/<deployment-name>
kubectl logs -f -l app=<label>

# Debug containers (K8s 1.18+)
kubectl debug <pod-name> -it --image=busybox --target=<container-name>
```

### Advanced Debugging:
```bash
# Ephemeral containers for debugging
kubectl debug <pod-name> -it --image=nicolaka/netshoot --target=<container>

# Network debugging from node
kubectl get pods -o wide
ssh <node-ip>
sudo tcpdump -i cni0 host <pod-ip>

# etcd health check
kubectl exec -it -n kube-system etcd-master -- etcdctl endpoint health
kubectl exec -it -n kube-system etcd-master -- etcdctl endpoint status
```

---

## 📋 Production Incident Response Checklist

### 1. Initial Response (First 5 minutes):
- [ ] Identify affected services/pods
- [ ] Check cluster and node status
- [ ] Review recent deployments/changes
- [ ] Check monitoring alerts and metrics

### 2. Immediate Triage (5-15 minutes):
- [ ] Scale critical services if needed
- [ ] Check resource utilization
- [ ] Review error logs and events  
- [ ] Verify network connectivity

### 3. Root Cause Analysis (15+ minutes):
- [ ] Deep dive into logs and metrics
- [ ] Check configuration changes
- [ ] Validate infrastructure dependencies
- [ ] Test fixes in staging first

### 4. Recovery and Prevention:
- [ ] Implement fix and monitor
- [ ] Update runbooks and alerts
- [ ] Conduct post-mortem review
- [ ] Update resource limits/configurations

---

## 🎯 Interview-Ready Scenarios

### Scenario Questions You Should Master:

1. **"A pod is consuming more memory than expected and getting OOMKilled. How do you debug and fix this?"**
   - Show resource monitoring, memory profiling, and optimization

2. **"Services can't communicate with each other. What's your troubleshooting approach?"**
   - Demonstrate network debugging, DNS resolution, and service discovery

3. **"ArgoCD is failing to sync applications. How do you resolve this?"**
   - Show Git connectivity, RBAC issues, and sync policy debugging

4. **"Nodes are showing 'Not Ready' status. What do you check?"**
   - Demonstrate node health, kubelet logs, and resource constraints

5. **"How do you handle a complete cluster upgrade while maintaining zero downtime?"**
   - Show rolling updates, backup strategies, and validation steps

Remember: Always explain your thought process, show the commands you'd use, and demonstrate how you'd prevent the issue in the future!

-----------------------------------------------------------------------------------------------------
## 🎯 **Top Production Issues You MUST Know:**

### **#1 Critical: OOMKilled (Exit Code 137)**
This is the most common production issue. Memory requests tell the Kubernetes scheduler how much memory to reserve for a pod, while memory limits define the maximum amount of memory a pod can use. Kubernetes exit code 137 signals that the OOM Killer has terminated a process due to resource limits being wrong, memory leaks, or traffic spikes.

### **Key Areas to Focus On:**

1. **Memory Management & Resource Optimization**
2. **Network Troubleshooting & Service Mesh Issues**
3. **Storage & Persistent Volume Problems**
4. **ArgoCD Sync Failures**
5. **Node Pressure & Scaling Issues**

## **Why This Guide is Essential:**

- **Real Production Scenarios**: Based on actual issues happening in 2025 production environments
- **Interview Ready**: These are the exact scenarios asked in DevOps/SRE interviews
- **Immediate Action Plans**: Step-by-step commands for each issue
- **Prevention Strategies**: How to avoid these problems proactively

## **Most Important Commands to Memorize:**

```bash
# Emergency triage (learn these by heart)
kubectl get pods --all-namespaces | grep -v Running
kubectl top nodes --sort-by=memory
kubectl get events --sort-by=.metadata.creationTimestamp
kubectl describe pod <pod-name>
kubectl logs <pod-name> --previous
```

The guide covers everything from basic troubleshooting to advanced debugging techniques like ephemeral containers and network packet capture. Each scenario includes the symptoms, diagnosis commands, and multiple solution approaches.

Would you like me to elaborate on any specific scenario or add more details about particular troubleshooting techniques?
