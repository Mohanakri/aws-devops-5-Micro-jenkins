# 🚀 My-AWS_DevOps_5-Micro-Service

This repository demonstrates a complete DevOps pipeline for **5 microservices** built in different programming languages, using **Jenkins**, **ArgoCD**, **Docker**, and deployed to **AWS EKS**.

Each microservice is independently containerized and can be built, tested, and deployed using Jenkins pipelines with GitOps deployment via ArgoCD.

---



# Repository 1: Source Code Repository

## 📁 Directory Structure

```plaintext
My-aws_devops_5-Micro-service/
├── Jenkinsfile
├── docker-compose.yml
└── src/
    ├── java-maven-service/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/
    │       └── main/
    │           └── java/
    │               └── com/
    │                   └── example/
    │                       └── App.java
    │
    ├── java-gradle-service/
    │   ├── Dockerfile
    │   ├── build.gradle
    │   └── src/
    │       └── main/
    │           └── java/
    │               └── com/
    │                   └── example/
    │                       └── App.java
    │
    ├── python-service/
    │   ├── Dockerfile
    │   ├── requirements.txt
    │   └── app.py
    │
    ├── go-service/
    │   ├── Dockerfile
    │   ├── go.mod
    │   └── main.go
    │
    └── javascript-service/
        ├── Dockerfile
        ├── package.json
        └── app.js

---

## 🔧 Tech Stack

| Layer            | Tools Used                                      |
|------------------|-------------------------------------------------|
| CI/CD            | Jenkins, ArgoCD                                 |
| Containerization | Docker                                          |
| Orchestration    | AWS EKS (Elastic Kubernetes Service)            |
| IaC              | Terraform (optional for infra provisioning)     |
| Languages        | Java (Maven/Gradle), Python, Go, JavaScript     |

---

## 📦 Microservices Overview

| Service              | Language | Build Tool | Description                     |
|----------------------|----------|------------|---------------------------------|
| `java-maven-service` | Java     | Maven      | A sample Java app using Maven   |
| `java-gradle-service`| Java     | Gradle     | A sample Java app using Gradle  |
| `python-service`     | Python   | pip        | A sample Flask/FastAPI app      |
| `go-service`         | Go       | native     | A simple Go web service         |
| `javascript-service` | Node.js  | npm        | A simple Node.js Express app    |



