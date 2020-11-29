package ru.ijo42.rbirb.tgbot.command;

import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.ijo42.rbirb.tgbot.UpdateReceiver;
import ru.ijo42.rbirb.tgbot.model.User;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;
import ru.ijo42.rbirb.tgbot.annotations.RequiredToken;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.StagingDTO;

import java.util.List;

@Component
@RequiredToken
@BotCommand(command = "/infostage", message = "Returns staging by id")
public class StagingInfoHandler extends AbstractBaseHandler {
    @SuppressWarnings("ConstantConditions")
    @Override
    protected List<BotApiMethod<Message>> handle(User user, long chatId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, userService.getToken(user.getId()));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String arg = UpdateReceiver.extractMessage(message);
        ResponseEntity<StagingDTO> result = restTemplate.exchange(restEndpoint + "/staging/" + arg + "/info", HttpMethod.GET, entity, StagingDTO.class);

        return List.of(MessageBuilder.create(chatId).line(result.getBody().toString()).build());
    }
}
