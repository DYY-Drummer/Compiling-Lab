package com;



public class Analysis {
    public static Analysis analysis=new Analysis();
    String[] opt={"=",";","(",")","{","}","+","*","/", "<",">"};
    public void analyze(String str) throws Exception {

        String[] word =str.split("\\s+");
        for(int i=0;i<word.length;i++) {
            goNext(word[i]);
            //System.out.print("------"+word[i]+"------\n");
        }
    }
    public void goNext(String next) throws Exception {
        if(next.equals("")||next.equals('\t'))
            return;
        //System.out.print("------"+next+"------\n");
        if(Character.isLetter(next.charAt(0))||next.charAt(0)=='_')
        {
            identifier(next);
        }
        else if(Character.isDigit(next.charAt(0))){
            number(next);
        }else if(isOpt(String.valueOf(next.charAt(0)))){
            operator(next);
        }else{
            System.out.println("Err");
            throw new Exception("Wrong input");
        }
    }
    public boolean isOpt(String c)
    {
        for(int i=0;i< opt.length;i++)
            if(opt[i].equals(c))
                return true;
        return false;
    }
    public void identifier(String str) throws Exception {
        char[] temp=str.toCharArray();
        StringBuilder word=new StringBuilder();
        int i;
        for(i=0;i<temp.length;i++)
        {
            if(!Character.isLetter(temp[i])&&!Character.isDigit(temp[i])&&temp[i]!='_')
            {
                break;
            }
            word.append(temp[i]);
        }
        if(!isKeyword(word.toString())){
            System.out.print("Ident("+word.toString()+")\n");
        }
        if(i!=temp.length){
            String next=String.valueOf(temp).substring(i,temp.length);
            //System.out.println(temp);
            goNext(next);
        }

    }
    public boolean isKeyword(String str)
    {
        if(str!=null){
            if ("if".equals(str)) {
                System.out.println("If");
            } else if ("else".equals(str)) {
                System.out.println("Else");
            } else if ("while".equals(str)) {
                System.out.println("While");
            } else if ("break".equals(str)) {
                System.out.println("Break");
            } else if ("continue".equals(str)) {
                System.out.println("Continue");
            } else if ("return".equals(str)) {
                System.out.println("Return");
            } else {
                return false;
            }
        }
        return true;
    }
    public void number(String str) throws Exception {
        char[] temp=str.toCharArray();
        StringBuilder word=new StringBuilder();
        int i;
        for(i=0;i<temp.length;i++)
        {
            if(!Character.isDigit(temp[i]))
            {
                break;
            }
            word.append(temp[i]);
        }
        System.out.print("Number("+word.toString()+")\n");
        if(i!=temp.length){
            String next=String.valueOf(temp).substring(i,temp.length);
            goNext(next);
        }
    }
    public void operator(String str) throws Exception {

        if (str.charAt(0)=='=') {
            if(str.length()>1){
                if(str.charAt(1)=='='){
                    System.out.println("Eq");
                    if(str.length()>2){
                        goNext(str.substring(2));
                    }
                    return;
                }
                else {
                    System.out.println("Assign");
                    goNext(str.substring(1));
                }
                return;
            }else{
                System.out.println("Assign");
            }
        } else if (str.charAt(0)==';') {
            System.out.println("Semicolon");
        } else if (str.charAt(0)=='(') {
            System.out.println("LPar");
        } else if (str.charAt(0)==')') {
            System.out.println("RPar");
        } else if (str.charAt(0)=='{') {
            System.out.println("LBrace");
        } else if (str.charAt(0)=='}') {
            System.out.println("RBrace");
        } else if (str.charAt(0)=='+'){
            System.out.println("Plus");
        } else if (str.charAt(0)=='*') {
            System.out.println("Mult");
        } else if (str.charAt(0)=='/') {
            System.out.println("Div");
        } else if (str.charAt(0)=='<') {
            System.out.println("Lt");
        } else if (str.charAt(0)=='>') {
            System.out.println("Gt");
        }
        if(str.length()>1){
            goNext(str.substring(1));
        }
    }
}

