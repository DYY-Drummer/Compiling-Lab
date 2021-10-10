FROM openjdk:8
COPY ./ /app/
WORKDIR /app/
RUN javac -cp src/ src/com/Test.java src/com/Analysis.java
