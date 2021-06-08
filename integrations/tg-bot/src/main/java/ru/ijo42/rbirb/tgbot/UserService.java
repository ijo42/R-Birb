package ru.ijo42.rbirb.tgbot;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;
import ru.ijo42.rbirb.tgbot.annotations.RequiredToken;
import ru.ijo42.rbirb.tgbot.command.AbstractBaseHandler;
import ru.ijo42.rbirb.tgbot.model.User;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class UserService {
    final List<User> users = new ArrayList<>();

    public User get(int userId) {
        return users.stream().filter(u -> u.getId() == userId).findFirst().orElseGet(() -> {
            User newUser = new User(userId);
            users.add(newUser);
            return newUser;
        });
    }

    public User get(User user) {
        return get(user.getId());
    }

    public void updateToken(User user, String token) {
        get(user).setToken(token);
    }

    public final boolean isAuthorized(Class<? extends AbstractBaseHandler> clazz, User user) {
        log.debug("Authorizing {} to use {}", user, clazz.getSimpleName());
        final boolean required = clazz.isAnnotationPresent(RequiredToken.class);
        if (required) {
            log.debug("Required token");
            return !user.getToken().isBlank();
        }
        return true;
    }

    @NonNull
    public String getToken(int userId) {
        String t = get(userId).getToken();
        if (t == null)
            throw new RuntimeException("NULL token for " + userId);
        return t;
    }

    public void changeState(User user, User.State state) {
        get(user).setState(state);
    }
}
