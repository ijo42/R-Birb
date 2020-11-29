package ru.ijo42.rbirb.tgbot.command;

import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;
import ru.ijo42.rbirb.tgbot.annotations.RequiredToken;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.StagingDTO;
import ru.ijo42.rbirb.tgbot.model.Status;
import ru.ijo42.rbirb.tgbot.model.User;

import java.util.List;

@Component
@Slf4j
@RequiredToken
@BotCommand(command = "/list", message = "Returns staging list")
public class StagingListHandler extends AbstractBaseHandler {
    @SneakyThrows
    @Override
    protected List<BotApiMethod<Message>> handleStateless(User user, long chatId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, userService.getToken(user.getId()));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(restEndpoint + "/staging", HttpMethod.GET, entity, String.class);
        log.error(result.getBody());
        if (result.getStatusCode() != HttpStatus.OK)
            return List.of(MessageBuilder.create(chatId).line(result.getStatusCode().getReasonPhrase()).build());

        List<StagingDTO> stagingDTO = mapper.readValue(result.getBody(), TypeFactory.defaultInstance().
                constructCollectionType(List.class, StagingDTO.class));

        MessageBuilder mb = MessageBuilder.create(chatId);
        mb.line("List:");
        for (StagingDTO dto : stagingDTO)
            if (dto.getStatus() == Status.ACTIVE && dto.getModerator() == -1)
                mb.line("ID: %d[%s]\nUPLOADER: %s".formatted(dto.getId(), dto.isAnimated() ? "GIF" : "PNG", dto.getUploader()));

        return List.of(mb.build());
    }
}
