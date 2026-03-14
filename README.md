# Transaction Aggregation API

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen?style=for-the-badge&logo=spring)
![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![JWT](https://img.shields.io/badge/JWT-Security-blue?style=for-the-badge&logo=json-web-tokens)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=for-the-badge&logo=docker)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Minikube-326CE5?style=for-the-badge&logo=kubernetes)
![H2](https://img.shields.io/badge/H2-Database-yellow?style=for-the-badge&logo=h2)
![Tests](https://img.shields.io/badge/Tests-Passing-success?style=for-the-badge&logo=github-actions)

**A production-ready REST API demonstrating modern Java backend development with Spring Boot, Docker, and Kubernetes**

</div>

---

## Table of Contents
- [Project Overview](#project-overview)
- [Why This Project?](#why-this-project)
- [Technology Choices](#technology-choices)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Docker Deployment](#docker-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [API Documentation](#api-documentation)
- [Testing Strategy](#testing-strategy)
- [Project Structure](#project-structure)
- [Environment Variables](#environment-variables)
- [Contributing](#contributing)
- [License](#license)

---

## Project Overview

The **Transaction Aggregation API** is a sophisticated backend service designed to aggregate customer financial transaction data from multiple sources. It provides comprehensive APIs for transaction management, categorization, and advanced analytics. This project serves as a demonstration of modern Java backend development practices, containerization, and cloud-native deployment strategies.

### Business Value

| Benefit | Description |
|---------|-------------|
| **Financial Data Aggregation** | Centralizes transaction data from multiple sources |
| **Real-time Analytics** | Provides instant categorization and aggregation |
| **Scalable Architecture** | Designed to handle growing transaction volumes |
| **Secure by Design** | Implements JWT authentication and role-based access |

---

## Why This Project?

This project was created to demonstrate proficiency in:

| Area | Demonstration |
|------|--------------|
| **Java/Spring Boot** | Clean architecture, REST APIs, JPA, Security |
| **Database Design** | Proper relationships, indexing, query optimization |
| **Security** | JWT authentication, password encryption, role-based access |
| **Testing** | Unit tests, integration tests, mocking |
| **Containerization** | Docker multi-stage builds, optimization |
| **Orchestration** | Kubernetes deployment, scaling, self-healing |
| **Documentation** | OpenAPI/Swagger, comprehensive README |
| **DevOps** | CI/CD ready, health checks, monitoring |

---

## Technology Choices

### Why Spring Boot?

| Reason | Benefit |
|--------|---------|
| **Mature Ecosystem** | Extensive libraries for every need |
| **Production-Ready** | Built-in metrics, health checks, externalized config |
| **Active Community** | Regular updates, security patches, support |
| **Developer Productivity** | Auto-configuration, starter dependencies |

### Why Java 21?

-   **LTS Version**: Long-term support for enterprise applications
-    **Virtual Threads**: Improved concurrency for I/O operations
-    **Pattern Matching**: Cleaner, more readable code
-    **Performance**: Ongoing JVM improvements

### Why JWT for Authentication?

-    **Stateless**: No server-side session storage needed
-    **Scalable**: Works seamlessly with horizontal scaling
-    **Decentralized**: Can be validated by any service
-    **Standard**: Industry-standard for API authentication

### Why H2 Database?

-    **Zero Configuration**: Perfect for development and testing
-    **In-Memory Mode**: Fast for unit tests
-    **Compatibility**: Easy to switch to PostgreSQL/MySQL in production
-    **Console**: Built-in web interface for debugging

### Why Docker?

-    **Consistency**: Same environment from dev to production
-    **Isolation**: No dependency conflicts
-    **Reproducibility**: Build once, run anywhere
-    **Microservices Ready**: Foundation for container orchestration

### Why Kubernetes?

-    **Orchestration**: Manages container lifecycle automatically
-    **Self-Healing**: Restarts failed containers automatically
-    **Scaling**: Horizontal pod autoscaling based on load
-    **Rolling Updates**: Zero-downtime deployments
-    **Service Discovery**: Built-in DNS for service communication

### Why Minikube?

-    **Local Development**: Full Kubernetes cluster on your machine
-    **Learning**: Perfect for understanding K8s concepts
-    **Testing**: Validate configurations before cloud deployment
-    **Cost-Effective**: No cloud costs for development

---

##   Key Features

###     Authentication & Authorization

| Feature | Description |
|---------|-------------|
| JWT Authentication | Token-based authentication with refresh tokens |
| Password Encryption | Secure BCrypt password hashing |
| Role-Based Access | Granular permission control |
| User Management | Registration, login, and profile management |

###     Transaction Management

| Feature | Description |
|---------|-------------|
| Create Transactions | Record new financial transactions |
| Multiple Payment Methods | Credit Card, Debit Card, Bank Transfer, etc. |
| Transaction Categorization | Groceries, Dining, Shopping, etc. |
| Status Tracking | Pending, Completed, Failed, etc. |

###     Advanced Aggregation

| Feature | Description |
|---------|-------------|
| Category Analysis | Real-time aggregation by category |
| Payment Method Stats | Breakdown with statistics |
| Date Range Filtering | Custom date-based queries |
| Summary Statistics | Min, max, average, total calculations |

###     Architecture Highlights

| Pattern | Implementation | Benefit |
|---------|---------------|---------|
| **Strategy Pattern** | Pluggable payment processors | Easy to add new payment methods |
| **DTO Pattern** | Clean separation between layers | Prevents over-exposure of data |
| **Global Exception Handling** | `@RestControllerAdvice` | Consistent error responses |
| **Pagination** | Spring Data Pageable | Optimized for large datasets |

###     Kubernetes Native

| Feature | Description |
|---------|-------------|
| Ready-to-deploy Manifests | Complete K8s configuration |
| Horizontal Pod Autoscaling | Auto-scales based on load |
| Health Probes | Liveness and Readiness checks |
| ConfigMap | Externalized configuration |
| Secrets | Secure sensitive data |
| Ingress | External access with routing |

---

##  Architecture

### High-Level Design
┌─────────────────────────────────────────────────────────────┐
│   Client Applications │
└───────────────────────────────┬─────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│   Kubernetes Ingress │
│ (transaction-api.local) │
└───────────────────────────────┬─────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│   Service (ClusterIP) │
│ transaction-api-service │
└───────────────────────────────┬─────────────────────────────┘
│
┌───────────────────┼───────────────────┐
▼ ▼ ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│ 📦 Pod 1 │ │ 📦 Pod 2 │ │ 📦 Pod 3 │
│ (Port 8484) │ │ (Port 8484) │ │ (Port 8484) │
│ Transaction │ │ Transaction │ │ Transaction │
│ API v1 │ │ API v1 │ │ API v1 │
│ 💚 /actuator │ │ 💚 /actuator │ │ 💚 /actuator │
└───────────────┘ └───────────────┘ └───────────────┘

text

### Layer Architecture
┌─────────────────────────────────────────────────────────────┐
│   Presentation Layer │
│ (Controllers, DTOs, Exception Handlers) │
├─────────────────────────────────────────────────────────────┤
│   Business Layer │
│ (Services, Payment Processors, Business Logic) │
├─────────────────────────────────────────────────────────────┤
│   Data Access Layer │
│ (Repositories, Entities, JPA) │
├─────────────────────────────────────────────────────────────┤
│   Infrastructure Layer │
│ (Security, Config, Database, External Services) │
└─────────────────────────────────────────────────────────────┘

text

---

##  Getting Started

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **Java** | 21+ | Core runtime |
| **Maven** | 3.8+ | Build tool |
| **Docker Desktop** | Latest | Containerization |
| **Minikube** | Latest | Local Kubernetes |
| **kubectl** | Latest | K8s CLI |
| **Git** | Latest | Version control |

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/transaction-aggregation-api.git
cd transaction-aggregation-api

# Build the application
mvn clean package

# Run tests
mvn test

# Run locally
java -jar target/transaction-api-0.0.1-SNAPSHOT.jar
Verify Installation
bash
# Health check
curl http://localhost:8484/actuator/health

# Expected response
{
  "status": "UP"
}
     Docker Deployment
Why Docker?
Docker ensures your application runs the same way everywhere - from your laptop to production.

Build and Run
bash
# Build Docker image
docker build -t transaction-api:latest .

# Run container
docker run -p 8484:8484 transaction-api:latest

# Or use docker-compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop containers
docker-compose down
Docker Commands Cheat Sheet
Command	Description
docker build -t transaction-api:latest .	Build image
docker images	List images
docker ps	List running containers
docker logs <container-id>	View container logs
docker exec -it <container-id> sh	Access container shell

     Kubernetes Deployment
Why Kubernetes?
Kubernetes provides automated deployment, scaling, and management of containerized applications.

Install Minikube
<details> <summary><b>Windows</b></summary>
powershell
# Using Chocolatey (run as Admin)
choco install minikube kubectl

# Using winget
winget install Kubernetes.minikube Kubernetes.kubectl
</details><details> <summary><b>macOS</b></summary>
bash
brew install minikube kubectl
</details><details> <summary><b>Linux</b></summary>
bash
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
</details>
Start Minikube
bash
# Start cluster with sufficient resources
minikube start --cpus=4 --memory=8192 --driver=docker

# Enable essential addons
minikube addons enable ingress
minikube addons enable dashboard

# Verify cluster status
kubectl get nodes
minikube status
Deploy Application
bash
# Navigate to k8s directory
cd k8s

# Set Docker environment to Minikube
# For PowerShell:
& minikube -p minikube docker-env | Invoke-Expression
# For Linux/macOS:
eval $(minikube -p minikube docker-env)

# Build image in Minikube
docker build -t transaction-api:latest ..

# Deploy all resources
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml
kubectl apply -f hpa.yaml

# Watch pods start
kubectl get pods -w
Access the Application
<details> <summary><b>   Method 1: Port-Forwarding (Recommended for Development)</b></summary>
bash
# Forward service port to localhost
kubectl port-forward service/transaction-api-service 8484:80

# Access endpoints
curl http://localhost:8484/actuator/health
# Swagger UI: http://localhost:8484/swagger-ui/index.html
</details><details> <summary><b>     Method 2: Minikube Service</b></summary>
bash
# Get service URL
minikube service transaction-api-service --url

# Access the URL provided (e.g., http://127.0.0.1:51588)
</details><details> <summary><b>     Method 3: Ingress with Custom Domain</b></summary>
bash
# Start tunnel (keep terminal open)
minikube tunnel

# Get Minikube IP
minikube ip

# Add to hosts file (Windows: C:\Windows\System32\drivers\etc\hosts)
<minikube-ip> transaction-api.local

# Access via custom domain
curl http://transaction-api.local/actuator/health
</details>
Scaling and Management
bash
# Scale deployment
kubectl scale deployment transaction-api --replicas=3

# Check HPA status
kubectl get hpa -w

# View logs from all pods
kubectl logs -f -l app=transaction-api

# Describe pod for details
kubectl describe pod transaction-api-xxx

# Check resource usage
kubectl top pods
kubectl top nodes
Kubernetes Commands Cheat Sheet
Command	Description
kubectl get pods	List all pods
kubectl get deployments	List deployments
kubectl get services	List services
kubectl get ingress	List ingress rules
kubectl logs <pod-name>	View pod logs
kubectl exec -it <pod-name> -- sh	Access pod shell
kubectl delete -f k8s/	Delete all resources
Clean Up
bash
# Delete all Kubernetes resources
kubectl delete -f k8s/

# Stop Minikube
minikube stop

# Delete cluster (if needed)
minikube delete
     API Documentation
Once running, access the interactive Swagger documentation:

Environment	URL
Local	http://localhost:8484/swagger-ui/index.html
Kubernetes (port-forward)	http://localhost:8484/swagger-ui/index.html
Kubernetes Service	http://127.0.0.1:xxxxx/swagger-ui/index.html

     Authentication Endpoints
Method	Endpoint	Description	Request Body
POST	/api/v1/auth/register	Register new user	{username, email, password, firstName, lastName}
POST	/api/v1/auth/login	    Login and get JWT	{username, password}
POST	/api/v1/auth/refresh	Refresh access token	{refreshToken}
POST	/api/v1/auth/logout	    Logout user	-

     Transaction Endpoints
Method	Endpoint	                                        Description	Auth Required
POST	/api/v1/transactions	                            Create transaction	    
GET	    /api/v1/transactions/{id}	                        Get transaction by ID	    
GET	    /api/v1/transactions/user/{userId}	                Get user transactions	    
GET	    /api/v1/transactions/user/{userId}/aggregate	    Get aggregated data	    
GET	    /api/v1/transactions/user/{userId}/count	        Count transactions	    
GET	    /api/v1/transactions/user/{userId}/validate/{id}	Validate ownership	    

     User Endpoints
Method	Endpoint	                        Description	Auth Required
POST	/api/v1/users	                    Create user	    
GET	    /api/v1/users/{id}	                Get user by ID	    
GET	    /api/v1/users/username/{username}	Get by username	    
GET	    /api/v1/users	                    List all users	    
PUT	    /api/v1/users/{id}	                Update user	    
DELETE	/api/v1/users/{id}	                Delete user	    

     Testing Strategy
Why Comprehensive Testing?
     Reliability: Catch bugs before deployment
     Confidence: Refactor with peace of mind
     Documentation: Tests show how code should behave
     Quality: Enforces best practices

Test Types
bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TransactionServiceTest

# Run specific test method
mvn test -Dtest=TransactionServiceTest#createTransaction_Success

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
# Open target/site/jacoco/index.html in browser


Test Coverage
Test Type	Coverage	Description
Unit Tests	90%+	Service layer testing
Repository Tests	85%+	Data access layer
Controller Tests	90%+	API endpoints with security
Integration Tests	80%+	Full application context
Kubernetes Tests	Manual	Deployment validation

```
### Project Structure
text
     transaction-aggregation-api
├──    src
│   ├──  main
│   │   ├──  java/com/example/transaction_api
│   │   │   ├──  controller      # REST endpoints
│   │   │   ├──  service          # Business logic
│   │   │   ├──  repository       # Data access
│   │   │   ├──  model            # JPA entities
│   │   │   ├──  dto              # Data transfer objects
│   │   │   ├──  exception        # Custom exceptions
│   │   │   ├──  security         # JWT & security
│   │   │   └──  config           # Configuration
│   │   └──  resources
│   │       ├──  application.yml  # Main config
│   │       └──  data.sql         # Test data
│   └──  test                     # Test classes
├──  k8s                          # Kubernetes manifests
│   ├──  configmap.yaml
│   ├──  secret.yaml
│   ├──  deployment.yaml
│   ├──  service.yaml
│   ├──  ingress.yaml
│   └──  hpa.yaml
├──  Dockerfile                    # Docker config
├──  docker-compose.yml            # Compose config
├──  pom.xml                       # Maven deps
└──  README.md                     # This file

     Environment Variables
yaml
# Application configuration
SPRING_PROFILES_ACTIVE: dev|docker|k8s
SERVER_PORT: 8484

# Database (H2)
SPRING_DATASOURCE_URL: jdbc:h2:mem:transactiondb
SPRING_DATASOURCE_USERNAME: sa
SPRING_DATASOURCE_PASSWORD: password

# JWT Configuration
JWT_SECRET: your-256-bit-secret
JWT_EXPIRATION: 86400000        # 24 hours in milliseconds
JWT_REFRESH_EXPIRATION: 604800000  # 7 days in milliseconds

     Health Checks
Endpoint	Purpose	Used By
/actuator/health/liveness	Liveness probe	Kubernetes
/actuator/health/readiness	Readiness probe	Kubernetes
/actuator/health	Standard health	Load balancers
/actuator/metrics	Application metrics	Monitoring tools
bash
# Test health endpoints
curl http://localhost:8484/actuator/health
curl http://localhost:8484/actuator/metrics

     Performance Optimizations
Optimization	Implementation	Benefit
Database        Indexes	Strategic indexes on user_id, date, category	Faster queries
Pagination	    Pageable requests for large datasets	Memory efficiency
Connection      Pooling	HikariCP configuration	Reduced latency
Caching         Ready	Spring Cache abstraction	Faster responses
Lazy            Loading	JPA fetch strategies	Reduced data transfer
Horizontal      Scaling	Kubernetes HPA	Handle traffic spikes

# Security Considerations
Concern	            Implementation
Passwords	        BCrypt with salt rounds (10+ rounds)
Tokens	            JWT with 24h expiration, 7d refresh
Input               Validation	Bean Validation on all endpoints
SQL                 Injection	JPA parameterized queries
CORS	            Configured for specific origins only
K8s                 Secrets	Base64 encoded, RBAC protected
Network Policies	Pod isolation (configurable)

# Contributing
How to Contribute?
Create a feature branch (git checkout -b feature/amazing)
Commit your changes (git commit -m 'Add amazing feature')
Push to the branch (git push origin feature/amazing)
Open a Pull Request

# Development Guidelines
Guideline	Description
     Write tests	New features must include tests
     Follow style	Match existing code style
     Update docs	Keep documentation current
     Pass all tests	Ensure CI pipeline passes
     Meaningful commits	Clear commit messages
     License
This project is licensed under the MIT License - see the LICENSE file for details.

text
<div align="center">
⭐ Built with ❤️ for modern financial applications ⭐
Report Bug •
Request Feature •
Star the Project

If you found this project helpful, please give it a star! ⭐

</div> 