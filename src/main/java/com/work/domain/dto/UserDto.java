package com.work.domain.dto;

/**DTO пользователя.*/
public class UserDto {
    /**Идентификатор.*/
    public String id;
    /**Логин.*/
    public String login;

    public UserDto() {}
    public UserDto(String id, String login) {
        this.id = id;
        this.login = login;
    }
}
