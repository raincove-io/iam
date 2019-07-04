FROM maven:3.6.1-slim as builder
COPY . /app
WORKDIR /app
# skipping test for now on the CI b/c embedded Cassandra won't start
RUN mvn -B package -DskipTests=true

FROM openjdk:8u212-jre
COPY --from=builder /app/target/iam.jar /
ENTRYPOINT ["java", "-jar", "/iam.jar"]
