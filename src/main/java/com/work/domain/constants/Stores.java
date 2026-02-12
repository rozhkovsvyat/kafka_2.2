package com.work.domain.constants;

/**Хранилища.*/
public final class Stores {
    /**Запрещенные слова.*/
    public static final String PROHIBITED_WORD = System.getenv().getOrDefault("STORE_PROHIBITED_WORD", "prohibited-word-store");
    /**Пользователи.*/
    public static final String USER = System.getenv().getOrDefault("STORE_USER", "user-store");
    /**Блокировки.*/
    public static final String BAN = System.getenv().getOrDefault("STORE_BAN", "ban-store");
}
