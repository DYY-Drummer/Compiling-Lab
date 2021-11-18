package com;

import java.math.BigInteger;
import java.util.ArrayList;

public class Tokenizer {
    public static ArrayList<Token> token_list=new ArrayList<>();
    String[] opt={"=",";","(",")","{","}","+","-","*","/", "%","<",">",","};
    boolean isComment=false;
    boolean nextLine;
    public void analyze(String str) throws Exception {
        if(isComment){
            char[] temp=str.toCharArray();
            for(int i=0;i<temp.length-1;i++){
                if(temp[i]=='*'&&temp[i+1]=='/'){
                    isComment=false;
                    if(i+1<temp.length-1) {
                        str = str.substring(i + 2);
                        break;
                    }
                    else
                        return;
                }
            }
        }
        if(isComment)
            return;
        String[] word =str.split("\\s+");
        nextLine=false;
        for(int i=0;i<word.length;i++) {
            if(nextLine)
                break;
            if(isComment){
                char[] temp=word[i].toCharArray();
                for(int j=0;j<temp.length-1;j++){
                    if(temp[j]=='*'&&temp[j+1]=='/'){
                        isComment=false;
                        if(j+1<temp.length-1) {
                            word[i] = word[i].substring(j + 2);
                            break;
                        }
                    }
                }
            }
            if(isComment)
                continue;
            //System.out.println(word[i]);
            goNext(word[i]);
            //System.out.print("------"+word[i]+"------\n");
        }
    }

    public void goNext(String next) throws Exception {
        if(next.equals("")){
            return;
        }
        if(isComment){
            char[] temp=next.toCharArray();
            for(int i=0;i<temp.length-1;i++){
                if(temp[i]=='*'&&temp[i+1]=='/'){
                    isComment=false;
                    if(i+1<temp.length-1) {
                        next = next.substring(i + 2);
                        break;
                    }
                    else{
                        return;
                    }
                }
            }
        }
        if (isComment)
            return;
        if(next.length()>1&&next.charAt(0)=='/'&&next.charAt(1)=='/'){
            nextLine=true;
            return;
        }

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
            throw new Exception("Wrong input in goNext()");
        }
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
            //System.out.print("Ident("+word.toString()+")\n");
            token_list.add(new Token("Ident",word.toString()));
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
            } else if ("return".equals(str)) {
                token_list.add(new Token("Keyword","return"));
            } else if("int".equals(str)) {
                token_list.add(new Token("Keyword","int"));
            } else if("const".equals(str)) {
                token_list.add(new Token("Keyword","const"));
            } else {
                return false;
            }
        }
        return true;
    }
    public boolean isOpt(String c)
    {
        for(int i=0;i< opt.length;i++)
            if(opt[i].equals(c))
                return true;
        return false;
    }
    public void number(String str) throws Exception {
        if(str.charAt(0)=='0'){
            if(str.length()>1&&(str.charAt(1)=='x'||str.charAt(1)=='X')){
                hexadecimal(str);
            }
            else {
                octal(str);
            }
        }
        else{
            decimal(str);
        }

    }
    public void decimal(String str) throws Exception{
        String num=cutNumber(str);
        token_list.add(new Token("Num",num.toString()));
        if(num.length()<str.length()){
            String next=str.substring(num.length());
            goNext(next);
        }
    }
    public int hex16To10(String hex){
        BigInteger intNum=new BigInteger(hex,16);
        return intNum.intValue();
    }
    public void hexadecimal(String str) throws Exception{
        if(str.length()<3){
            throw new Exception("hex only has '0x'");
        }
        char[] temp=str.toCharArray();
        StringBuilder word=new StringBuilder();
        //word.append(str, 0, 2);
        int i;
        for(i=2;i<temp.length;i++)
        {
            if(!Character.isDigit(temp[i])&&!((temp[i]<='f'&&temp[i]>='a')||(temp[i]<='F'&&temp[i]>='A')))
            {
                break;
            }
            word.append(temp[i]);
        }
        token_list.add(new Token("Num",hex16To10(word.toString())+""));
        if(i!=temp.length){
            String next=str.substring(i);
            goNext(next);
        }
    }
    public int oct8To10(String oct){
        if(oct.length()>1)
            oct=oct.substring(1);
        BigInteger intNum=new BigInteger(oct,8);
        return intNum.intValue();
    }
    public void octal(String str) throws Exception{
        String num=cutNumber(str);
        token_list.add(new Token("Num",oct8To10(num)+""));
        if(num.length()<str.length()){
            String next=str.substring(num.length());
            goNext(next);
        }
    }
    public String cutNumber(String str){
        char[] temp=str.toCharArray();
        StringBuilder word=new StringBuilder();
        for(int i=0;i<temp.length;i++)
        {
            if(!Character.isDigit(temp[i]))
            {
                break;
            }
            word.append(temp[i]);
        }
        return word.toString();
    }
    public void operator(String str) throws Exception {

        if (str.charAt(0)=='=') {
            token_list.add(new Token("=","="));
            /*if(str.length()>1){
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
            }*/
        } else if (str.charAt(0)==';') {
            token_list.add(new Token(";",";"));
        } else if (str.charAt(0)=='(') {
            token_list.add(new Token("LPar","("));
        } else if (str.charAt(0)==')') {
            token_list.add(new Token("RPar",")"));
        } else if (str.charAt(0)=='{') {
            token_list.add(new Token("LBrace","{"));
        } else if (str.charAt(0)=='}') {
            token_list.add(new Token("RBrace","}"));
        } else if (str.charAt(0)=='+'){
            token_list.add(new Token("UnaryOp","+"));
        } else if (str.charAt(0)=='-'){
            token_list.add(new Token("UnaryOp","-"));
        } else if (str.charAt(0)=='*') {
            if(str.length()>1&&str.charAt(1)=='/'){
                isComment=false;
                //token_list.add(new Token("RComment","*/"));
                if(str.length()>2){
                    goNext(str.substring(2));
                }
                return;
            }else {
                token_list.add(new Token("Mult", "*"));
            }
        } else if (str.charAt(0)=='/') {
            if(str.length()>1&&str.charAt(1)=='*'){
                isComment=true;
                //token_list.add(new Token("LComment","/*"));
                if(str.length()>2){
                    goNext(str.substring(2));
                }
                return;
            }
            else if(str.length()>1&&str.charAt(1)=='/'){
                nextLine=true;
                return;
            }
            else {
                token_list.add(new Token("Div", "/"));
            }
        } else if(str.charAt(0)=='%'){
          token_list.add(new Token("%","%"));
        } else if(str.charAt(0)==','){
            token_list.add(new Token(",",","));
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
