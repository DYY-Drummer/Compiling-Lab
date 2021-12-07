package com;

import java.io.*;

public class Test {
    public static void main(String[] args) {
        try {

            File input=new File(args[0]);
            File output=new File(args[1]);
            BufferedReader reader=new BufferedReader(new FileReader(input));
            PrintStream outputStream=new PrintStream(output);
            //System.setOut(outputStream);
            System.out.println("declare i32 @getch()\n" +
                    "declare void @putch(i32)\n"+"declare void @putint(i32)\n"+"declare i32 @getint()\n");
            Tokenizer tokenizer=new Tokenizer();

            String temp;

            //separate token
            while((temp=reader.readLine())!=null) {
                System.out.print("  "+temp+"\n");
                tokenizer.analyze(temp);
            }
            tokenizer.token_list.add(new Token("END","#"));
//            for(int i=0;i<Analysis.token_list.size();i++)
//                System.out.println(Analysis.token_list.get(i).word);
            Parser parser=new Parser();
            parser.CompUnit();
            //close file
            reader.close();
        }catch (IOException e){
            System.out.println(e);
        }catch (Exception e1){
            System.out.println(e1);
            System.exit(1);
        }


    }
}
