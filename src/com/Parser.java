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
    String expValue;
    int registerNum;
    boolean constInit;
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
            System.out.print("{");
        }
        else {
            throw new Exception("wrong LBrace format");
        }
        while(!getNextToken().word.equals("}")){
            /*for(int i=currentToken;i<currentToken+6;i++){
            System.out.println(token_list.get(i).word);
        }*/
            currentToken--;
            BlockItem();
        }
        System.out.print("\n}");
    }
    public void BlockItem()throws Exception{
        Token token=getNextToken();
        if(token.word.equals("}")){
            currentToken--;
        }else if(token.id.equals("Keyword")&&!token.word.equals("return")){
            currentToken--;
            Decl();
        }else if(token.id.equals("Ident")||token.word.equals("return")){
            currentToken--;
            Stmt();
        }else if(token.id.equals("Func")){
            if(token.word.equals("putint")){
                putint();
            }
        }else{System.out.println("-------"+token.word);
            throw new Exception("Wrong in BlockItem");
        }
    }
    public void putint()throws Exception{
        currentToken++;
        Exp();
        expValue=calculator.compute(expression.toString());
        expression=new StringBuilder("");
        System.out.printf("\n\tcall void @putint(i32 %s)",expValue);
        currentToken+=2;
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
        //----------------------------------------------------------------------------------------------
        //var.value=expValue;
        register_map.put(varName,registerNum);
        System.out.printf("\n\t%%l%d = alloca i32",registerNum);
        System.out.printf("\n\tstore i32 %s, i32* %%l%d",expValue,registerNum);
        registerNum++;

        var.assigned=true;
        variable_list.add(var);

    }
    public void ConstInitVal()throws Exception{
        constInit=true;
        ConstExp();
        constInit=false;
    }
    public void ConstExp()throws Exception{
        AddExp();
        expValue=calculator.compute(expression.toString());
        expression=new StringBuilder("");
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
//        for(int i=currentToken;i<currentToken+6;i++){
//            System.out.println(token_list.get(i).word);
//        }
        Token token=getNextToken();
        if(!token.id.equals("Ident")){
            throw new Exception("Missing Ident in VarDef");
        }
        if(var_isExist(token.word)){
            throw new Exception("local variable name already exist");
        }
        String name=token.word;
        int reg=registerNum;
        registerNum++;
        System.out.printf("\n\t%%l%d = alloca i32",reg);
        register_map.put(name,reg);
        Variable var=new Variable(name,false);
        if(getNextToken().word.equals("=")){
            InitVal();

            var.assigned=true;
            System.out.printf("\n\tstore i32 %s, i32* %%l%d",expValue,reg);
        }else{
            currentToken--;
        }
        variable_list.add(var);
    }
    public void InitVal()throws Exception{
        Exp();
        expValue=calculator.compute(expression.toString());
        expression=new StringBuilder("");
    }
    public void Stmt() throws Exception{
        Token token=getNextToken();
        if(token.word.equals("return")){
            Exp();
            expValue=calculator.compute(expression.toString());
            //System.out.printf("\ti32 %d",calculator.compute(expression.toString()));
            expression=new StringBuilder("");
            System.out.printf("\n\t%%l%d = load i32, i32* %s",registerNum,expValue);
            System.out.printf("\n\tret i32 %%l%d",registerNum);
            registerNum++;
        } else if(token_list.get(currentToken+1).word.equals("=")){
            if(!register_map.containsKey(token.word)){
                throw new Exception("variable in Stmt hasn't been declared");
            }
            String name=token.word;
            Variable var = new Variable("temp",false);
            for(Variable i:variable_list){
                if(i.name.equals(name)){
                    var=i;
                    break;
                }
            }
            if(var.isConst){
                throw new Exception("Can't change the value of Const variable");
            }
            int reg=register_map.get(token.word);
            currentToken++;
            Exp();
            expValue=calculator.compute(expression.toString());
            expression=new StringBuilder("");

            System.out.printf("\n\tstore i32 %s, i32* %%l%d",expValue,reg);
            var.assigned=true;

        } else{
            Exp();
        }
        if(!getNextToken().word.equals(";")){
            throw new Exception("Missing ';' in Stmt");
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
        }else if(token.word.equals(";")||token.word.equals(")")||token.word.equals(",")){
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
        }else if(token.word.equals(";")||token.word.equals("+")||token.word.equals("-")||token.word.equals(")")||token.word.equals(",")){
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
        } else if(token.id.equals("Num")){
            expression.append(token.word);
        } else if(token.id.equals("Ident")){
            for(Variable i:variable_list){
                if(token.word.equals(i.name)){
                    if(constInit&&!i.isConst){
                        throw new Exception("Const val can't be init by val");
                    }
                    if((!i.assigned)){
                        throw new Exception("variable in UnaryExp hasn't been assigned");
                    }
                    expression.append("v"+register_map.get(i.name));
                    return;
                }
            }
            throw new Exception("variable in UnaryExp hasn't been declared");
        } else if(token.word.equals("(")){
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
