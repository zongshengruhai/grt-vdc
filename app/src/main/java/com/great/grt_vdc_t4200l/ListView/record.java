package com.great.grt_vdc_t4200l.ListView;

public class record {

    private String number;
    private String content;
    private String hint;

    public record(){
    }

    public record(String number,String content,String hint){
        this.number = number;
        this.content = content;
        this.hint = hint;
    }

    public String getNumber(){
        return number;
    }

    public String getContent(){
        return content;
    }

    public String getHint(){
        return hint;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void setHint(String hint){
        this.hint = hint;
    }


}
