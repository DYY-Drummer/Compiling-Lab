package com;

public class Variable {
    String name;
    boolean isConst;
    boolean assigned=false;
    int reg;
    int constValue;
    public Variable(String name,boolean isConst,int reg){
        this.name=name;
        this.isConst=isConst;
        this.reg=reg;
    }
}
