package com;

import java.util.*;

public class Parser {
    ArrayList<Token> token_list=Tokenizer.token_list;
    //ArrayList<Variable> variable_list=new ArrayList<>();
    Map<String,Variable> register_map;
    LinkedList<Map<String,Variable>> stack_block = new LinkedList<>();
    Map<String,Variable> global_map = new HashMap<>();
    Calculator calculator=new Calculator();
    CondCalculator condCalculator=new CondCalculator();
    ConstCalculator constCalculator=new ConstCalculator();
    StringBuilder expression=new StringBuilder("");
    StringBuilder cond_exp=new StringBuilder("");
    int currentToken;
    String expValue;
    int registerNum;
    static int registerNum_temp;
    boolean constInit;
    boolean globalInit;
    int label_cond;
    LinkedList<Integer> stack_label_cond=new LinkedList<>();
    LinkedList<Integer> stack_label_if = new LinkedList<>();
    Stack<Integer> stack_label_while=new Stack<>();
    int label_or;
    int label_and;
    int label_stmt;
    int label_while;
    boolean isWhile;
    boolean isWhile_old;
    String and_result;
    int count_Not;
    ArrayList<Integer> dim = new ArrayList<>();
    ArrayList<Integer> dim_current =new ArrayList<>();
    int array_level;
    boolean FParamInit;
    ArrayList<Variable> copyFParam;
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
        label_cond=1;
        label_or=1;
        label_and=1;
        label_stmt=1;
        label_while=1;
        register_map=global_map;
        stack_block.push(global_map);
        isWhile=false;
        FParamInit=false;
        while(!token_list.get(currentToken+1).word.equals("#")){
            if(currentToken<token_list.size()-5&&token_list.get(currentToken+3).word.equals("(")){
                FuncDef();
            }else {
                Decl();
            }
        }
    }
    int paramsNum;
    public void FuncDef() throws Exception{
        registerNum=1;
        registerNum_temp=1;
        register_map=new HashMap<>();
        copyFParam=new ArrayList<>();
        paramsNum=0;
        stack_block.push(register_map);
        System.out.print("\ndefine dso_local");
        Variable funcVar=new Variable("",false,0);
        FuncType(funcVar);
        FuncIdent(funcVar);
        if(getNextToken().word.equals("(")){
            System.out.print("(");
        }
        else {
            throw new Exception("Missing LPar of FunDef");
        }
        if (!getNextToken().word.equals(")")){
            currentToken--;
            FuncFParams();
        }
        funcVar.paramsNum=paramsNum;
        System.out.print(") {");

        copyFParam();
        copyFParam=new ArrayList<>();
        Block();
        System.out.print("\n}");
    }
    public void FuncFParams()throws Exception{
        FParamInit=true;
        FuncFParam();
        paramsNum++;
        while(getNextToken().word.equals(",")){
            System.out.print(", ");
            FuncFParam();
            paramsNum++;
        }
        FParamInit=false;
    }
    public void FuncFParam()throws Exception{
        if(!getNextToken().word.equals("int")){
            throw new Exception("Wrong BType in FuncFParam");
        }
        String varName=getNextToken().word;
        Variable var =new Variable(varName,false,registerNum);
        if(token_list.get(currentToken+1).word.equals("[")){
            ArrayList<Integer> dim=new ArrayList<>();
            currentToken+=2;
            while(getNextToken().word.equals("[")){
                ConstExp();
                dim.add(Integer.parseInt(expValue));
                if(!getNextToken().word.equals("]")){
                    throw new Exception("Missing RBra of Array in FuncFParam");
                }
            }
            currentToken--;
            print_arrayDim(dim,0);
            var.dim=dim;
            var.isFParam=true;
            System.out.printf("* %%l%d",registerNum);
        }else {
            System.out.printf("i32 %%l%d",registerNum);
            copyFParam.add(var);
        }
        var.assigned=true;
        register_map .put(varName,var);
        registerNum++;
    }
    public void copyFParam(){

        for(Variable i:copyFParam){
            System.out.printf("\n\t%%l%d = alloca i32",registerNum);
            System.out.printf("\n\tstore i32 %%l%d, i32* %%l%d",i.reg,registerNum);
            i.reg=registerNum;
            registerNum++;
        }


    }
    public void FuncType(Variable var) throws Exception{
        Token token=getNextToken();
        if(token.word.equals("int")){
            System.out.print(" i32");
            var.isVoid=false;
        }else if (token.word.equals("void")){
            System.out.print(" void");
            var.isVoid=true;
        }
        else{
            throw new Exception("wrong FuncType");
        }
    }
    public void FuncIdent(Variable var) throws Exception{
        String funcName=getNextToken().word;
        if(global_map.containsKey(funcName)){
            throw new Exception("duplicate FuncIdent");
        }
        var.name=funcName;
        global_map.put(funcName,var);
        System.out.printf("@%s",funcName);
    }
    public void Block() throws Exception{
        if(!getNextToken().word.equals("{")){
            throw new Exception("Missing LBrace in Block");
        }
        register_map=new HashMap<>();
        stack_block.addLast(register_map);
        while(!getNextToken().word.equals("}")){
            /*for(int i=currentToken;i<currentToken+6;i++){
            System.out.println(token_list.get(i).word);
        }*/
            currentToken--;
            BlockItem();
        }
        stack_block.removeLast();
        register_map=stack_block.getLast();
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
        if(register_map==global_map){
            globalInit=true;
        }
        if(token.word.equals("const")){
            ConstDecl();
        }else if (token.word.equals("int")){
            VarDecl();
        }else{
            throw new Exception("Wrong in Decl");
        }
        globalInit=false;
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
        if(register_map.containsKey(token.word)){
            throw new Exception("Duplicate ConstDef name in same Block");
        }

        while(getNextToken().word.equals("[")){
            ConstExp();
            dim.add(Integer.parseInt(expValue));
            if(!getNextToken().word.equals("]")){
                throw new Exception("Missing RBra of Array in ConstDef");
            }
        }
        currentToken--;
        if(!getNextToken().word.equals("=")){
            throw new Exception("Missing '=' in ConstDef");
        }
        int reg=0;
        if(dim.size()>0){
            for(int i=0;i<dim.size();i++){
                dim_current.add(0);
            }
            array_level=-1;
            if(register_map!=global_map){
                reg=registerNum;
                registerNum++;
                array_memset(reg);
                arrayInitVal(registerNum-1);
            } else {
                System.out.printf("\n@%s = dso_local global ",varName);
                print_arrayDim(dim,0);
                System.out.print(" ");
            }
        }


        if(dim.size()==0||register_map==global_map){
            ConstInitVal();
            reg=registerNum;
        }
        Variable var=new Variable(varName,true,reg);


        if(dim.size()==0){
            if (register_map != global_map) {
                System.out.printf("\n\t%%l%d = alloca i32", registerNum);
                exp_format();
                System.out.printf("\n\tstore i32 %s, i32* %%l%d", expValue, registerNum);
                registerNum++;
            } else {
                System.out.printf("\n@%s = dso_local global i32 %s", varName, expValue);
                var.constValue = Integer.parseInt(expValue);
            }
        }else{
            var.dim=dim;
            dim =new ArrayList<>();
            dim_current=new ArrayList<>();
        }
        var.assigned=true;
        register_map.put(varName, var);

    }

    public void array_memset(int reg) {
        System.out.printf("\n\t%%l%d = alloca ", reg);
        print_arrayDim(dim,0);
        System.out.printf("\n\t%%l%d = getelementptr ",registerNum);
        print_arrayDim(dim,0);
        System.out.print(", ");
        print_arrayDim(dim,0);
        System.out.printf("* %%l%d",reg);
        int size=1;
        for(int i =0;i<=dim.size();i++){
            if(i<dim.size()){
                size*=dim.get(i);
            }
            System.out.print(", i32 0");
        }
        System.out.printf("\n\tcall void @memset(i32* %%l%d, i32 0, i32 %d)",registerNum,size*4);
        registerNum++;
    }

    public void print_arrayDim(ArrayList<Integer> dim,int i){
        if(dim.size()==0){
            System.out.print("i32");
        }else if(i<dim.size()-1){
            System.out.printf("[%d x ",dim.get(i));
            print_arrayDim(dim,i+1);
            System.out.print("]");
        }else{
            System.out.printf("[%d x i32]",dim.get(i));

        }
    }

    public String string_arrayDim(ArrayList<Integer> dim,int i){
        StringBuilder arrayFormat=new StringBuilder("");
        if(dim.size()==0){
            arrayFormat.append("i32* ");
        }else if(i<dim.size()-1){
            arrayFormat.append("["+dim.get(i)+" x ");
            arrayFormat.append(string_arrayDim(dim,i+1));
            arrayFormat.append("]");
        }else{
            arrayFormat.append("i32");
        }
        return arrayFormat.toString();
    }
    public void ConstInitVal()throws Exception{
        constInit=true;
        if(getNextToken().word.equals("{")){
            array_level++;
            if(array_level>dim.size()-1){
                throw new Exception("out of the max dim of Array");
            }
            if(!getNextToken().word.equals("}")){
                if(array_level==dim.size()-1&&dim.size()>1){
                    System.out.printf("[%s x i32] ",dim.get(dim.size()-1));
                }
                System.out.print("[");
                currentToken--;
                ConstInitVal();
                dim_current.set(array_level,dim_current.get(array_level)+1);
                while(getNextToken().word.equals(",")){
                    System.out.print(", ");
                    ConstInitVal();
                    dim_current.set(array_level,dim_current.get(array_level)+1);
                }
                currentToken--;
                while(dim_current.get(array_level)<dim.get(array_level)){
                    if(array_level==dim.size()-1){
                        System.out.print(", i32 0");
                    }else{
                        System.out.print(", ");
                        print_arrayDim(dim,array_level+1);
                        System.out.print(" zeroinitializer");
                    }
                    dim_current.set(array_level,dim_current.get(array_level)+1);
                }
                if(!getNextToken().word.equals("}")){
                    System.out.println("Missing RBra of ArrayInit in ConstInitval ");
                }
                System.out.print("]");
            }else{
                if(array_level>0) {
                    print_arrayDim(dim, array_level);
                }
                System.out.print(" zeroinitializer");
                dim_current.set(array_level,dim.get(array_level));

            }
            dim_current.set(array_level,0);
            array_level--;
        }else {
            currentToken--;
            ConstExp();
            if(dim.size()>0){
                System.out.print("i32 "+expValue);
            }
        }
        constInit=false;
    }
    public void ConstExp()throws Exception{
        AddExp();
        if(register_map==global_map||FParamInit) {
            expValue=constCalculator.compute(expression.toString());
        }else{
            expValue = calculator.compute(expression.toString());
        }
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
        if(register_map.containsKey(token.word)){
            throw new Exception("Duplicate VarDef name in the same Block");
        }
        String name=token.word;
        int reg = registerNum;
        Variable var = new Variable(name, false, reg);;
        while(getNextToken().word.equals("[")){
            ConstExp();
            dim.add(Integer.parseInt(expValue));
            if(!getNextToken().word.equals("]")){
                throw new Exception("Missing RBra of Array in VarDef");
            }
        }
        if(dim.size()>0){
            var.dim=dim;
        }
        currentToken--;
        array_level=-1;
        for(int i=0;i<dim.size();i++){
            dim_current.add(0);
        }
        if(register_map!=global_map){
            registerNum++;
            if(dim.size()>0){
                array_memset(reg);
                if (getNextToken().word.equals("=")){
                    arrayInitVal(registerNum-1);
                } else{
                    currentToken--;
                }
                var.assigned=true;
            }else {
                System.out.printf("\n\t%%l%d = alloca i32", reg);
                if (getNextToken().word.equals("=")) {
                    InitVal();
                    exp_format();
                    System.out.printf("\n\tstore i32 %s, i32* %%l%d", expValue, reg);
                    var.assigned = true;
                } else{
                    currentToken--;
                }
            }
        }else{
            if(getNextToken().word.equals("=")){
                if(dim.size()>0){
                    System.out.printf("\n@%s = dso_local global ",var.name);
                    print_arrayDim(dim,0);
                    System.out.print(" ");
                    ConstInitVal();
                }else {
                    InitVal();
                    System.out.printf("\n@%s = dso_local global i32 %s", var.name, expValue);
                }
            }else{
                currentToken--;
                if(dim.size()>0){
                    System.out.printf("\n@%s = dso_local global ",var.name);
                    print_arrayDim(dim,0);
                    System.out.print(" zeroinitializer");
                }else {
                    System.out.printf("\n@%s = dso_local global i32 0", var.name);
                }

            }
            var.assigned=true;

        }
        if(dim.size()>0){
            dim =new ArrayList<>();
            dim_current=new ArrayList<>();
        }

        register_map.put(name,var);
    }
    public void arrayInitVal(int reg)throws Exception{
        if(getNextToken().word.equals("{")){
            array_level++;
            if(array_level>dim.size()-1){
                throw new Exception("out of the max dim of Array");
            }
            if(!getNextToken().word.equals("}")){

                currentToken--;
                arrayInitVal(reg);
                dim_current.set(array_level,dim_current.get(array_level)+1);
                while(getNextToken().word.equals(",")){
                    arrayInitVal(reg);
                    dim_current.set(array_level,dim_current.get(array_level)+1);
                }
                currentToken--;

                if(!getNextToken().word.equals("}")){
                    System.out.println("Missing RBra of ArrayInit in ConstInitval ");
                }

            }else{
                dim_current.set(array_level,dim.get(array_level));
            }
            dim_current.set(array_level,0);
            array_level--;
        }else {
            currentToken--;
            Exp();
            expValue=calculator.compute(expression.toString());
            exp_format();
            expression=new StringBuilder("");
            int position=0;
            for(int i=0;i<dim.size()-1;i++){
                position+=dim_current.get(i)*dim.get(i+1);
            }
            position+=dim_current.get(dim.size()-1);
            System.out.printf("\n\t%%l%d = getelementptr i32, i32* %%l%d, i32 %d",registerNum,reg,position);
            System.out.printf("\n\tstore i32 %s, i32* %%l%d",expValue,registerNum);
            registerNum++;
        }
    }
    public void InitVal()throws Exception{
        Exp();
        if(register_map!=global_map) {
            expValue = calculator.compute(expression.toString());
        }else{
            expValue=constCalculator.compute(expression.toString());
        }
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
            If();
        } else if(token.word.equals("while")){
            While();
        } else if(token.word.equals("break")){
            System.out.printf("\n\tbr label %%Label_whileEnd_%d",stack_label_while.peek());
        } else if(token.word.equals("continue")){
            System.out.printf("\n\tbr label %%Label_while_%d",stack_label_while.peek());
        } else if(isAssign()){
            String name=token.word;
            Map<String,Variable> varMap=isDeclared(name);
            if(varMap==null){
                throw new Exception("variable in Stmt hasn't been declared");
            }
            Variable var=varMap.get(name);
            if(var.isConst){
                throw new Exception("Can't change the value of Const variable");
            }
            String reg;
            if(token_list.get(currentToken+1).word.equals("[")){
                reg=getElementPtr(var);
            }else if(varMap==global_map){
                reg="@"+var.name;
            }else {
                reg="%l"+var.reg;
            }
            currentToken++;
            Exp();
            expValue=calculator.compute(expression.toString());
            expression=new StringBuilder("");
            exp_format();
            System.out.printf("\n\tstore i32 %s, i32* %s",expValue,reg);

            var.assigned=true;
            if(!getNextToken().word.equals(";")){
                throw new Exception("Missing ';' after Exp in Assign");
            }
        } else if(token.id.equals("Func")){
            if(token.word.equals("putint")){
                putint();
            }else if(token.word.equals("putch")){
                putch();
            }else if(token.word.equals("getarray")){
                getarray();
                registerNum_temp++;
            }else if(token.word.equals("putarray")){
                putarray();
            }
            if(!getNextToken().word.equals(";")){
                throw new Exception("Missing ';' after Func in Stmt");
            }
        } else if(token.word.equals("{")){
            currentToken--;
            Block();
        } else if(token.word.equals(";")){

        } else{
            currentToken--;
            Exp();
            expValue=calculator.compute(expression.toString());
            expression=new StringBuilder("");

            if(!getNextToken().word.equals(";")){
                System.out.println();
                for(int i=currentToken-5;i<currentToken+10;i++){
                    System.out.printf(token_list.get(i).word);
                }
                throw new Exception("Missing ';' after Exp in Stmt");
            }
        }

    }
    public boolean isAssign(){
        int currentToken_save=currentToken;
        int flag=0;
        if(token_list.get(currentToken_save+1).word.equals("[")){
            flag++;
            currentToken_save++;
        }
        while(flag>0||token_list.get(currentToken_save+1).word.equals("[")){
            currentToken_save++;
            if(token_list.get(currentToken_save).word.equals("[")){
                flag++;
            }else if(token_list.get(currentToken_save).word.equals("]")){
                flag--;
            }
        }
        return token_list.get(currentToken_save+1).word.equals("=");
    }

    public void If() throws Exception{
        isWhile_old=isWhile;
        if(!token_list.get(currentToken-1).word.equals("else")){
            isWhile=false;
            stack_label_if.addLast(1);
            stack_label_cond.addLast(label_cond);
            label_cond++;
            System.out.printf("\n\tbr label %%Label_if_%d_%d",stack_label_if.getLast(),stack_label_cond.getLast());
        }
        if(!getNextToken().word.equals("(")){
            throw new Exception("Missing LPar of if Cond");
        }
        System.out.printf("\n\nLabel_if_%d_%d:",stack_label_if.getLast(),stack_label_cond.getLast());
        int temp=stack_label_if.removeLast();
        stack_label_if.addLast(temp+1);
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
                System.out.printf("\n\nLabel_if_%d_%d:",stack_label_if.getLast(),stack_label_cond.getLast());
                int label=stack_label_if.removeLast();
                stack_label_if.addLast(label+1);
                currentToken--;
                Stmt();
                System.out.printf("\n\tbr label %%Label_cond_%d",stack_label_cond.getLast());
                System.out.printf("\n\nLabel_cond_%d:",stack_label_cond.getLast());
                stack_label_cond.removeLast();
                stack_label_if.removeLast();
            }else{
                currentToken--;
                Stmt();
            }
        }else {
            System.out.printf("\n\nLabel_if_%d_%d:",stack_label_if.getLast(),stack_label_cond.getLast());
            int label=stack_label_if.removeLast();
            stack_label_if.addLast(label+1);
            System.out.printf("\n\tbr label %%Label_cond_%d",stack_label_cond.getLast());
            System.out.printf("\n\nLabel_cond_%d:",stack_label_cond.getLast());
            stack_label_cond.removeLast();
            stack_label_if.removeLast();
            currentToken--;
        }
        isWhile=isWhile_old;
    }
    public void While() throws Exception{
        isWhile=true;
        stack_label_while.push(label_while);
        label_while++;
        System.out.printf("\n\tbr label %%Label_while_%d",stack_label_while.peek());
        if(!getNextToken().word.equals("(")){
            throw new Exception("Missing LPar of while Cond");
        }
        System.out.printf("\n\nLabel_while_%d:",stack_label_while.peek());

        Cond();

        if(!getNextToken().word.equals(")")){
            throw new Exception("Missing RPar of while Cond");
        }
        System.out.printf("\n\nLabel_stmt_%d:",label_stmt);
        label_stmt++;
        Stmt();
        System.out.printf("\n\tbr label %%Label_while_%d",stack_label_while.peek());
        System.out.printf("\n\nLabel_whileEnd_%d:",stack_label_while.pop());
        isWhile=false;
    }
    public void Cond()throws Exception{
        LOrExp();
    }
    public void LOrExp()throws Exception{
        LAndExp();
        System.out.printf("\n\nLabel_or_%d:",label_or);
        label_or++;
        LOrExp_();
        if(isWhile){
            System.out.printf("\n\tbr label %%Label_whileEnd_%d",stack_label_while.peek());
        }else {
            System.out.printf("\n\tbr label %%Label_if_%d_%d", stack_label_if.getLast(), stack_label_cond.getLast());
        }
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
        System.out.printf("\n\tbr i1 %s,label %%Label_stmt_%d, label %%Label_or_%d",and_result,label_stmt,label_or);

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
        System.out.printf("\n\tbr i1 %s,label %%Label_and_%d, label %%Label_or_%d",and_result,label_and,label_or);

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
        System.out.printf("\n\t%%l%d = add i32 %s, 0", registerNum, expValue);
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
            System.out.printf("\n\t%%l%d = add i32 %s, 0", registerNum, expValue);
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
        } else if(token.id.equals("Func")){
            if(token.word.equals("getint")){
                getint();
            }else if(token.word.equals("getch")){
                getch();
            }else if(token.word.equals("getarray")){
                getarray();
            }
            expression.append("t"+registerNum_temp);
            registerNum_temp++;
        } else if(token.id.equals("Ident")){
            Map<String,Variable> varMap=isDeclared(token.word);
            if(varMap==null){
                throw new Exception("Ident in UnaryExp hasn't been declared");
            } else if(token_list.get(currentToken+1).word.equals("(")){
                Func();
                if(!global_map.containsKey(token.word)){
                    System.out.println("\n----------"+token.word);
                    throw new Exception("Undeclared Func name");
                }
                Variable var=global_map.get(token.word);
                if(RParamsNum!=var.paramsNum){
                    throw new Exception("RParams' number is not the same as FParam's number");
                }
                if(var.isVoid){
                    System.out.printf("\n\tcall void @%s(%s)",var.name,RParamsInit.toString());
                    expression.append("0");
                }else {
                    System.out.printf("\n\t%%t%d = call i32 @%s(%s)",registerNum_temp,var.name,RParamsInit.toString());
                    expression.append("t"+registerNum_temp);
                    registerNum_temp++;
                }
            } else if(varMap==global_map){
                Variable var=varMap.get(token.word);
                if(globalInit||FParamInit){
                    if(!var.isConst) {
                        throw new Exception("Global(or FParam) val can't be init by var");
                    }
                    expression.append(var.constValue);
                } else{
                    if(token_list.get(currentToken+1).word.equals("[")){
                        String ptr=getElementPtr(var);
                        System.out.printf("\n\t%%t%d = load i32, i32* %s",registerNum_temp,ptr);
                    }else{
                        System.out.printf("\n\t%%t%d = load i32, i32* @%s",registerNum_temp,var.name);
                    }
                    expression.append("t"+registerNum_temp);
                    registerNum_temp++;
                }
            } else{
                Variable var=varMap.get(token.word);
                if(constInit&&!var.isConst){
                    throw new Exception("Const val can't be init by var");
                }
                if((!var.assigned)){
                    throw new Exception("variable in UnaryExp hasn't been assigned");
                }
                if(token_list.get(currentToken+1).word.equals("[")){
                    String ptr=getElementPtr(var);
                    System.out.printf("\n\t%%t%d = load i32, i32* %s",registerNum_temp,ptr);
                    expression.append("t"+registerNum_temp);
                    registerNum_temp++;
                }else{
                    expression.append("v"+varMap.get(var.name).reg);
                }
            }
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
    public String getElementPtr(Variable var)throws Exception{

        ArrayList<String> index=new ArrayList<>();
        while(getNextToken().word.equals("[")){
            StringBuilder expression_save=expression;
            expression=new StringBuilder("");
            Exp();
            expValue=calculator.compute(expression.toString());
            exp_format();
            expression=expression_save;
            index.add(expValue);
            if(!getNextToken().word.equals("]")){
                throw new Exception("Missing RBra of Array index in getElementPtr()");
            }
        }
        currentToken--;
        System.out.printf("\n\t%%t%d = getelementptr ",registerNum_temp);
        registerNum_temp++;
        print_arrayDim(var.dim,0);
        System.out.print(", ");
        print_arrayDim(var.dim,0);
        if(isDeclared(var.name)==global_map){
            System.out.printf("* @%s",var.name);
        }else{
            System.out.printf("* %%l%d",var.reg);
        }
        if(!var.isFParam){
            System.out.print(", i32 0");
        }
        for(String i:index){
            System.out.printf(", i32 %s",i);
        }
        for(int i=index.size();i<var.dim.size();i++){
            System.out.print(", i32 0");
        }
        return "%t"+ (registerNum_temp - 1);
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
    public Map<String,Variable> isDeclared(String name){
        for(int i=stack_block.size()-1;i>=0;i--){
            if(stack_block.get(i).containsKey(name)){
                return stack_block.get(i);
            }
        }
        if(global_map.containsKey(name)){
            return global_map;
        }
        return null;
    }
    int RParamsNum;
    public void Func()throws Exception{
        currentToken++;
        RParamsInit=new StringBuilder("");
        RParamsNum=0;
        if(!token_list.get(currentToken+1).word.equals(")")){
            FuncRParams();
        }
        if(!getNextToken().word.equals(")")){
            throw new Exception("Missing RPar of FuncRParams");
        }
    }
    StringBuilder RParamsInit;
    int dimCount;
    public void FuncRParams()throws Exception{
       FuncRParam();
       RParamsNum++;
       while(getNextToken().word.equals(",")){
           RParamsInit.append(", ");
           FuncRParam();
           RParamsNum++;
       }
       currentToken--;
    }
    public void FuncRParam()throws Exception{
        String varName = getNextToken().word;


        if(isPtrParam(varName)){
            Variable var=isDeclared(varName).get(varName);
            RParamsInit.append(string_arrayDim(var.dim, dimCount)+"* "+getElementPtr(var));
        }else{
            currentToken--;
            RParamsInit.append("i32 ");
            Exp();
            expValue=calculator.compute(expression.toString());
            exp_format();
            expression=new StringBuilder("");
            RParamsInit.append(expValue);
        }
    }
    public boolean isPtrParam(String varName){
        Map<String,Variable> varMap=isDeclared(varName);
        if(varMap==null){
            return false;
        }
        Variable var=varMap.get(varName);
        int currentToken_temp=currentToken+1;
        dimCount=0;
        while(token_list.get(currentToken_temp).word.equals("[")){
            dimCount++;
            while(!token_list.get(currentToken_temp).word.equals("]")){
                currentToken_temp++;
            }
            currentToken_temp++;
        }
        return var.dim != null && dimCount < var.dim.size();
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
    public void putarray()throws Exception{
        RParamsInit=new StringBuilder("");
        RParamsNum=0;
        currentToken++;
        FuncRParams();
        if(RParamsNum!=2){
            throw new Exception("putarray(i32, i32*)'s RParams number is not 2");
        }
        currentToken++;
        System.out.printf("\n\tcall i32 @putarray(%s)",RParamsInit.toString());
    }
    public void getint(){
        System.out.printf("\n\t%%t%d = call i32 @getint()",registerNum_temp);
        currentToken+=2;
    }
    public void getch(){
        System.out.printf("\n\t%%t%d = call i32 @getch()",registerNum_temp);
        currentToken+=2;
    }
    public void getarray()throws Exception{
        RParamsInit=new StringBuilder("");
        RParamsNum=0;
        currentToken++;
        FuncRParams();
        if(RParamsNum!=1){
            throw new Exception("getarray(i32*)'s RParams number is not 1");
        }
        currentToken++;
        System.out.printf("\n\t%%t%d = call i32 @getarray(%s)",registerNum_temp,RParamsInit.toString());
    }
}


