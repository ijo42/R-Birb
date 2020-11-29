package ru.ijo42.rbirb.tgbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Predicate;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private int id;
    private String name;
    private String token;
    private State state;

    public User(int id) {
        this.id = id;
        this.name = String.valueOf(id);
        this.token = "";
        this.state = State.listen;
    }

    public enum State {
        listen, awaitPhoto("/upload", (x -> x.getMessage().hasPhoto() || x.getMessage().hasAnimation()));
        public String cmd;
        public Predicate<Update> predicate;

        State(String cmd, Predicate<Update> predicate) {
            this.cmd = cmd;
            this.predicate = predicate;
        }

        State() {
        }
    }
}
