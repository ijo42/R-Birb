package ru.ijo42.rbirb.tgbot.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.ijo42.rbirb.tgbot.Bot;
import ru.ijo42.rbirb.tgbot.UserService;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.User;

import java.util.List;

@Slf4j
public abstract class AbstractBaseHandler {

    @Value("${telegram.bot.admin}")
    protected long botAdmin;

    @Value("${rest.endpoint}")
    protected String restEndpoint;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    @Lazy
    protected Bot absSender;

    @Autowired
    protected UserService userService;

    protected final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public List<BotApiMethod<Message>> authorizeAndHandle(User user, long chatId, String message) {
        if (userService.isAuthorized(this.getClass(), user))
            return handleStateless(user, chatId, message);
        else
            return handleUnauthorized(user, chatId, message);
    }

    protected abstract List<BotApiMethod<Message>> handleStateless(User user, long chatId, String message);

    public List<BotApiMethod<Message>> handleOther(Update update) {
        return null;
    }

    private List<BotApiMethod<Message>> handleUnauthorized(User user, long chatId, String message) {
        log.info("Unauthorized access: {} {}", user, message);
        return List.of(MessageBuilder.create(chatId)
                        .line("Your id is *%s*", chatId)
                        .line("Please contact your supervisor to gain access")
                        .build(),
                MessageBuilder.create(botAdmin)
                        .line("*Unauthorized access:* %s", chatId)
                        .line("*Message:* %s", message.replaceAll("_", "-"))
                        .build());
    }
}
