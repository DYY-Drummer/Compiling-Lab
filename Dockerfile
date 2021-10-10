FROM openjdk:8
COPY ./ /app/
WORKDIR /app/
RUN javac -d ./src/com src/com/Test.java src/com/Analysis.java
