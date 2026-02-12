package com.work.domain.constants;

/**Топики.*/
public final class Topics {
    /**Запрещенные слова.*/
    public static final String PROHIBITED_WORD = System.getenv().getOrDefault("TOPIC_PROHIBITED_WORD", "prohibited_word");
    /**Входящий поток сообщений.*/
    public static final String RAW_MESSAGE = System.getenv().getOrDefault("TOPIC_RAW_MESSAGE", "raw_message");
    /**Финальный поток сообщений.*/
    public static final String MESSAGE = System.getenv().getOrDefault("TOPIC_MESSAGE", "message");
    /**Пользователи.*/
    public static final String USER = System.getenv().getOrDefault("TOPIC_USER", "user");
    /**Блокировки.*/
    public static final String BAN = System.getenv().getOrDefault("TOPIC_BAN", "ban");
}
