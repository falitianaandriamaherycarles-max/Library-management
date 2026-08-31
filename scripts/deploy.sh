#!/bin/bash
# deploy.sh - Script de déploiement

set -e

echo "Deployment Script"
echo "═══════════════════════════════════════════"

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Vérification des arguments
if [ $# -lt 1 ]; then
    echo "Usage: ./deploy.sh <environment> [version]"
    echo "Environments: local, staging, production"
    echo "Example: ./deploy.sh local"
    echo "Example: ./deploy.sh staging"
    echo "Example: ./deploy.sh production 1.0.0"
    exit 1
fi

ENVIRONMENT=$1
VERSION=${2:-$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)}

log_info "Deploying version ${VERSION} to ${ENVIRONMENT}..."

# Vérifier que le JAR existe
JAR_FILE="target/library-management-${VERSION}.jar"
if [ ! -f "$JAR_FILE" ]; then
    log_error "JAR file not found: $JAR_FILE"
    log_info "Building the project first..."
    mvn clean package
fi

case $ENVIRONMENT in
    local)
        log_info "Deploying Locally..."
        log_info "Starting application locally..."

        # Arrêter l'application existante
        pkill -f "library-management" 2>/dev/null || true

        # Lancer l'application
        nohup java -jar $JAR_FILE > app.log 2>&1 &
        APP_PID=$!

        log_info "Application started with PID: $APP_PID"
        log_info "Logs: tail -f app.log"
        log_info "API: http://localhost:8080/api/books"
        ;;

    staging)
        log_info "Deploying to Staging..."
        # Simulation de déploiement vers staging
        log_info "Copying artifact to staging server..."
        # scp $JAR_FILE deployer@staging-server:/apps/library/
        log_info "Staging deployment completed!"
        ;;

    production)
        log_info "Deploying to PRODUCTION"

        # Demander confirmation
        read -p "Are you sure you want to deploy to PRODUCTION? (yes/no) " -r
        if [[ ! $REPLY =~ ^[Yy](es)?$ ]]; then
            log_warn "Deployment cancelled."
            exit 0
        fi

        # Exécuter les tests de régression
        log_info "Running regression tests..."
        ./scripts/regression-tests.sh
        if [ $? -ne 0 ]; then
            log_error "Regression tests failed. Aborting deployment."
            exit 1
        fi

        log_info "Deploying to production..."
        # scp $JAR_FILE deployer@prod-server:/apps/library/
        log_info "Production deployment completed!"
        ;;

    *)
        log_error "Unknown environment: $ENVIRONMENT"
        echo "Available environments: local, staging, production"
        exit 1
        ;;
esac

log_info "Deployment completed successfully!"