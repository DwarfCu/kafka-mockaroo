FROM alpine/git as clone 
WORKDIR /app
RUN git clone --branch develop https://github.com/DwarfCu/kafka-mockaroo.git

FROM maven:3.5-jdk-8-alpine as build 
WORKDIR /app
COPY --from=clone /app/kafka-mockaroo /app 
RUN mvn clean package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/mockaroo-1.0-jar-with-dependencies.jar /app
ENTRYPOINT [ "/usr/bin/java", "-jar", "mockaroo-1.0-jar-with-dependencies.jar"]