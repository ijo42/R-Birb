package ru.ijo42.rbirb.tgbot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${telegram.bot.name}")
    @Getter
    private String botUsername;

    @Value("${telegram.bot.token}")
    @Getter
    private String botToken;

    @Value("${telegram.bot.admin}")
    private String botAdmin;

    public static Bot absSender;

    private final UpdateReceiver updateReceiver;

    public Bot(UpdateReceiver updateReceiver) {
        this.updateReceiver = updateReceiver;
        absSender = this;
    }

    @Override
    public void onUpdateReceived(Update update) {
        List<BotApiMethod<Message>> messagesToSend = updateReceiver.handle(update);

        if (messagesToSend != null && !messagesToSend.isEmpty()) {
            messagesToSend.forEach(response -> {
                if (response instanceof SendMessage) {
                    executeWithExceptionCheck((SendMessage) response);
                }
            });
        }
    }

    @PostConstruct
    public void startBot() {
        sendStartReport();
    }

    public void executeWithExceptionCheck(SendMessage sendMessage) {
        try {
            execute(sendMessage);
            log.debug("Executed {}", sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception while sending message {} to user: {}", sendMessage, e.getMessage());
        }
    }

    public void sendStartReport() {
        executeWithExceptionCheck(new SendMessage(botAdmin,"Bot start up is successful"));
        log.debug("Start report sent to Admin");
    }

    public void executeWithExceptionCheck(SendPhoto sendImageUploadingAFile) {
        try {
            execute(sendImageUploadingAFile);
            log.debug("Executed {}", sendImageUploadingAFile);
        } catch (TelegramApiException e) {
            log.error("Exception while sending message {} to user: {}", sendImageUploadingAFile, e.getMessage());
        }
    }
}