package com;

import java.util.ArrayList;
import java.util.List;

public class Calculator {
    public int compute(String calStr){
        calStr=removeSerialSign(calStr);
        try {
            return (int)calBase(calStr);
        } catch (Exception e) {
            System.out.println("wrong expression:"+e);
            return -0xFFFFFFF;
        }

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
    public double calBase(String calStr){
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
        private List<Double> numberArr;
        private List<String> calStrArr;
        public List<Double> getNumberArr() {
            return numberArr;
        }
        public void setNumberArr(List<Double> numberArr) {
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
        List<Double> numberArr = new ArrayList<>();
        List<String> calStrArr = new ArrayList<String>();
        if (calStr.charAt(0) == 'a' || calStr.charAt(0) == 'b') {
            calStr = "0"+calStr;
        }
        String regex = "[a-d]";
        String[] numberArr_ = calStr.split(regex);
        for (int i = 0; i < numberArr_.length; i++) {

            if (numberArr_[i].startsWith("@")) {
                numberArr.add(0-Double.parseDouble(numberArr_[i].substring(1)));
                continue;
            }
            numberArr.add(Double.parseDouble(numberArr_[i]));
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


    public double baseCal(ModelArr modelArr){
        List<String> calStrArr = modelArr.getCalStrArr();
        List<Double> numberArr = modelArr.getNumberArr();
        if ((calStrArr == null || calStrArr.size() == 0) && numberArr.size() == 1) {
            return numberArr.get(0);
        }else{
            double result = Double.MIN_VALUE;
            int index = isHave_cd(calStrArr);
            if (index == -1) {
                index = 0;
            }
            if ("a".equals(calStrArr.get(index))) {
                result = numberArr.get(index)+numberArr.get(index+1);
            }else if ("b".equals(calStrArr.get(index))) {
                result = numberArr.get(index)-numberArr.get(index+1);
            }else if ("c".equals(calStrArr.get(index))) {
                result = numberArr.get(index)*numberArr.get(index+1);
            }else if("d".equals(calStrArr.get(index))){
                result = numberArr.get(index)/numberArr.get(index+1);
            }else if("e".equals(calStrArr.get(index))){
                result = numberArr.get(index)%numberArr.get(index+1);
            }
            modelArr.setCalStrArr(doCalCompleteForCalStrArr(calStrArr,index));
            modelArr.setNumberArr(doCalCompleteForNumberArr(numberArr,index,result));
            return baseCal(modelArr);
        }
    }

    public double DifficultCal(String calStr){
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
            double result = baseCal(modelArr);
            String result__ = result+""; //此处得到 -2
            String result_ = result>0?(result+""):("@"+(result__.substring(1)));
            calStr = calStr.substring(0,start)+result_+calStr.substring(end+1);
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

    public List<Double> doCalCompleteForNumberArr(List<Double> numberArr,int index,Double result){
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
