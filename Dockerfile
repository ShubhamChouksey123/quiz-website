FROM maven:3.8.5-openjdk-17 AS build
COPY . .

# this is not working 
# RUN cd /app ; \
#     ./mvnw spotless:apply; \
#     ./mvnw clean install \
#     ;

RUN cd /app ; \
    mvn spotless:apply;

RUN cd /app ; \
    mvn clean install -DskipTests;


FROM openjdk:17.0.1-jdk-slim
COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]