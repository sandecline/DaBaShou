package com.dabashou.user.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新个人资料DTO
 */
public class UpdateProfileDto {

    @Size(max = 50, message = "昵称最长50")
    private String nickname;

    @Size(max = 255, message = "头像URL最长255")
    private String avatar;

    @Size(max = 100, message = "邮箱最长100")
    private String email;

    @Size(max = 50, message = "校区最长50")
    private String campus;

    @Size(max = 50, message = "楼栋最长50")
    private String building;

    @Size(max = 500, message = "个人简介最长500")
    private String bio;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
