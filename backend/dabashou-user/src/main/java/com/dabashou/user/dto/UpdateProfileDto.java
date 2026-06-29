package com.dabashou.user.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新个人信息DTO
 */
public class UpdateProfileDto {
    @Size(max = 50) private String nickname;
    private String avatar;
    @Size(max = 500) private String bio;
    private String campus;
    private String building;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
}
