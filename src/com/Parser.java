package com;

import java.util.ArrayList;

public class Parser {
    ArrayList<Token> token_list=Tokenizer.token_list;
    Calculator calculator=new Calculator();
    StringBuilder expression=new StringBuilder("");
    int currentToken;
    public Token getNextToken()throws Exception{
        currentToken++;
        if(currentToken>token_list.size()-1){
            throw new Exception("Token_list doesn't enough!");
        }
        return token_list.get(currentToken);
    }
    public void CompUnit() throws Exception{
        if(token_list.size()==0)
            return;
//        for(int i=0;i<token_list.size();i++){
//            System.out.printf("%s ",token_list.get(i).word);
//        }
        currentToken=-1;
        FuncDef();
    }
    public void FuncDef() throws Exception{
        System.out.print("define dso_local");
        FuncType();
        Ident();
        if(getNextToken().word.equals("(")){
            System.out.print("(");
        }
        else {
            throw new Exception("wrong def format");
        }
        if(getNextToken().word.equals(")")){
            System.out.print(")");
        }
        else {
            throw new Exception("wrong def format");
        }
        Block();
    }
    public void FuncType() throws Exception{
        Token token=getNextToken();
        if(token.word.equals("int")){
            System.out.print(" i32");
        }
        else{
            throw new Exception("wrong FuncType");
        }
    }
    public void Ident() throws Exception{
        Token token=getNextToken();
        if(token.word.equals("main")){
            System.out.print(" @main");
        }
    }
    public void Block() throws Exception{
        if(getNextToken().word.equals("{")){
            System.out.print("{\n");
        }
        else {
            throw new Exception("wrong LBrace format");
        }
        Stmt();
        if(getNextToken().word.equals("}")){
            System.out.print("}");
        }
        else {
            throw new Exception("wrong RBrace format");
        }
    }
    public void Stmt() throws Exception{
        if(getNextToken().word.equals("return")){
            System.out.print("\tret");
        }
        else {
            throw new Exception("Wrong return");
        }
        Exp();
        System.out.printf("\ti32 %d",calculator.compute(expression.toString()));
        expression=new StringBuilder("");
        if(!getNextToken().word.equals(";")){
            throw new Exception("Expected ';'");
        }

    }
    public void Exp()throws Exception{
        AddExp();
    }

    public void AddExp()throws Exception {
        MulExp();
        AddExp_();
    }
    public void AddExp_()throws Exception{
        Token token=getNextToken();
        if(token.word.equals("+")||token.word.equals("-")){
            expression.append(token.word);
            MulExp();
            AddExp_();
        }else if(token.word.equals(";")||token.word.equals(")")){
            currentToken--;
        }else{
            throw new Exception("Wrong in AddExp_()");
        }
    }
    public void MulExp()throws Exception{
        UnaryExp();
        MulExp_();
    }
    public void MulExp_()throws Exception{
//        for(int i=currentToken;i<token_list.size();i++)
//            System.out.print(token_list.get(i).word+" ");
        Token token=getNextToken();
        if(token.word.equals("*")||token.word.equals("/")||token.word.equals("%")){
            expression.append(token.word);
            UnaryExp();
            MulExp_();
        }else if(token.word.equals(";")||token.word.equals("+")||token.word.equals("-")||token.word.equals(")")){
            currentToken--;
        }else{
            throw new Exception("Wrong in MulExp_()");
        }
    }
    public void UnaryExp()throws Exception{
        Token token=getNextToken();
        if(token.id.equals("UnaryOp")){
            expression.append(token.word);
            UnaryExp();
        }
        else if(token.id.equals("Num")){
            expression.append(token.word);
        }else if(token.word.equals("(")){
            expression.append(token.word);
            Exp();
            if(getNextToken().word.equals(")")){
                expression.append(")");
            }else{
                throw new Exception("Missing RPar in UnaryExp");
            }
        }else{
            throw new Exception("UnaryExp error");
        }
    }
}
