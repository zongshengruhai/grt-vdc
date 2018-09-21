package com.great.grt_vdc_t4200l.ListView;

public class shortItem {

    private String number;
    private String content;
    private String hint;
    private String url;

    public shortItem(){
    }

    public shortItem(String number, String content, String hint, String url){
        this.number = number;
        this.content = content;
        this.hint = hint;
        this.url = url;
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

    public String getUrl(){
        return url;
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

    public void setUrl(String url){this.url = url;}


}
