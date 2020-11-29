package ru.ijo42.rbirb.tgbot.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.UpdateReceiver;
import ru.ijo42.rbirb.tgbot.model.User;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;

import java.util.List;

@Component
@BotCommand(command = "/token", message = "Set your token")
public class TokenHandler extends AbstractBaseHandler {
    @Override
    public List<BotApiMethod<Message>> handle(User user, long chatId, String message) {

        String token = UpdateReceiver.extractMessage(message);
        userService.updateToken(user, token);

        return List.of(MessageBuilder.create(chatId)
                .line("Your token is *%s*", userService.getToken(user.getId()))
                .build());
    }

}
