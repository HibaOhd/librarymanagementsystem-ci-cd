# Utiliser une image Java de base
FROM eclipse-temurin:17-jdk-alpine

# Optionnel : métadonnées
LABEL maintainer="ghitabellamine66@gmail.com"
LABEL description="Library Management System App"

# Copier le jar compilé dans le conteneur
COPY target/librarymanagementsystem-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app

# Exposer le port utilisé par l'application 
EXPOSE 9080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
