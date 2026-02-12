package com.work.domain.dto;

/**DTO обогащенного сообщения.*/
public class MessageDto extends RawMessageDto {
    /**Логин отправителя.*/
    public String fromLogin;
    /**Логин адресата.*/
    public String toLogin;

    public MessageDto() {
        super();
    }

    public MessageDto(RawMessageDto rawMessageDto, String fromLogin, String toLogin) {
        super(rawMessageDto.fromUserId, rawMessageDto.toUserId, rawMessageDto.text);
        this.fromLogin = fromLogin;
        this.toLogin = toLogin;
    }
}
