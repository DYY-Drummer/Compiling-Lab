FROM openjdk:8
COPY ./ /app/
WORKDIR /app/
RUN javac src/com/Test.java src/com/Analysis.java -d ./
