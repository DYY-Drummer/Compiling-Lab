package com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Parser {
    ArrayList<Token> token_list=Tokenizer.token_list;
    ArrayList<Variable> variable_list=new ArrayList<>();
    Map<String,Integer> register_map=new HashMap<>();
    Calculator calculator=new Calculator();
    StringBuilder expression=new StringBuilder("");
    int currentToken;
    int expValue;
    int registerNum;
    public Token getNextToken()throws Exception{
        currentToken++;
        if(currentToken>token_list.size()-1){
            throw new Exception("Token_list doesn't enough!");
        }
        return token_list.get(currentToken);
    }
    public boolean var_isExist(String name){
        for(Variable i:variable_list){
            if(i.name.equals(name)){
                return true;
            }
        }
        return false;
    }
    public void CompUnit() throws Exception{
        if(token_list.size()==0)
            return;
//        for(int i=0;i<token_list.size();i++){
//            System.out.printf("%s ",token_list.get(i).word);
//        }
        currentToken=-1;
        registerNum=1;
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
        BlockItem();
        if(getNextToken().word.equals("}")){
            System.out.print("}");
        }
        else {
            throw new Exception("wrong RBrace format");
        }
    }
    public void BlockItem()throws Exception{
        Token token=getNextToken();
        if(token.word.equals("}")){
            currentToken--;
        }else if(token.id.equals("Keyword")){
            currentToken--;
            Decl();
        }else if(token.id.equals("Ident")){
            currentToken--;
            Stmt();
        }else{
            throw new Exception("Wrong in BlockItem");
        }
    }
    public void Decl()throws Exception{
        Token token=getNextToken();
        if(token.word.equals("const")){
            ConstDecl();
        }else if (token.word.equals("int")){
            VarDecl();
        }else{
            throw new Exception("Wrong in Decl");
        }
    }
    public void ConstDecl()throws Exception{
        Token token=getNextToken();
        if(!token.word.equals("int")){
            throw new Exception("Wrong BType in ConstDecl");
        }
        ConstDef();
        while(getNextToken().word.equals(",")){
            ConstDef();
        }
        currentToken--;
        if(!getNextToken().word.equals(";")){
            throw new Exception("Missing ';' in ConstDecl");
        }
    }
    public void ConstDef()throws Exception{
        Token token=getNextToken();
        String varName;
        if(token.id.equals("Ident")){
            varName=token.word;
        }else{
            throw new Exception("Missing Ident in ConstDef");
        }
        if(!getNextToken().word.equals("=")){
            throw new Exception("Missing '=' in ConstDef");
        }
        Variable var=new Variable(varName,true);
        ConstInitVal();
        var.value=expValue;
        variable_list.add(var);

    }
    public void ConstInitVal()throws Exception{
        ConstExp();
    }
    public void ConstExp()throws Exception{
        AddExp();
    }
    public void VarDecl()throws Exception{
        VarDef();
        while (getNextToken().word.equals(",")){
            VarDef();
        }
        currentToken--;
        if(!getNextToken().word.equals(";")){
            throw new Exception("Missing ';' in VarDecl");
        }
    }
    public void VarDef()throws Exception{
        Token token=getNextToken();
        if(!token.id.equals("Ident")){
            throw new Exception("Missing Ident in VarDef");
        }
        if(var_isExist(token.word)){
            throw new Exception("local variable name already exist");
        }
        String name=token.word;
        int reg=registerNum;
        System.out.printf("\n\t%%l%d = alloca i32",registerNum);
        register_map.put(name,reg);
        token =getNextToken();
        if(token.word.equals("=")){
            InitVal();
            Variable var=new Variable(name,false);
            var.value=expValue;
            variable_list.add(var);
            //计算表达式这里还要修改成寄存器计算
            System.out.printf("\n\tstore i32 %%%d, i32* %%%d",registerNum,reg);
        }else{
            currentToken--;
        }
    }
    public void InitVal()throws Exception{
        Exp();
    }
    public void Stmt() throws Exception{
        if(getNextToken().word.equals("return")){
            System.out.print("\tret");
        }
        else {
            throw new Exception("Wrong return");
        }
        Exp();
        expValue=calculator.compute(expression.toString(),registerNum);
        //System.out.printf("\ti32 %d",calculator.compute(expression.toString()));
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
