package com;

import java.io.*;

public class Test {
    public static void main(String[] args) {
        try {

            File input=new File(args[0]);
            File output=new File(args[1]);
            BufferedReader reader=new BufferedReader(new FileReader(input));
            PrintStream outputStream=new PrintStream(output);
            System.setOut(outputStream);
            Analysis analysis=new Analysis();
            String temp;
            //separate token
            while((temp=reader.readLine())!=null) {
                //System.out.print("------"+temp+"------\n");
                analysis.analyze(temp);
            }
            Analysis.token_list.add(new Token("END","#"));
//            for(int i=0;i<Analysis.token_list.size();i++){
//                System.out.printf("%s  %s\n",Analysis.token_list.get(i).id,Analysis.token_list.get(i).word);
//            }
            //parser
            analysis.CompUnit();
            //close file
            reader.close();
        }catch (IOException e){
            System.out.println(e);
        }catch (Exception e1){
            System.out.println(e1);
        }


    }
}
