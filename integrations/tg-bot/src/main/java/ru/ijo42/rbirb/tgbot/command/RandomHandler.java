package ru.ijo42.rbirb.tgbot.command;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.PhotoDTO;
import ru.ijo42.rbirb.tgbot.model.User;

import java.util.List;

@Component
@BotCommand(command = "/random", message = "Gets random photo")
public class RandomHandler extends AbstractBaseHandler {

    @SneakyThrows
    @Override
    protected List<BotApiMethod<Message>> handleStateless(User user, long chatId, String message) {
        String result = restTemplate.getForObject(restEndpoint + "/random/info", String.class);
        PhotoDTO photoDTO = mapper
                .readValue(result, PhotoDTO.class);

        return List.of(MessageBuilder.create(chatId)
                .line(restEndpoint + "/" + photoDTO.getId())
                .build());
    }
}
