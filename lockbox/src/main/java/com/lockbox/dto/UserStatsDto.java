package com.lockbox.dto;

public class UserStatsDto {
    private long passwords;
    private long secureNotes;
    private long categories;
    private long tags;

    public UserStatsDto() {
    }

    public UserStatsDto(long passwords, long secureNotes, long categories, long tags) {
        this.passwords = passwords;
        this.secureNotes = secureNotes;
        this.categories = categories;
        this.tags = tags;
    }

    public long getPasswords() {
        return passwords;
    }

    public void setPasswords(long passwords) {
        this.passwords = passwords;
    }

    public long getSecureNotes() {
        return secureNotes;
    }

    public void setSecureNotes(long secureNotes) {
        this.secureNotes = secureNotes;
    }

    public long getCategories() {
        return categories;
    }

    public void setCategories(long categories) {
        this.categories = categories;
    }

    public long getTags() {
        return tags;
    }

    public void setTags(long tags) {
        this.tags = tags;
    }
} 