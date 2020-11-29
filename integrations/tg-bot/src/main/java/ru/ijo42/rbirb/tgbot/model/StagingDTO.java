package ru.ijo42.rbirb.tgbot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StagingDTO {

    private String uuid;

    private String uploader;

    private long moderator;

    private boolean isAnimated;

    private Long id;

    private Status status;

    private String error;
}
