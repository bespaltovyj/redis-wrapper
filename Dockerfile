FROM anapsix/alpine-java:8_jdk

WORKDIR /opt/redis-test
COPY ./target/redis-client-1.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar","redis-client-1.0-SNAPSHOT.jar"]
