package com.example.pocketledger;

/**
 * Model class representing a diary entry
 */
public class DiaryEntry {
    private int id;
    private String title;
    private String content;
    private String mood; // 心情: happy, neutral, sad, etc.
    private String date;
    private String createdAt;

    // Constructor for creating new entry
    public DiaryEntry(String title, String content, String mood, String date) {
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.date = date;
    }

    // Constructor for reading from database
    public DiaryEntry(int id, String title, String content, String mood, String date, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.date = date;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getMood() {
        return mood;
    }

    public String getDate() {
        return date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Get mood emoji for display
    public String getMoodEmoji() {
        if (mood == null)
            return "😐";
        switch (mood) {
            case "happy":
                return "😊";
            case "excited":
                return "🎉";
            case "neutral":
                return "😐";
            case "sad":
                return "😢";
            case "angry":
                return "😠";
            case "love":
                return "❤️";
            default:
                return "😐";
        }
    }
}
