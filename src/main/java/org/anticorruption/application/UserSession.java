package org.anticorruption.application;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class UserSession {
    private static UserSession instance;
    private String token;
    private List<String> groups;
    private String username;

    private UserSession() {
        groups = new ArrayList<>();
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void clear() {
        token = null;
        groups.clear(); // Очищаем список, а не присваиваем null
        username = null;
    }

    public boolean hasGroup(String group) {
        return groups.contains(group);
    }
}