package com.finalwork.soulcapsule.dto;

public class MoodRequest {

    private Long id;
    private Long userId;
    private String moodType;
    private String content;
    private String tags;
    private String imageUrl;

    public MoodRequest() {
    }

    public MoodRequest(Long userId, String moodType, String content) {
        this(userId, moodType, content, null, null);
    }

    public MoodRequest(Long userId, String moodType, String content, String imageUrl) {
        this(null, userId, moodType, content, null, imageUrl);
    }

    public MoodRequest(Long id, Long userId, String moodType, String content, String tags, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.moodType = moodType;
        this.content = content;
        this.tags = tags;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMoodType() {
        return moodType;
    }

    public void setMoodType(String moodType) {
        this.moodType = moodType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
