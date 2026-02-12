package com.work.domain.constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**Данные.*/
public final class Data {
    /**Справочник пользователей (key: id, value: логин).*/
    public static final Map<String, String> USER_MAP = new HashMap<>() {{
        put(UUID.randomUUID().toString(), "alice223");
        put(UUID.randomUUID().toString(), "charlie.love");
        put(UUID.randomUUID().toString(), "99_kevin_99");
        put(UUID.randomUUID().toString(), "$julia-jet");
        put(UUID.randomUUID().toString(), "_m.argo");
    }};
    
    /**Запрещенные слова.*/
    public static final List<String> PROHIBITED_WORDS = Arrays.asList(
        "idiot",
        "stupid",
        "foolish",
        "jerk",
        "dummy");

    /**Сообщения.*/
    public static final List<String> MESSAGES = Arrays.asList(
        "Hello there!",
        "You are an idiot",
        "Have a nice day)",
        "My boss is jerk",
        "Kafka is cool",
        "this is stupid", 
        "What an excellent coffee!",
        "DUMM.Y! DUMMY! DU[MMY]!",
        "i love java!",
        "foolish practicum.. ^^");
}
