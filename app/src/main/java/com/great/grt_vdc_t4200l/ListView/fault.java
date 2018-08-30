package com.great.grt_vdc_t4200l.ListView;

public class fault {
    private String number;
    private String content;
    private String startTime;
    private String endTime;

    public fault(){

    }

    public fault(String number,String content,String startTime,String endTime){
        this.number = number;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getNumber() {
        return number;
    }

    public String getContent() {
        return content;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public void setEndTime(String endTime){
        this.endTime = endTime;
    }
}
