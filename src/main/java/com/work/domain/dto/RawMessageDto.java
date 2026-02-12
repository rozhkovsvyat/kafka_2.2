package com.work.domain.dto;

/**DTO сообщения.*/
public class RawMessageDto {
    /**Идентификатор отправителя.*/
    public String fromUserId;
    /**Идентификатор адресата.*/
    public String toUserId;
    /**Текст.*/
    public String text;

    public RawMessageDto() {}  
    public RawMessageDto(String fromUserId, String toUserId, String text) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.text = text;
    }
}
