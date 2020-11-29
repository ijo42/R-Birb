package ru.ijo42.rbirb.tgbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;
import ru.ijo42.rbirb.tgbot.command.AbstractBaseHandler;
import ru.ijo42.rbirb.tgbot.command.StagingPictureHandler;
import ru.ijo42.rbirb.tgbot.command.UploadHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateReceiver {
    private final List<AbstractBaseHandler> handlers;
    private final UserService userService;

    public List<BotApiMethod<Message>> handle(Update update) {
        try {
            int userId = 0;
            long chatId = 0;
            String text = null;
            if(update.hasMessage() && !update.getMessage().isGroupMessage() && (update.getMessage().hasPhoto() || update.getMessage().hasAnimation()))
                return ((UploadHandler) handlers.stream().filter(x->x instanceof UploadHandler).
                        findFirst().orElseThrow()).
                        upload(update);
            if (isMessageWithText(update)) {
                final Message message = update.getMessage();
                    chatId = message.getChat().getId();
                    userId = message.getFrom().getId();
                text = message.getText();
                log.debug("Update is text message {} from {} in {}", text, userId, chatId);
            } else if (update.hasCallbackQuery()) {
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                userId = callbackQuery.getFrom().getId();
                chatId = Long.parseLong(callbackQuery.getChatInstance());
                text = callbackQuery.getData();
                log.debug("Update is callback query {} from {} in {}", text, userId, chatId);
                return ((StagingPictureHandler) handlers.stream().filter(x->x instanceof StagingPictureHandler).
                        findFirst().orElseThrow()).processButton(update);
            }
            if (text != null && userId != 0) {
                return getHandler(text).authorizeAndHandle(userService.getOrCreate(userId), chatId, text);
            }

            throw new UnsupportedOperationException();
        } catch (UnsupportedOperationException e) {
            log.debug("Command: {} is unsupported", update.toString());
            return Collections.emptyList();
        }
    }

    private AbstractBaseHandler getHandler(String text) {
        return handlers.stream()
                .filter(h -> h.getClass()
                        .isAnnotationPresent(BotCommand.class))
                .filter(h -> Stream.of(h.getClass()
                        .getAnnotation(BotCommand.class)
                        .command())
                        .anyMatch(c -> c.equalsIgnoreCase(extractCommand(text))))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private boolean isMessageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }

    private boolean isGroupMessageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().isGroupMessage();
    }

    public static String extractCommand(String text) {
        return text.split(" ")[0];
    }

    public static String[] extractArgs(String text) {
        return text.split(" ");
    }

    public static String extractMessage(String text) {
        return text.split(" ",2)[1];
    }
}
