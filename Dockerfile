FROM maven:3.6.1-slim as builder
COPY . /app
WORKDIR /app
RUN mvn -B verify

FROM openjdk:8u212-jre
COPY --from=builder /app/target/iam.jar /
ENTRYPOINT ["java", "-jar", "/iam.jar"]
