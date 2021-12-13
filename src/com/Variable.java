package com;

import java.util.ArrayList;

public class Variable {
    String name;
    boolean isConst;
    boolean assigned=false;
    int reg;
    int constValue;
    ArrayList<Integer> dim;
    public Variable(String name,boolean isConst,int reg){
        this.name=name;
        this.isConst=isConst;
        this.reg=reg;
    }
}
