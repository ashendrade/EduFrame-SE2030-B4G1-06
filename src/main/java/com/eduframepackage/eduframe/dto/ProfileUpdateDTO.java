package com.eduframepackage.eduframe.dto;

public class ProfileUpdateDTO {

    private String fullName;
    private String email;
    private String currentPassword;
    private String newPassword;

    public ProfileUpdateDTO() {
    }

    public ProfileUpdateDTO(String fullName, String email, String currentPassword, String newPassword) {
        this.fullName = fullName;
        this.email = email;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
