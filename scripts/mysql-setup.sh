#!/bin/bash
# mysql-setup.sh - Script de configuration et setup MySQL

echo "MySQL Configuration Script"
echo "═══════════════════════════════════════════"

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
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

# Vérifier si MySQL est installé
if ! command -v mysql &> /dev/null; then
    log_error "MySQL n'est pas installé"
    echo "Installation sur Ubuntu/Debian:"
    echo "  sudo apt update && sudo apt install mysql-server -y"
    echo ""
    echo "Installation sur macOS avec Homebrew:"
    echo "  brew install mysql"
    echo ""
    echo "Ou utilisez Docker:"
    echo "  docker-compose -f docker-compose-mysql.yml up -d"
    exit 1
fi

log_info "MySQL est installé"

# Démarrer MySQL si nécessaire
if systemctl is-active --quiet mysql 2>/dev/null; then
    log_info "MySQL est en cours d'exécution"
elif brew services list | grep mysql | grep -q started 2>/dev/null; then
    log_info "MySQL est en cours d'exécution (Homebrew)"
else
    log_warn "MySQL n'est pas en cours d'exécution"
    echo "Démarrage de MySQL..."
    if command -v systemctl &> /dev/null; then
        sudo systemctl start mysql
        sudo systemctl enable mysql
    elif command -v brew &> /dev/null; then
        brew services start mysql
    fi
fi

# Créer la base de données
log_info "Configuration de la base de données..."

# Demander le mot de passe root
read -sp "Entrez le mot de passe root MySQL: " MYSQL_ROOT_PASSWORD
echo ""

# Créer la base de données et l'utilisateur
mysql -u root -p"$MYSQL_ROOT_PASSWORD" << EOF
-- Créer la base de données
CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Créer l'utilisateur
CREATE USER IF NOT EXISTS 'library_user'@'localhost' IDENTIFIED BY 'library_password';
CREATE USER IF NOT EXISTS 'library_user'@'%' IDENTIFIED BY 'library_password';

-- Donner les permissions
GRANT ALL PRIVILEGES ON library_db.* TO 'library_user'@'localhost';
GRANT ALL PRIVILEGES ON library_db.* TO 'library_user'@'%';

-- Appliquer les changements
FLUSH PRIVILEGES;

-- Afficher les bases de données
SHOW DATABASES;

-- Afficher les utilisateurs
SELECT User, Host FROM mysql.user;
EOF

if [ $? -eq 0 ]; then
    log_info "Base de données créée avec succès!"
    log_info "Database: library_db"
    log_info "User: library_user"
    log_info "Password: library_password"
    echo ""
    log_info "Test de connexion:"
    echo "  mysql -u library_user -p library_db"
    echo ""
    log_info "Pour importer les données initiales:"
    echo "  mysql -u root -p library_db < init.sql"
else
    log_error "Erreur lors de la création de la base de données"
    exit 1
fi