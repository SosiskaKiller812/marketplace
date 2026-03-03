#  Marketplace Microservices
<p align="center">
  <img src="https://img.shields.io/badge/status-under%20development-yellow?style=flat-square" alt="Status"/>
  <img src="https://img.shields.io/badge/release-pre--alpha-orange?style=flat-square" alt="Pre-alpha"/>
</p>

> 🚧 **Heads up!** This project is under active development. APIs and data models may change without notice.

**Marketplace** – a modern, cloud‑native e‑commerce platform built with a microservices architecture. The system is designed for scalability, maintainability, and high performance, leveraging the latest Java ecosystem and industry best practices.

---

##  Microservices Overview

The platform consists of five core services, each responsible for a specific business capability:

| Service       | Description                                                                                    |
|---------------|------------------------------------------------------------------------------------------------|
| **Auth**      | Handles authentication, authorization, and JWT issuance/validation.                            |
| **User**      | Manages user profiles, addresses, and account settings.                                        |
| **Product**   | Manages product catalog, categories, inventory, and search.                                    |
| **Gateway**   | API Gateway – single entry point for external clients, routing, rate limiting.                 |
| **BFF**       | Backend‑for‑Frontend – tailored API for frontend clients, aggregates data, JWT validation      |

---

##  Technology Stack

| Area                 | Technologies                                                                                                                                                |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Language**         | Java 21                                                                                                                                                     |
| **Frameworks**       | Spring Boot 3, Spring Security, Spring Data JPA, Spring Cloud Gateway                                                                                       |
| **Security**         | JWT (asymmetric keys – private/public), OAuth2 Resource Server                                                                                              |
| **Database**         | PostgreSQL, Liquibase for migrations                                                                                                                        |
| **API Documentation**| Swagger (springdoc‑openapi)                                                                                                                                 |
| **Containerization** | Docker, Docker Compose                                                                                                                                      |
| **Build Tool**       | Maven                                                                                                                                                       |

---

##  Design Patterns & Best Practices

The project embraces a variety of proven design patterns to ensure clean, maintainable, and scalable code:

| Pattern            | Usage                                                                                                 |
|--------------------|--------------------------------------------------------------------------------------------------------|
| **MVC**            | Spring Controllers, Services, Repositories – separates presentation, business logic, and data access. |
| **DTO**            | Data Transfer Objects for decoupling API contracts from internal entities.                             |
| **Builder**        | Used extensively with Lombok `@Builder` for immutable objects and test data creation.                  |
| **BFF**            | Dedicated backend service per frontend (BFF) to optimise data for specific clients.                    |
| **API Gateway**    | Single entry point (`gateway`) routing requests to appropriate microservices.                           |
| **Repository**     | Spring Data JPA repositories abstracting database access.                                              |
| **Service Layer**  | Business logic encapsulated in `@Service` beans.                                                       |
| **Factory**        | For creating complex objects (e.g., test fixtures, DTOs).                                              |
| **Strategy**       | Used in pricing/discount calculations (e.g., different promotion strategies).                          |
| **Observer/Event** | Spring `ApplicationEvent` for cross‑service communication (e.g., user registration → send email).      |
| **CQRS**           | Lightweight separation of read and write models in some services (e.g., product search vs. management).|
| **Singleton**      | Spring beans are singletons by default, ensuring efficient resource usage.                             |
| **SOLID Principles**| Applied throughout – single responsibility, open/closed, etc.                                        |

---

##  Security Architecture

Authentication is based on **asymmetric JWT signatures**:
- **Auth service** holds the **private key** to sign tokens.
- Gateway service use the **public key** to verify incoming JWTs without needing to call the Auth service on every request.

Spring Security Resource Server configuration is used in all protected microservices.  
Tokens include roles and custom claims, enabling fine‑grained authorization.

---

## 🐳 Infrastructure & Deployment

All services are containerized with Docker. A `docker-compose.yml` at the root orchestrates the entire system:

- PostgreSQL instances (one per service, but can be shared for simplicity).
- Liquibase runs automatically on startup to apply database migrations.
- The **gateway** service exposes port `8180` and routes requests based on path prefixes (e.g., `/auth/**`, `/users/**`, `/products/**`).
- The **BFF** service sits behind the gateway and provides aggregated APIs for the frontend.

```bash
# Start the whole platform
docker-compose up -d
