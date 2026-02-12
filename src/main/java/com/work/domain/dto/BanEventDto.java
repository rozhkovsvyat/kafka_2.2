package com.work.domain.dto;

/**DTO события стоп-листа.*/
public class BanEventDto {
    /**Идентификатор владельца стоп-листа (ключ).*/
    public String ownerUserId;
    /**Идентификатор заблокированного адресата (ключ).*/
    public String bannedUserId;
    /**Событие разбана?*/
    public boolean isUnban;

    public BanEventDto() {}
    public BanEventDto(String ownerUserId, String bannedUserId, boolean isUnbanned) {
        this.ownerUserId = ownerUserId;
        this.bannedUserId = bannedUserId;
        this.isUnban = isUnbanned;
    }
}
