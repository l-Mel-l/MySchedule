package com.example.myschedule;

public class Schedule {
    private String lessonName;
    private String startTime;
    private String endTime;
    private String roomNumber;
    private String weekday;
    private String weekName;
    private String perStartTime;
    private String perEndTime;
    private String Scheduleid;

    public Schedule() {
    }

    public Schedule(String lessonName, String startTime, String endTime, String roomNumber, String weekday, String weekName, String perStartTime, String perEndTime) {
        this.lessonName = lessonName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomNumber = roomNumber;
        this.weekday = weekday;
        this.weekName = weekName;
        this.perStartTime = perStartTime;
        this.perEndTime = perEndTime;
        this.Scheduleid = Scheduleid;
    }

    public String getLessonName() {
        return lessonName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getWeekday() {
        return weekday;
    }

    public String getWeekName() {
        return weekName;
    }

    public String getPerStartTime() {
        return perStartTime;
    }

    public String getPerEndTime() {
        return perEndTime;
    }
    public String getScheduleid() {
        return Scheduleid;
    }
}


