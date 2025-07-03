# Reasoning README for Multi-Tenant Web Application

This document outlines the architectural and design decisions made during the development of this multi-tenant web application, built with Quarkus.

## 1. Core Requirements & Tech Stack

The application is designed to meet the following high-level requirements:
- **Multi-tenancy & RBAC**: Isolate tenants and provide role-based access control within each tenant.
- **APIs**: Expose functionality via versioned REST, gRPC, and an MCP interface for AI agents.
- **Authentication**: Secure user and AI agent access using OAuth2/OIDC, including an "On-Behalf-Of" flow for agents.
- **Scalability**: Stateless microservices, asynchronous processing for heavy workloads, targeting ~10M users.
- **Tech Stack**: Quarkus (Java 17+ LTS), PostgreSQL, Hibernate Panache, Flyway, Kafka/RabbitMQ (TBD).

**Chosen Stack Justification**:
- **Quarkus**: Selected for its Kubernetes-native design, developer productivity (hot reload, dev services), performance (fast startup, low memory), and comprehensive ecosystem for building modern cloud-native Java applications. Its reactive capabilities are well-suited for scalable systems.
- **Java 17+ (LTS)**: Provides modern language features, performance improvements, and long-term support.
- **PostgreSQL**: A robust, open-source relational database with strong support for complex queries, JSON, and scalability features.
- **Hibernate Panache**: Simplifies JPA-based data access in Quarkus, reducing boilerplate and improving developer experience.
- **Flyway**: For version-controlled database schema migrations, ensuring consistent schema across environments.
- **REST (JAX-RS with RESTEasy Reactive)**: Standard for web APIs, well-supported by Quarkus for both imperative and reactive programming models. RESTEasy Reactive is chosen for better performance and alignment with a reactive architecture.
- **gRPC**: For high-performance, cross-platform RPC, suitable for inter-service communication.
- **OAuth2/OIDC**: Standard protocols for secure authentication and authorization. Quarkus provides excellent support via its `quarkus-oidc` extension.
- **Messaging Queue (Kafka/RabbitMQ - To Be Decided)**: Essential for decoupling services and handling asynchronous tasks to improve scalability and resilience. The specific choice will be made based on detailed workload analysis (e.g., Kafka for high-throughput streaming, RabbitMQ for flexible routing and traditional messaging). For now, `quarkus-kafka-client` is included as a placeholder.
- **Config Profiles**: Quarkus's built-in support for configuration profiles (`%dev`, `%prod`, etc.) allows for easy management of environment-specific settings.

## 2. Architecture

A **layered architecture** (Controllers/Resources -> Services -> Repositories/Entities) will be followed:
- **Web/gRPC/MCP Layers (Controllers/Resources)**: Handle incoming requests, perform initial validation, extract parameters, and delegate to the service layer. This layer is responsible for API contracts and data transformation (DTOs).
- **Service Layer**: Contains the core business logic, orchestrates calls to repositories or other services, and handles transactions. This layer is tenant-aware.
- **Data Access Layer (Repositories/Entities)**: Manages data persistence using Hibernate Panache entities and repositories. All data access will be designed with tenant isolation in mind.

**Statelessness**: Services will be designed to be stateless. Tenant context (e.g., `Tenant-ID`) will be passed in requests (e.g., via HTTP headers or gRPC metadata) to ensure that each request can be processed independently and correctly routed to the appropriate tenant's data and configuration.

## 3. Multi-Tenancy Strategy

- **Tenant Isolation**: Each tenant will operate as a distinct security domain.
    - **Database**: A common approach is to use a shared database with a discriminator column (e.g., `tenant_id`) on all tenant-specific tables. Row-Level Security (RLS) in PostgreSQL, or Hibernate filters, will be employed to ensure that queries are automatically scoped to the current tenant, preventing data leakage.
    - **OIDC**: Quarkus's OIDC multi-tenancy support (`quarkus-oidc`'s dynamic resolver or multiple named OIDC configurations) will be leveraged. Each tenant might correspond to a separate OIDC realm or client configuration if required by the identity provider setup. The `Tenant-ID` from the request will be crucial in resolving the correct OIDC configuration or validating tokens against the correct tenant issuer.

## 4. Role-Based Access Control (RBAC)

