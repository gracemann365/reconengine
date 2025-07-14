# ===================================================================
# Just Command Runner for Reconciliation Engine PoC
#
# Provides streamlined commands for common development tasks.
# ===================================================================

# -------------------------------------------------------------------
# Variables
# -------------------------------------------------------------------

# List of all runnable microservice modules
SERVICES := "naas orchestrator matcher reporter escalator monitor"

# -------------------------------------------------------------------
# Default Command (Help)
# -------------------------------------------------------------------

# List all available commands
default:
    @just --list

# -------------------------------------------------------------------
# Build & Compilation Commands
# -------------------------------------------------------------------

# Clean all build artifacts
clean:
    @echo "🧹 Cleaning project build artifacts..."
    @mvn clean

# Build the entire project, skipping tests for speed (dev build)
build:
    @echo "🏗️ Building the entire project (skipping tests)..."
    @mvn clean install -DskipTests

# Build the entire project and run all tests (CI build)
ci-build:
    @echo "✅ Building the entire project and running all tests..."
    @mvn clean install

# -------------------------------------------------------------------
# Quality Assurance & Dependency Commands
# -------------------------------------------------------------------

# Run all unit and integration tests
test:
    @echo "🔬 Running all tests..."
    @mvn test

# Check code style with Spotless
lint:
    @echo "💅 Checking code style with Spotless..."
    @mvn spotless:check

# Apply code style formatting with Spotless
format:
    @echo "🎨 Applying code style formatting with Spotless..."
    @mvn spotless:apply

# Run OWASP Dependency Check to find CVEs
cve-check:
    @echo "🛡️ Running OWASP Dependency Check for CVEs..."
    @mvn dependency-check:check

# Display the dependency tree for the entire project
deps:
    @echo "🌳 Displaying dependency tree..."
    @mvn dependency:tree

# -------------------------------------------------------------------
# Microservice Runtime Commands
# -------------------------------------------------------------------

# Run a specific microservice (e.g., `just run naas`)
run service:
    @echo "🚀 Running the {{service}} microservice..."
    @mvn spring-boot:run -pl {{service}}

# -------------------------------------------------------------------
# Containerization Commands
# -------------------------------------------------------------------

# Build a Docker/OCI image for a specific microservice using Spring Boot
# (e.g., `just build-image naas`)
build-image service:
    @echo "📦 Building container image for {{service}}..."
    @mvn spring-boot:build-image -pl {{service}}

# Build Docker/OCI images for all microservices
build-image-all:
    @echo "📦📦 Building container images for all services..."
    @mvn spring-boot:build-image -DskipTests

# --- Docker Compose (Example) ---
# Uncomment if you have a docker-compose.yml file to run services.

# # Start all services defined in docker-compose.yml
# up:
#   @echo "🐳 Starting all services with Docker Compose..."
#   @docker-compose up -d --build
#
# # Stop all services managed by Docker Compose
# down:
#   @echo "🛑 Stopping all services..."
#   @docker-compose down