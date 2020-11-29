package ru.ijo42.rbirb.tgbot.command;

import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.SneakyThrows;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.ijo42.rbirb.tgbot.model.User;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;
import ru.ijo42.rbirb.tgbot.annotations.RequiredToken;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.StagingDTO;
import ru.ijo42.rbirb.tgbot.model.Status;

import java.util.List;

@Component
@RequiredToken
@BotCommand(command = "/list", message = "Returns staging list")
public class StagingListHandler extends AbstractBaseHandler {
    @SneakyThrows
    @Override
    protected List<BotApiMethod<Message>> handle(User user, long chatId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, userService.getToken(user.getId()));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(restEndpoint + "/staging", HttpMethod.GET, entity, String.class);

        /*if(result.getStatusCode() != HttpStatus.OK)
            return List.of(MessageBuilder.create(chatId).line(result.getStatusCode().getReasonPhrase()).build());
        */List<StagingDTO> stagingDTO = mapper.readValue(result.getBody(), TypeFactory.defaultInstance().constructCollectionType(List.class, StagingDTO.class));

        MessageBuilder mb = MessageBuilder.create(chatId);
        for (StagingDTO dto : stagingDTO)
            if(dto.getStatus() == Status.ACTIVE)
                mb.line("ID: " + dto.getId()+ "\nUPLOADER: " + dto.getUploader());

        return List.of(mb.build());
    }
}
