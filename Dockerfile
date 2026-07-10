FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM tomcat:10.1-jdk17-temurin
WORKDIR /usr/local/tomcat/webapps

RUN rm -rf ROOT
COPY --from=build /app/target/archat-1.0-SNAPSHOT.war ROOT.war

EXPOSE 8080
