package com.finalwork.soulcapsule;



public class FootprintItem {



    private final String year;

    private final String dateMonthDay;

    private final String weekDay;

    private final String time;

    private final long timestamp;

    private final String moodStatus;

    private final boolean goodMood;

    private final String reason;

    private final String feelings;

    private final String content;

    private final boolean hasImage;

    private final String aiReply;



    public FootprintItem(String year, String dateMonthDay, String weekDay, String time,

                         long timestamp, String moodStatus, boolean goodMood, String reason,

                         String feelings, String content, boolean hasImage, String aiReply) {

        this.year = year;

        this.dateMonthDay = dateMonthDay;

        this.weekDay = weekDay;

        this.time = time;

        this.timestamp = timestamp;

        this.moodStatus = moodStatus;

        this.goodMood = goodMood;

        this.reason = reason;

        this.feelings = feelings;

        this.content = content;

        this.hasImage = hasImage;

        this.aiReply = aiReply;

    }



    public String getYear() {

        return year;

    }



    public String getDateMonthDay() {

        return dateMonthDay;

    }



    public String getWeekDay() {

        return weekDay;

    }



    public String getTime() {

        return time;

    }



    public long getTimestamp() {

        return timestamp;

    }



    public String getMoodStatus() {

        return moodStatus;

    }



    public boolean isGoodMood() {

        return goodMood;

    }



    public String getReason() {

        return reason;

    }



    public String getFeelings() {

        return feelings;

    }



    public String getContent() {

        return content;

    }



    public boolean hasImage() {

        return hasImage;

    }



    public String getAiReply() {

        return aiReply;

    }



    public boolean hasAiReply() {

        return aiReply != null && !aiReply.isEmpty();

    }



    public String getFullDateLabel() {

        return year.replace("年", "") + "." + dateMonthDay + " " + weekDay;

    }

}

