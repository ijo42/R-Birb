package ru.ijo42.rbirb.tgbot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhotoDTO {

    private String uploader;

    private long moderator;

    private boolean isAnimated;

    private Long id;

    private Status status;

}
