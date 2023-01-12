FROM adoptopenjdk:11-jre-hotspot

WORKDIR /src

ADD . /src

EXPOSE 8082:8082

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=staging", "-Duser.timezone=Asia/Kolkata", "service-taxpayer-everification-din-business-1.0.0.jar"]
