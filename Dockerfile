FROM alpine:latest AS build
ENV JAVA_HOME=/opt/jdk/jdk-21.0.1
ENV PATH=$JAVA_HOME/bin:$PATH

# (2)
ADD https://download.bell-sw.com/java/21.0.1+12/bellsoft-jdk21.0.1+12-linux-x64-musl.tar.gz /opt/jdk/
RUN tar -xzvf /opt/jdk/bellsoft-jdk21.0.1+12-linux-x64-musl.tar.gz -C /opt/jdk/

# (3)
RUN ["jlink", "--compress=2", \
     "--module-path", "/opt/jdk/jdk-21.0.1/jmods/", \
# (4)
     "--add-modules", "ALL-MODULE-PATH", \
     "--no-header-files", "--no-man-pages", \
     "--output", "/springboot-runtime"]

# (5)
FROM alpine:latest
# (6)
COPY --from=build  /springboot-runtime /opt/jdk
ENV PATH=$PATH:/opt/jdk/bin
EXPOSE 8080
COPY build/libs/delivery-fee-0.0.1-SNAPSHOT.jar /opt/app/
CMD ["java", "-jar", "/opt/app/delivery-fee-0.0.1-SNAPSHOT.jar"]
