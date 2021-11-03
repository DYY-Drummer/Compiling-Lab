FROM openjdk:8
COPY ./ /app/
WORKDIR /app/
RUN javac -cp src/ src/com/Test.java src/com/Tokenizer.java src/com/Token.java src/com/Calculator.java src/com/Parser.java
