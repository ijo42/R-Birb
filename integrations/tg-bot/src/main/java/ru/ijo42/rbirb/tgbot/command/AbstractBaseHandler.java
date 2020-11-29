package ru.ijo42.rbirb.tgbot.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.ijo42.rbirb.tgbot.Bot;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.User;
import ru.ijo42.rbirb.tgbot.UserService;

import java.util.List;

@Slf4j
public abstract class AbstractBaseHandler {

    @Value("${telegram.bot.admin}")
    protected long botAdmin;

    @Value("${rest.endpoint}")
    protected String restEndpoint;

    @Autowired
    protected RestTemplate restTemplate;

    protected Bot getAbsSender(){ return Bot.absSender; };

    @Autowired
    protected UserService userService;

    protected final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);


    public final List<BotApiMethod<Message>> authorizeAndHandle(User user, long chatId, String message) {
        return userService.isAuthorized(this.getClass(), user) ?
                handle(user, chatId, message) : handleUnauthorized(user, chatId, message);
    }

    protected abstract List<BotApiMethod<Message>> handle(User user, long chatId,  String message);

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

    public static <T> T fromJSON(final TypeReference<T> type,
                                 final String jsonPacket) {
        T data = null;
        try {
            data = new ObjectMapper().readValue(jsonPacket, type);
        } catch (Exception e) {
            // Handle the problem
        }
        return data;
    }
}
