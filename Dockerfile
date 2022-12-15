FROM maven:3-amazoncorretto-17 as build_stage

COPY ./pom.xml ./pom.xml
RUN mvn -B dependency:go-offline

COPY ./ ./
RUN mvn -B package
RUN mv ./target/HifumiBot*.jar ./hifumi.jar

FROM amazoncorretto:17 as final_stage

COPY --from=build_stage ./scripts/image/run_jar.sh /opt/run_jar.sh
COPY --from=build_stage ./hifumi.jar /opt/hifumi.jar
RUN chmod +x /opt/run_jar.sh

# get latest security package updates
RUN yum update -y --security

# non-root user
USER 1000
CMD ["sh", "/opt/run_jar.sh"]
# CMD tail -f /dev/null # debugging