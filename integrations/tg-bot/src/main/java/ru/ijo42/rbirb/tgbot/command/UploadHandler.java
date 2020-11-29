package ru.ijo42.rbirb.tgbot.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.ijo42.rbirb.tgbot.annotations.BotCommand;
import ru.ijo42.rbirb.tgbot.builder.MessageBuilder;
import ru.ijo42.rbirb.tgbot.model.User;

import java.util.*;

import static java.lang.Math.pow;

@Component
@Slf4j
@BotCommand(command = "/upload", message = "Returns staging by id")
public class UploadHandler extends AbstractBaseHandler {

    @Override
    protected List<BotApiMethod<Message>> handle(User user, long chatId, String message) {
        return List.of(MessageBuilder.create(chatId).line("Send picture").build());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public List<BotApiMethod<Message>> upload(Update update) {
        PhotoSize size = getPhoto(update);
        if(size==null)
            return List.of(MessageBuilder.create(update.getMessage().getFrom().getId())
                    .line("Invalid picture").build());
        String filePath = getFilePath(size);
        java.io.File file = downloadPhotoByFilePath(filePath);

        var params = new LinkedMultiValueMap<String, FileSystemResource>();
        params.put("file", Collections.singletonList(new FileSystemResource(file)));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        var requestEntity = new HttpEntity<>(params, httpHeaders);
        ResponseEntity<String> entity = restTemplate.exchange(restEndpoint + "/upload", HttpMethod.POST, requestEntity, String.class);
        if(file.exists())
            file.delete();
        return Collections.singletonList(MessageBuilder.create(
                update.getMessage().getFrom().getId()).
                line(
                        entity.getStatusCode().getReasonPhrase()
                ).build());
    }

    public PhotoSize getPhoto(Update update) {
        if (update.hasMessage() && update.getMessage().hasPhoto()) {

            List<PhotoSize> photos = update.getMessage().getPhoto();

            return photos.stream().filter(s->s.getFileSize() < 16 * pow(2,20))
                    .max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
        }

        return null;
    }

    public String getFilePath(PhotoSize photo) {
        Objects.requireNonNull(photo);

        if (photo.getFilePath() != null) {
            return photo.getFilePath();
        } else {
            try {
                File file = getAbsSender().execute(GetFile.builder().fileId(photo.getFileId()).build());
                return file.getFilePath();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        return null; // Just in case
    }

    public java.io.File downloadPhotoByFilePath(String filePath) {
        try {
            return getAbsSender().downloadFile(filePath);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
