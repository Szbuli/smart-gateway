FROM ibm-semeru-runtimes:open-17-jdk AS build

ADD gradlew build.gradle  /usr/src/app/
ADD gradle /usr/src/app/gradle
ADD lib /usr/src/app/lib
ADD src /usr/src/app/src
ADD config.csv deviceTypes.json  /usr/src/app/

WORKDIR /usr/src/app/

RUN sh gradlew shadowJar

FROM ibm-semeru-runtimes:open-17-jre AS runtime

COPY --from=build /usr/src/app/build/libs/mqtt-gateway.jar /usr/src/mqtt-gateway.jar
COPY --from=build /usr/src/app/build/libs/config.csv /usr/src/config.csv
COPY --from=build /usr/src/app/build/libs/deviceTypes.json /usr/src/deviceTypes.json

WORKDIR /usr/src/app/

ENTRYPOINT ["java", "-jar", "/usr/src/mqtt-gateway.jar", "--mqtt", "/usr/src/mqtt.properties"]
