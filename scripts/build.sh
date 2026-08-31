#!/bin/bash
# build.sh - Script de build complet pour Spring Boot

set -e

echo "Starting build process..."
echo "═══════════════════════════════════════════"

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_section() {
    echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

# Vérification des prérequis
log_section "Checking Prerequisites"

if ! command -v mvn &> /dev/null; then
    log_error "Maven is not installed"
    exit 1
fi

if ! command -v java &> /dev/null; then
    log_error "Java is not installed"
    exit 1
fi

log_info "Maven version: $(mvn -version | head -1)"
log_info "Java version: $(java -version 2>&1 | head -1)"

# Clean
log_section "Cleaning Previous Builds"
mvn clean
log_info "Clean completed"

# Compilation
log_section "Compiling Source Code"
mvn compile
log_info "Compilation successful"

# Tests unitaires
log_section "Running Unit Tests"
mvn test
log_info "Unit tests passed"

# Tests d'intégration
log_section "Running Integration Tests"
mvn verify
log_info "Integration tests passed"

# Couverture de code
log_section "Code Coverage"
mvn jacoco:report
log_info "Coverage report generated"

# Package
log_section "Packaging Application"
mvn package
log_info "Package created"

# Résumé
log_section "Build Summary"
echo -e "${GREEN}Build completed successfully!${NC}"
echo ""
echo "Test Reports: target/surefire-reports/"
echo "Coverage Report: target/site/jacoco/index.html"
echo "Artifact: $(ls -t target/*.jar | head -1)"
echo ""
echo "To run the application:"
echo "  java -jar $(ls -t target/*.jar | head -1)"
echo "  or"
echo "  mvn spring-boot:run"