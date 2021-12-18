package com;

import java.util.ArrayList;
import java.util.List;

public class Calculator {
    int registerNum;
    public String compute(String calStr){
        //System.out.println("------"+calStr);
        calStr=removeSerialSign(calStr);
        calStr=addPar(calStr);
        //System.out.println("------"+calStr);
        registerNum=Parser.registerNum_temp;
        try {

            String result=calBase(calStr);
            Parser.registerNum_temp=registerNum;
            return result;
        } catch (Exception e) {
            System.out.println("wrong expression:"+e);
            return "-0xFFFFFFF";
        }

    }
    public String addPar(String str){
        StringBuilder calStr=new StringBuilder(str);

            for (int i = 0; i < calStr.length(); i++) {
                if ((calStr.charAt(i) == '-' || calStr.charAt(i) == '+') && Character.isDigit(calStr.charAt(i + 1))) {
                    if (i != 0 && (calStr.charAt(i - 1) == '*' || calStr.charAt(i - 1) == '/' || calStr.charAt(i - 1) == '%')) {
                        calStr.insert(i, '(');
                        i += 2;
                        while (i<calStr.length()&&Character.isDigit(calStr.charAt(i))) {
                            i++;
                        }
                        calStr.insert(i, ')');
                    }
                }
            }

        return calStr.toString();
    }
    public String removeSerialSign(String str){
        StringBuilder calStr=new StringBuilder("");
        for(int i=0;i<str.length();i++){
            if(str.charAt(i)=='-'||str.charAt(i)=='+'){
                int flag=str.charAt(i)=='+'?1:-1;
                while(i<str.length()-1&&(str.charAt(i+1)=='+'||str.charAt(i+1)=='-')){
                    if(str.charAt(i+1)=='-'){
                        flag=-flag;
                    }
                    i++;
                }
                calStr.append(flag==1?'+':'-');
                continue;
            }
            calStr.append(str.charAt(i));
        }
        return calStr.toString();
    }
    public String calBase(String calStr){
        return DifficultCal(calStr);
    }
    public String replace(String calStr){
        calStr = calStr.replace("+", "a");
        calStr = calStr.replace("-", "b");
        calStr = calStr.replace("*", "c");
        calStr = calStr.replace("/", "d");
        calStr = calStr.replace("%", "e");
        return calStr;
    }

     class ModelArr {
        private List<String> numberArr;
        private List<String> calStrArr;
        public List<String> getNumberArr() {
            return numberArr;
        }
        public void setNumberArr(List<String> numberArr) {
            this.numberArr = numberArr;
        }
        public List<String> getCalStrArr() {
            return calStrArr;
        }
        public void setCalStrArr(List<String> calStrArr) {
            this.calStrArr = calStrArr;
        }
    }

    public ModelArr splitCalStr(String calStr){
        ModelArr modelArr = new ModelArr();
        List<String> numberArr = new ArrayList<>();
        List<String> calStrArr = new ArrayList<String>();
        if (calStr.charAt(0) == 'a' || calStr.charAt(0) == 'b') {
            calStr = "0"+calStr;
        }
        String regex = "[a-e]";
        String[] numberArr_ = calStr.split(regex);
        for (int i = 0; i < numberArr_.length; i++) {

//            if (numberArr_[i].startsWith("@")) {
//                numberArr.add(0-Double.parseDouble(numberArr_[i].substring(1)));
//                continue;
//            }
            numberArr.add(numberArr_[i]);
        }

        for (int i = 0; i < calStr.length(); i++) {
            if (calStr.charAt(i) == 'a' || calStr.charAt(i) == 'b' || calStr.charAt(i) == 'c' || calStr.charAt(i) == 'd'||calStr.charAt(i) == 'e') {
                calStrArr.add(""+calStr.charAt(i));
            }
        }
        modelArr.setNumberArr(numberArr);
        modelArr.setCalStrArr(calStrArr);

        return modelArr;
    }


