package ru.ijo42.rbirb.tgbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ijo42.rbirb.tgbot.annotations.RequiredToken;
import ru.ijo42.rbirb.tgbot.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Slf4j
public class UserService {
    List<User> users = new ArrayList<>();

    public User getOrCreate(int userId) {
        Optional<User> possibleUser = users.stream().filter(u->u.getId() == userId).findFirst();

        return possibleUser.orElseGet(()->{
            User newUser = new User(userId);
            users.add(newUser);
            return newUser;
        });
    }

    public void updateToken(User user, String token) {
        users.stream().filter(u->u.getId() == user.getId()).forEach(u->u.setToken(token));
    }

    public final boolean isAuthorized(Class<?> clazz, User user) {
        log.debug("Authorizing {} to use {}", user, clazz.getSimpleName());
        final boolean required = Stream.of(clazz.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(RequiredToken.class));
        if(required) {
            log.debug("Required token");
            return user.getToken() != null;
        }
        else
            return true;
    }

    public String getToken(int userId) {
        return users.stream().filter(u->u.getId() == userId).map(User::getToken).findFirst().orElse("null");
    }
}
