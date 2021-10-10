package com;

import java.io.*;

public class Test {
    public static void main(String[] args) {
        try {

            File file=new File(args[0]);
            BufferedReader reader=new BufferedReader(new FileReader(file));
            String temp;
            while((temp=reader.readLine())!=null) {
                //System.out.print("------"+temp+"------\n");
                Analysis.analysis.analyze(temp);
            }
            //System.out.println("Over!!!!!!!!!");
            reader.close();
        }catch (IOException e){
            System.out.println(e);
        }catch (Exception e1){

        }


    }
}
