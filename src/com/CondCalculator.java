package com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/*
   a -> ==
   b -> !=
   c -> <
   d -> >
   e -> <=
   f -> >=
 */

public class CondCalculator {
    static int registerNum=1;
    public String compute(String calStr){
        //System.out.println("\n------"+calStr);
        try {
            return calBase(calStr);
        } catch (Exception e) {
            System.out.println("wrong expression:"+e);
            return "-0xFFFFFFF";
        }

    }
    public String calBase(String calStr){
        return DifficultCal(calStr);
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
        List<String> calStrArr = new ArrayList<>();

        String regex = "[a-f]";
        String[] numberArr_ = calStr.split(regex);
        numberArr.addAll(Arrays.asList(numberArr_));

        for (int i = 0; i < calStr.length(); i++) {
            if (calStr.charAt(i) == 'a' || calStr.charAt(i) == 'b' || calStr.charAt(i) == 'c' || calStr.charAt(i) == 'd'||calStr.charAt(i) == 'e'||calStr.charAt(i) == 'f'||calStr.charAt(i) == 'g') {
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
            String result=numberArr.get(0);
            if(result.charAt(1)=='l'){
                System.out.printf("\n\t%%t%d = icmp ne i32 %s,0",registerNum,result);
                result="%t"+registerNum;
                registerNum++;
            }
            return result;
        }else{
            int index = isHave_cd(calStrArr);
            if (index == -1) {
                index = 0;
            }

            String a=numberArr.get(index);
            String b=numberArr.get(index+1);
            //System.out.println("\n"+a+"_____"+b);
            String opt="";
            if ("a".equals(calStrArr.get(index))) {
                opt="eq";
                //result = numberArr.get(index)+numberArr.get(index+1);
                //System.out.printf("-------------%f + %f \n",numberArr.get(index),numberArr.get(index+1));
            }else if ("b".equals(calStrArr.get(index))) {
                opt="ne";
                //result = numberArr.get(index)-numberArr.get(index+1);
                //System.out.printf("-------------%f - %f \n",numberArr.get(index),numberArr.get(index+1));
            }else if ("c".equals(calStrArr.get(index))) {
                opt="slt";
                //result = numberArr.get(index)*numberArr.get(index+1);
                //System.out.printf("-------------%f * %f \n",numberArr.get(index),numberArr.get(index+1));
            }else if("d".equals(calStrArr.get(index))){
                opt="sgt";
                //result = (int)(numberArr.get(index)/numberArr.get(index+1));
                //System.out.printf("-------------%f / %f \n",numberArr.get(index),numberArr.get(index+1));
            }else if("e".equals(calStrArr.get(index))){
                opt="sle";
            }else if("f".equals(calStrArr.get(index))){
                opt="sge";
            }

            System.out.printf("\n\t%%t%d = icmp %s i32 %s, %s",registerNum,opt,a,b);
            modelArr.setCalStrArr(doCalCompleteForCalStrArr(calStrArr,index));
            modelArr.setNumberArr(doCalCompleteForNumberArr(numberArr,index,"%t"+registerNum));
            registerNum++;
            return baseCal(modelArr);
        }
    }

    public String DifficultCal(String calStr){
        int start = findLastLeftKH(calStr);
        int end = findNearLeftKH(calStr,start);
        if (start == -1 || end == -1) {

            ModelArr modelArr = splitCalStr(calStr);
            return baseCal(modelArr);
        }else{
            String calStr_ = calStr.substring(start+1, end);
            ModelArr modelArr = splitCalStr(calStr_);
            String result = baseCal(modelArr);
            String result__ = result+"";
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
