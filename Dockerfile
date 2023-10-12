# Start with a base image containing Maven (which will download and cache dependencies)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# This will cache the Maven layers unless the pom.xml changes
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package

# Now, use the official OpenJDK base image for runtime
FROM maven:3.9.4-eclipse-temurin-17
WORKDIR /app
# Explicitly copy the keystore
COPY src/main/resources/keystore.p12 /app/keystore.p12
# Copy the entire Maven project from the build stage
COPY --from=build /app/pom.xml ./pom.xml
COPY --from=build /app/src ./src

EXPOSE 443

# Set the default command to run your application using Maven
CMD ["mvn", "spring-boot:run"]
