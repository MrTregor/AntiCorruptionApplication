package org.anticorruption.application.Models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessGroup {
    private Long id;
    private String name;

    public AccessGroup(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    public AccessGroup() {
    }
    // Геттеры и сеттеры
}
