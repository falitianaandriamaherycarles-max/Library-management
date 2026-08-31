#!/bin/bash
# test.sh - Exécution des tests

set -e

echo "Running tests..."
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

log_section() {
    echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

# Déterminer le type de tests à exécuter
TEST_TYPE=${1:-"all"}

case $TEST_TYPE in
    "unit")
        log_section "Running Unit Tests Only"
        mvn test
        ;;
    "integration")
        log_section "Running Integration Tests Only"
        mvn verify
        ;;
    "regression")
        log_section "Running Regression Tests Only"
        mvn test -Pregression-tests
        ;;
    "all")
        log_section "Running All Tests"
        mvn test verify
        ;;
    *)
        log_error "Unknown test type: $TEST_TYPE"
        echo "Usage: ./test.sh [unit|integration|regression|all]"
        exit 1
        ;;
esac

# Vérifier les résultats
if [ $? -eq 0 ]; then
    log_info "All tests passed!"

    # Afficher un résumé des tests
    echo -e "\n  Test Summary:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    if [ -d "target/surefire-reports" ]; then
        echo "Test Results:"
        find target/surefire-reports -name "*.xml" | while read file; do
            tests=$(grep -o 'tests="[0-9]*"' "$file" 2>/dev/null | grep -o '[0-9]*' || echo "0")
            failures=$(grep -o 'failures="[0-9]*"' "$file" 2>/dev/null | grep -o '[0-9]*' || echo "0")
            errors=$(grep -o 'errors="[0-9]*"' "$file" 2>/dev/null | grep -o '[0-9]*' || echo "0")
            skipped=$(grep -o 'skipped="[0-9]*"' "$file" 2>/dev/null | grep -o '[0-9]*' || echo "0")
            echo "  - $(basename "$file" .xml): $tests tests, $failures failures, $errors errors, $skipped skipped"
        done
    fi

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Générer le rapport de couverture
    if [ "$TEST_TYPE" != "regression" ]; then
        mvn jacoco:report
        echo "Coverage report: target/site/jacoco/index.html"
    fi

else
    log_error "Tests failed!"
    exit 1
fi