FROM openjdk:8
COPY ./* /app/
WORKDIR /app/
RUN javac -d ./src/com Test.java Analysis.java
