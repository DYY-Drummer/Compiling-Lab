FROM openjdk:8
COPY ./ /app/
WORKDIR /app/
RUN javac -cp src/ src/com/Test.java src/com/Tokenizer.java src/com/Token.java src/com/Calculator.java src/com/CondCalculator.java src/com/ConstCalculator.java src/com/Parser.java src/com/Variable.java
