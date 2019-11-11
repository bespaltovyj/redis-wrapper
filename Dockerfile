FROM openjdk:11.0.2

WORKDIR /opt/redis-test
COPY ./target/redis-client-1.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar","redis-client-1.0-SNAPSHOT.jar"]
