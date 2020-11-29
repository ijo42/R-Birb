package ru.ijo42.rbirb.tgbot.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.ijo42.rbirb.tgbot.UpdateReceiver;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;
import ru.ijo42.rbirb.tgbot.annotations.RequiredToken;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.User;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredToken
@BotCommand(command = "/staging", message = "Returns staging by id")
public class StagingPictureHandler extends AbstractBaseHandler {
    @SneakyThrows
    @Override
    protected List<BotApiMethod<Message>> handle(User user, long chatId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, userService.getToken(user.getId()));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        int arg = Integer.parseInt(UpdateReceiver.extractMessage(message));

        ResponseEntity<Resource> result = restTemplate.exchange(restEndpoint + "/staging/" + arg, HttpMethod.GET, entity, Resource.class);
        //log.error(result.toString());
        if(result.getStatusCode() != HttpStatus.OK || result.getBody() == null)
            return List.of(MessageBuilder.create(chatId).line(result.getStatusCode().getReasonPhrase()).build());

        String name = arg + (result.getHeaders().getContentType() == MediaType.IMAGE_GIF ? ".gif" : ".png");
        getAbsSender().executeWithExceptionCheck(sendImageUploadingAFile(result.getBody().getInputStream(), name, arg, chatId));
        return List.of();
    }


    @SneakyThrows
    public SendPhoto sendImageUploadingAFile(InputStream st, String name, int id, long chatId) {
        List<InlineKeyboardButton> i = List.of(
        InlineKeyboardButton.builder().text("ACCEPT").callbackData("accept@" + id).build(),
        InlineKeyboardButton.builder().text("DECLINE").callbackData("decline@" + id).build()
        );
        return SendPhoto.builder().chatId(String.valueOf(chatId)).photo(
                new InputFile(st, name)).replyMarkup(
                InlineKeyboardMarkup.builder().keyboard(Collections.singleton(i)).build()
        ).build();
    }

    public List<BotApiMethod<Message>> processButton(Update update){
        String msg = update.getInlineQuery().getQuery();
        if(msg.startsWith("accept"))
            accept(Integer.parseInt(msg.substring("accept@".length())), update.getMessage().getFrom().getId());
        else
            decline(Integer.parseInt(msg.substring("decline@".length())), update.getMessage().getFrom().getId());
        return List.of(MessageBuilder.create(update.getMessage().getFrom().getId())
                .line("ok")
                .build());
    }
    public void decline(int id, int userId){
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, userService.getToken(userId));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(restEndpoint + "/staging/" + id, HttpMethod.DELETE, entity, String.class);
    }
    public void accept(int id, int userId){
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, userService.getToken(userId));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(restEndpoint + "/staging/" + id, HttpMethod.POST, entity, String.class);
    }
}
