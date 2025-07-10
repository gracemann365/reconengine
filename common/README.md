# Reconciliation Engine Common Libraries

This module (`common`) serves as the central repository for shared utilities, Data Transfer Objects (DTOs), and cross-cutting concerns across all microservices within the Reconciliation Engine Proof of Concept (PoC).

## Purpose

The primary goal of this module is to promote code reuse, enforce architectural consistency, and standardize common functionalities such as:

*   **Data Transfer Objects (DTOs):** Unified Avro schemas for financial transactions (Quant), unreconciled exceptions (URE), and reconciliation metadata.
*   **Validation:** Generic interfaces and a YAML-driven rule engine for data validation.
*   **Configuration:** Centralized application configuration, feature flag management, and secure secret retrieval.
*   **PCI Compliance:** Utilities for sensitive data masking and audit logging.
*   **Security:** JWT handling, input sanitization, TLS configuration, and AES-GCM encryption.
*   **Resilience:** Integration with Resilience4j for circuit breakers, retries, and timeouts.
*   **Error Handling:** Standardized custom exceptions, DLQ routing, and trace ID injection.
*   **Monitoring:** Structured logging, custom metrics, OpenTelemetry tracing, and dedicated audit logging.

## Key Features

*   **Single Source of Truth:** Defines core data structures and common logic used by all services.
*   **Reduced Duplication:** Prevents redundant code implementations across microservices.
*   **Architectural Consistency:** Ensures all services adhere to common patterns for logging, error handling, and resilience.
*   **Simplified Development:** Provides ready-to-use components, accelerating microservice development.

## How to Use

To use this module in another Maven project, simply add it as a dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.grace.recon</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0-SNAPSHOT</version> <!-- Or the appropriate version -->
</dependency>
```

### Code Generation

This module uses the `avro-maven-plugin` to generate Java DTOs from Avro schemas (`.avsc` files). To regenerate these classes, navigate to the `common` module directory and run:

```bash
mvn generate-sources
```

### Running Tests

To run unit tests for this module, execute:

```bash
mvn test
```

**Note:** If tests are skipped during a full build (`-DskipTests`), you may need to run `mvn test` explicitly to ensure full test coverage and JaCoCo reports are generated.

## Contributing

Contributions are welcome! Please ensure any changes adhere to the project's coding standards and are thoroughly tested.

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
