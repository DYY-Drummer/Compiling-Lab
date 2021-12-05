package com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Parser {
    ArrayList<Token> token_list=Tokenizer.token_list;
    ArrayList<Variable> variable_list=new ArrayList<>();
    Map<String,Integer> register_map=new HashMap<>();
    Calculator calculator=new Calculator();
    CondCalculator condCalculator=new CondCalculator();
    StringBuilder expression=new StringBuilder("");
    StringBuilder cond_exp=new StringBuilder("");
    int currentToken;
    String expValue;
    int registerNum;

    boolean constInit;
    int label_cond;
    LinkedList<Integer> stack_label_cond=new LinkedList<>();
    int label_if;
    int label_or;
    int label_and;
    int label_stmt;
    String and_result;
    boolean isCondStmt;
    int count_Not;
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
        label_cond=1;
        label_if=1;
        label_or=1;
        label_and=1;
        label_stmt=1;

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
        System.out.print("{");
        Block();
        System.out.print("\n}");
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
        if(!getNextToken().word.equals("{")){
            throw new Exception("Missing LBrace in Block");
        }
        while(!getNextToken().word.equals("}")){
            /*for(int i=currentToken;i<currentToken+6;i++){
            System.out.println(token_list.get(i).word);
        }*/
            currentToken--;
            BlockItem();
        }
    }
    public void BlockItem()throws Exception{
        Token token=getNextToken();
        if(token.word.equals("}")){
            currentToken--;
        }else if(token.word.equals("const")||token.word.equals("int")){
            currentToken--;
            Decl();
        }else{
            currentToken--;
            Stmt();
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
        register_map.put(varName,registerNum);
        System.out.printf("\n\t%%l%d = alloca i32",registerNum);
        exp_format();
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
            token=getNextToken();
            if(token.id.equals("Func")){
                if(token.word.equals("getint")){
                    getint(reg);
                }else if(token.word.equals("getch")){
                    getch(reg);
                }else{
                    throw new Exception("wrong Func in VarDef");
                }
            }else {
                currentToken--;
                InitVal();
                exp_format();
                System.out.printf("\n\tstore i32 %s, i32* %%l%d", expValue, reg);
            }
            var.assigned = true;
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
            expression=new StringBuilder("");
            exp_format();
            System.out.printf("\n\tret i32 %s",expValue);
            if(!getNextToken().word.equals(";")){
                throw new Exception("Missing ';' after Exp in return");
            }

        } else if(token.word.equals("if")){
            if(!token_list.get(currentToken-1).word.equals("else")){
                stack_label_cond.addLast(label_cond);
                label_cond++;
            }
            if(!getNextToken().word.equals("(")){
                throw new Exception("Missing LPar of if Cond");
            }
            System.out.printf("\n\n%d_Label_if:",label_if);
            label_if++;
            Cond();
            if(!getNextToken().word.equals(")")){
                throw new Exception("Missing RPar of if Cond");
            }
            System.out.printf("\n\nLabel_stmt_%d:",label_stmt);
            label_stmt++;
            Stmt();
            System.out.printf("\n\tbr label %%Label_cond_%d",stack_label_cond.getLast());
            if(getNextToken().word.equals("else")){
                if(!getNextToken().word.equals("if"))
                {
                    System.out.printf("\n\nLabel_if_%d:",label_if);
                    label_if++;
                    currentToken--;
                    Stmt();
                    System.out.printf("\n\nLabel_cond_%d:",stack_label_cond.getLast());
                    stack_label_cond.removeLast();
                }else{
                    currentToken--;
                    Stmt();
                }
            }else {
                System.out.printf("\n\nLabel_if_%d:",label_if);
                label_if++;
                currentToken--;
            }

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
            token=getNextToken();
            if(token.id.equals("Func")){
                if(token.word.equals("getint")){
                    getint(reg);
                }else if(token.word.equals("getch")){
                    getch(reg);
                }else{
                    throw new Exception("wrong Func in Stmt");
                }
            }else{
                currentToken--;
                Exp();
                expValue=calculator.compute(expression.toString());
                expression=new StringBuilder("");
                exp_format();
                System.out.printf("\n\tstore i32 %s, i32* %%l%d",expValue,reg);
            }
            var.assigned=true;
            if(!getNextToken().word.equals(";")){
                throw new Exception("Missing ';' after Exp in Assign");
            }
        } else if(token.id.equals("Func")){
            if(token.word.equals("putint")){
                putint();
            }else if(token.word.equals("putch")){
                putch();
            }
            if(!getNextToken().word.equals(";")){
                throw new Exception("Missing ';' after Func in Stmt");
            }
        } else if(token.word.equals("{")){
            currentToken--;
            Block();
        } else{
            Exp();
            if(!getNextToken().word.equals(";")){
                throw new Exception("Missing ';' after Exp in Stmt");
            }
        }

    }
    public void Cond()throws Exception{
        LOrExp();
    }
    public void LOrExp()throws Exception{
        LAndExp();
        System.out.printf("\n\nLabel_or_%d:",label_or);
        label_or++;
        LOrExp_();
        System.out.printf("\n\tbr label %%label_if_%d",label_if);
    }
    public void LOrExp_()throws Exception{
        if(getNextToken().word.equals("||")){
            LAndExp();
            System.out.printf("\n\nLabel_or_%d:",label_or);
            label_or++;
            LOrExp_();
        }else{
            currentToken--;
        }
    }
    public void LAndExp()throws Exception{
        EqExp();
        System.out.printf("\n\nLabel_and_%d:",label_and);
        label_and++;
        LAndExp_();
        System.out.printf("\n\tbr i1 %s,label %%label_stmt_%d, label %%label_or_%d",and_result,label_stmt,label_or);

    }
    public void LAndExp_()throws Exception{
        if(getNextToken().word.equals("&&")){
            EqExp();
            System.out.printf("\n\nLabel_and_%d:",label_and);
            label_and++;
            LAndExp_();
        }else{
            currentToken--;
        }
    }
    public void EqExp()throws Exception{
        RelExp();
        EqExp_();
        and_result= condCalculator.compute(cond_exp.toString());
        cond_exp=new StringBuilder("");
        if(count_Not%2==1){
            System.out.printf("\n\t%%l%d = icmp eq i1 %s, 0",registerNum,and_result);
            and_result="%l"+registerNum;
            registerNum++;
        }
        count_Not=0;
        System.out.printf("\n\tbr i1 %s,label %%label_and_%d, label %%label_or_%d",and_result,label_and,label_or);

    }
    public void EqExp_()throws Exception{
        Token token=getNextToken();
        if(token.word.equals("==")||token.word.equals("!=")){
            if(token.word.equals("==")){
                cond_exp.append("a");
            }else {
                cond_exp.append("b");
            }
            RelExp();
            EqExp_();
        }else {
            currentToken--;
        }
    }
    public void RelExp()throws Exception{
        AddExp();
        expValue=calculator.compute(expression.toString());
        expression=new StringBuilder("");
        exp_format();
        System.out.printf("\n\t%%l%d = %s", registerNum, expValue);
        cond_exp.append("%l"+registerNum);
        registerNum++;
        RelExp_();
    }
    public void RelExp_()throws Exception{
        Token token=getNextToken();
        if(token.word.equals("<")||token.word.equals("<=")||token.word.equals(">")||token.word.equals(">=")){
            if(token.word.equals("<")){
                cond_exp.append("c");
            }else if (token.word.equals(">")){
                cond_exp.append("d");
            }else if(token.word.equals("<=")){
                cond_exp.append("e");
            }else {
                cond_exp.append("f");
            }
            AddExp();
            expValue=calculator.compute(expression.toString());
            expression=new StringBuilder("");
            exp_format();
            System.out.printf("\n\t%%l%d = %s", registerNum, expValue);
            cond_exp.append("%l"+registerNum);
            registerNum++;
            RelExp_();
        }else {
            currentToken--;
        }
    }
    public void Exp()throws Exception{
        AddExp();
    }

    public void AddExp()throws Exception {
        count_Not=0;
        MulExp();
        AddExp_();
    }
    public void AddExp_()throws Exception{
        Token token=getNextToken();
        if(token.word.equals("+")||token.word.equals("-")){
            expression.append(token.word);
            MulExp();
            AddExp_();
        }else {
            currentToken--;
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
        }else{
            currentToken--;
        }
    }
    public void UnaryExp()throws Exception{
        Token token=getNextToken();
        if(token.id.equals("UnaryOp")){
            if(token.word.equals("!")){
                count_Not++;
            }else{
                expression.append(token.word);
            }
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
    public void exp_format(){
        if(expValue.startsWith("v")){
            expValue=expValue.replace("v","%l");
            System.out.printf("\n\t%%l%d = load i32, i32* %s",registerNum,expValue);
            expValue="%l"+registerNum;
            registerNum++;
        }else if(expValue.startsWith("t")){
            expValue=expValue.replace("t","%t");
        }
    }
    public void putch()throws Exception{
        currentToken++;
        Exp();
        expValue=calculator.compute(expression.toString());
        expression=new StringBuilder("");
        exp_format();
        System.out.printf("\n\tcall void @putch(i32 %s)",expValue);
        currentToken++;
    }
    public void putint()throws Exception{
        currentToken++;
        Exp();
        expValue=calculator.compute(expression.toString());
        expression=new StringBuilder("");
        exp_format();
        System.out.printf("\n\tcall void @putint(i32 %s)",expValue);
        currentToken++;
    }
    public void getint(int reg){
        System.out.printf("\n\t%%l%d = call i32 @getint()",registerNum);
        System.out.printf("\n\tstore i32 %%l%d, i32* %%l%d",registerNum,reg);
        registerNum++;
        currentToken+=2;
    }
    public void getch(int reg){
        System.out.printf("\n\t%%l%d = call i32 @getch()",registerNum);
        System.out.printf("\n\tstore i32 %%l%d, i32* %%l%d",registerNum,reg);
        registerNum++;
        currentToken+=2;
    }
}