    public String baseCal(ModelArr modelArr){
        List<String> calStrArr = modelArr.getCalStrArr();
        List<String> numberArr = modelArr.getNumberArr();
        if ((calStrArr == null || calStrArr.size() == 0) && numberArr.size() == 1) {

            return numberArr.get(0);
        }else{
            String result = "unCal";
            int index = isHave_cd(calStrArr);
            if (index == -1) {
                index = 0;
            }
            String opt="";
            if ("a".equals(calStrArr.get(index))) {
                opt="add";
                //result = numberArr.get(index)+numberArr.get(index+1);
                //System.out.printf("-------------%f + %f \n",numberArr.get(index),numberArr.get(index+1));
            }else if ("b".equals(calStrArr.get(index))) {
                opt="sub";
                //result = numberArr.get(index)-numberArr.get(index+1);
                //System.out.printf("-------------%f - %f \n",numberArr.get(index),numberArr.get(index+1));
            }else if ("c".equals(calStrArr.get(index))) {
                opt="mul";
                //result = numberArr.get(index)*numberArr.get(index+1);
                //System.out.printf("-------------%f * %f \n",numberArr.get(index),numberArr.get(index+1));
            }else if("d".equals(calStrArr.get(index))){
                opt="sdiv";
                //result = (int)(numberArr.get(index)/numberArr.get(index+1));
                //System.out.printf("-------------%f / %f \n",numberArr.get(index),numberArr.get(index+1));
            }else if("e".equals(calStrArr.get(index))){
                opt="srem";
            }
            String a=numberArr.get(index);
            String b=numberArr.get(index+1);
            if(a.startsWith("v")){
                System.out.printf("\n\t%%t%d = load i32, i32* %%l%s",registerNum,a.substring(1));
                a="%t"+registerNum;
                registerNum++;
            } else if(a.startsWith("t")){
                a="%t"+a.substring(1);
            }
            if(b.startsWith("v")){
                System.out.printf("\n\t%%t%d = load i32, i32* %%l%s",registerNum,b.substring(1));
                b="%t"+registerNum;
                registerNum++;
            } else if(b.startsWith("t")){
                b="%t"+b.substring(1);
            }


            //System.out.printf("\n\t%%t%d = alloca i32",registerNum);
            System.out.printf("\n\t%%t%d = %s i32 %s, %s",registerNum,opt,a,b);
            modelArr.setCalStrArr(doCalCompleteForCalStrArr(calStrArr,index));
            modelArr.setNumberArr(doCalCompleteForNumberArr(numberArr,index,"t"+registerNum));
            registerNum++;
            return baseCal(modelArr);
        }
    }

    public String DifficultCal(String calStr){
        int start = findLastLeftKH(calStr);
        int end = findNearLeftKH(calStr,start);
        if (start == -1 || end == -1) {
            calStr = replace(calStr);
            ModelArr modelArr = splitCalStr(calStr);
            return baseCal(modelArr);
        }else{
            String calStr_ = calStr.substring(start+1, end);
            calStr_ = replace(calStr_);
            ModelArr modelArr = splitCalStr(calStr_);
            String result = baseCal(modelArr);
            String result__ = result+"";
            //String result_ = result>0?(result+""):("@"+(result__.substring(1)));
            calStr = calStr.substring(0,start)+result__+calStr.substring(end+1);
            return DifficultCal(calStr);
        }
    }

    public int isHave_cd(List<String> calStrArr){
        for (int i = 0; i < calStrArr.size(); i++) {
            if ("c".equals(calStrArr.get(i)) || "d".equals(calStrArr.get(i))|| "e".equals(calStrArr.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public List<String> doCalCompleteForCalStrArr(List<String> calStrArr,int index){

        calStrArr.remove(index);

        return calStrArr;
    }

    public List<String> doCalCompleteForNumberArr(List<String> numberArr,int index,String result){
        numberArr.set(index, result);
        numberArr.remove(index+1);
        return numberArr;
    }

    public int findLastLeftKH(String calStr){
        return calStr.lastIndexOf("(");
    }

    public int findNearLeftKH(String calStr,int leftIndex){
        return calStr.indexOf(")", leftIndex);
    }
}
