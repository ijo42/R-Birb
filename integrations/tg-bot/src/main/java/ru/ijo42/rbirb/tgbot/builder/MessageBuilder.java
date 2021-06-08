package ru.ijo42.rbirb.tgbot.builder;

import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public final class MessageBuilder {
    @Setter
    private long chatId;
    private final StringBuilder sb = new StringBuilder();
    private final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    private List<InlineKeyboardButton> row = null;

    private MessageBuilder() {
    }

    public static MessageBuilder create(long chatId) {
        MessageBuilder builder = new MessageBuilder();
        builder.setChatId(chatId);
        return builder;
    }

    public MessageBuilder line(String text, Object... args) {
        sb.append(String.format(text, args));
        return line();
    }

    public MessageBuilder line() {
        sb.append(String.format("%n"));
        return this;
    }

    public MessageBuilder row() {
        addRowToKeyboard();
        row = new ArrayList<>();
        return this;
    }

    public MessageBuilder button(String text, String callbackData) {
        InlineKeyboardButton k = new InlineKeyboardButton(text);
        k.setCallbackData(callbackData);
        row.add(k);
        return this;
    }

    public SendMessage build() {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), sb.toString());
        sendMessage.enableMarkdown(true);

        addRowToKeyboard();

        if (!keyboard.isEmpty()) {
            sendMessage.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
        }

        return sendMessage;
    }

    private void addRowToKeyboard() {
        if (row != null) {
            keyboard.add(row);
        }
    }
}
