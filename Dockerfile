FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY settings.xml /root/.m2/settings.xml

COPY . .

RUN find . -name "*.java" -type f -exec sed -i 's/@Slf4j//' {} \; && \
    find . -name "*.java" -type f -exec grep -l "class " {} \; | xargs -I {} sh -c 'echo "private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger($(basename {} .java).class);" > /tmp/logline && sed -i "/class /r /tmp/logline" {}'

RUN cd storage-fs-lib && mvn clean install -DskipTests && cd .. && \
    cd storage-object-lib && mvn clean install -DskipTests && cd ..

RUN cd repsy-api && mvn clean package -DskipTests

RUN ls -la repsy-api/target/

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/repsy-api/target/repsy-api-1.0-SNAPSHOT.jar app.jar

VOLUME /app/storage

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]