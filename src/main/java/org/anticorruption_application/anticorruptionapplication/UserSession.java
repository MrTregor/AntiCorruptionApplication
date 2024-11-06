package org.anticorruption_application.anticorruptionapplication;

import java.util.List;

public class UserSession {
    private static UserSession instance;
    private String token;
    private List<String> groups;
    private String username;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void clear() {
        token = null;
        groups = null;
        username = null;
    }

    public boolean hasGroup(String group) {
        return groups != null && groups.contains(group);
    }
}