- **Within each tenant**:
    - Tenant administrators will be able to define roles (e.g., `viewer`, `editor`, `admin`).
    - Permissions will be associated with these roles.
    - Users within a tenant will be assigned one or more roles.
- **Implementation**:
    - Database tables will store `Roles`, `Permissions`, and `UserRoles` mappings, all scoped by `tenant_id`.
    - Quarkus security extensions (`@RolesAllowed`, custom security checks) will be used to enforce these permissions at the API level. JWTs obtained via OIDC will carry role information (or claims that can be mapped to roles/permissions).

## 5. API Design

- **REST APIs**: Versioned (e.g., `/api/v1/...`). Standard HTTP methods and status codes will be used. DTOs (Data Transfer Objects) will be used for request/response bodies.
- **gRPC Services**: Defined using `.proto` files. Will largely mirror REST functionality for relevant use cases.
- **MCP Interface**: A custom protocol (details TBD, likely over HTTP/gRPC) for AI agents. Each request will require a `Tenant-ID` header for routing and context.

## 6. Authentication for AI Agents

- **OAuth2 "On-Behalf-Of" (OBO) Flow (or similar)**: This is the preferred approach. An AI agent, after authenticating itself (e.g., via client credentials), will exchange its token (or another grant) for a new token that includes the user's identity and permissions. This allows the agent to act on behalf of the user while respecting the user's context and access rights.
    - This avoids giving agents direct access to user credentials.
    - The specific implementation will depend on the capabilities of the chosen OIDC provider.
- **Resource Owner Password Credential (ROPC) Grant**: Will be avoided due to security risks, only considered if no other viable flow meets a critical requirement, and this decision would be heavily documented and justified.

## 7. Error Handling

- **Consistent JSON Responses (REST)**: Structured JSON errors with fields like `status`, `code` (internal error code), `message` (human-readable), and optionally `details`.
    - Example: `{"status": "error", "code": "TENANT_NOT_FOUND", "message": "Tenant with the specified ID does not exist."}`
- **gRPC**: Use standard gRPC status codes and error details.
- **Documented Error Codes**: A list of custom error codes and their meanings will be maintained in `docs/error_codes.md`.

## 8. Configuration Management

- **Quarkus Profiles**: `application.yml` will use profiles (`%dev`, `%sit`, `%pre-prod`, `%prod`) for environment-specific configurations.
- **Secrets Management**: For sensitive data like passwords and API keys, environment variables or a dedicated secrets management tool (e.g., HashiCorp Vault, Kubernetes Secrets) will be used, especially for PRE-PROD and PROD. Quarkus supports integration with Vault.

## 9. Scalability & Asynchronous Processing

- **Stateless Services**: As mentioned, crucial for horizontal scaling on Kubernetes.
- **Asynchronous Processing**: For tasks that are long-running or can be processed offline (e.g., report generation, bulk operations, notifications), a message queue (Kafka initially chosen, potentially RabbitMQ based on further analysis) will be used.
    - This involves `quarkus-kafka-client` for producing/consuming messages.
    - Events/messages will also be tenant-aware.

## 10. Testing Strategy

- **Unit Tests (JUnit 5)**: For individual components (services, utility classes).
- **Integration Tests (@QuarkusTest)**: For testing API endpoints (REST using RestAssured, gRPC), service interactions, and database operations within the Quarkus environment. Dev Services for databases and message brokers will be utilized to simplify test setup.

## Key Libraries/Patterns (Initial Choices)
- `quarkus-rest-jackson` (with `quarkus-rest`): For REST APIs with JSON using RESTEasy Classic.
- `quarkus-grpc`: For gRPC services.
- `quarkus-smallrye-openapi`: For generating OpenAPI documentation from JAX-RS annotations.
- `quarkus-oidc` / `quarkus-smallrye-jwt`: For OAuth2/OIDC authentication and JWT handling.
- `quarkus-hibernate-orm-panache`: For simplified JPA.
- `quarkus-jdbc-postgresql`: For PostgreSQL connectivity.
- `quarkus-flyway`: For database migrations.
- `quarkus-kafka-client`: For Kafka integration (initial choice for messaging).
- `quarkus-config-yaml`: For YAML configuration.
- `quarkus-junit5`, `rest-assured`: For testing.

This document will be updated as the project evolves and more detailed design decisions are made.
