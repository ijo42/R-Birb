package ru.ijo42.rbirb.tgbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private int id;
    private String name;
    private String token;

    public User(int id) {
        this.id = id;
        this.name = String.valueOf(id);
        this.token = "";
    }

}
