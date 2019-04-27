package com.seeks.utils.orc;

public class OcrText {
    public String text;
    public int x;
    public int y;
    public OcrText(String text,int x,int y){
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public String toXString() {
        return this.x+","+this.y+":"+this.text;
    }
}
