package com.work.domain.models;

import java.util.Set;

/**Модель стоп-листа.*/
public class BanModel {
    /**Идентификаторы заблокированных адресатов.*/
    public Set<String> bannedUserIds;

    public BanModel() {}
    public BanModel(Set<String> bannedUserIds) {
        this.bannedUserIds = bannedUserIds;
    }
}
