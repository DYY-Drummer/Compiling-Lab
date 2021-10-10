FROM openjdk:8
COPY ./ /app/
WORKDIR /app/
RUN javac -d . ./Test.java ./Analysis.java
