# Dockerfile pour l'application Library Management
FROM openjdk:17-jre-slim

LABEL maintainer="team@example.com"
LABEL version="1.0.0"
LABEL description="Library Management System Spring Boot"

# Variables d'environnement
ENV APP_HOME=/app
ENV JAVA_OPTS="-Xmx512m -XX:+UseG1GC"

# Créer le répertoire de l'application
RUN mkdir -p $APP_HOME && \
    mkdir -p $APP_HOME/logs && \
    mkdir -p $APP_HOME/reports

# Définir le répertoire de travail
WORKDIR $APP_HOME

# Copier le fichier JAR
COPY target/library-management-*.jar app.jar

# Copier les scripts
COPY scripts/ scripts/
RUN chmod +x scripts/*.sh

# Exposer le port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD java -jar app.jar --health-check || exit 1

# Point d'entrée
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Volumes
VOLUME ["$APP_HOME/logs", "$APP_HOME/reports"]