# ================================
# 1-bosqich: Build Stage
# ================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Maven dependency cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Source code
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# ================================
# 2-bosqich: Runtime Stage
# ================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Timezone
RUN apk add --no-cache tzdata
ENV TZ=Asia/Tashkent

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

# Application port
EXPOSE 8080

# JVM optimizations
ENTRYPOINT ["java", \
"-Xms256m", \
"-Xmx1024m", \
"-XX:+UseG1GC", \
"-XX:+UseContainerSupport", \
"-Djava.security.egd=file:/dev/./urandom", \
"-jar", \
"app.jar"]