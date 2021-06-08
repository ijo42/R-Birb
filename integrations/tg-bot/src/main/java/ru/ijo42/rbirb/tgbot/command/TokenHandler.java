package ru.ijo42.rbirb.tgbot.command;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.ijo42.rbirb.tgbot.UpdateReceiver;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.User;

import java.util.List;

@Component
@BotCommand(command = "/token", message = "Set your token")
public class TokenHandler extends AbstractBaseHandler {
    @Override
    public List<BotApiMethod<Message>> handleStateless(User user, long chatId, String message) {
        String token = UpdateReceiver.extractMessage(message);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = null;
        try {
            result = restTemplate.exchange(restEndpoint + "/moderate/check", HttpMethod.GET, entity, String.class);
        } catch (Exception ignored) {
        }
        if (result == null || result.getStatusCode() != HttpStatus.OK)
            return List.of(MessageBuilder.create(chatId)
                    .line("*%s* not a valid token", token)
                    .build());
        userService.updateToken(user, token);

        return List.of(MessageBuilder.create(chatId)
                .line("Your token is *%s*", userService.getToken(user.getId()))
                .build());
    }

}
