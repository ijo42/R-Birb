package ru.ijo42.rbirb.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.ijo42.rbirb.model.PhotoModel;
import ru.ijo42.rbirb.model.Status;

@Getter
@Setter
public class PhotoDTO {

    private String uploader;

    private long moderator;

    private boolean isAnimated;

    private Long id;

    private Status status;

    public PhotoDTO(PhotoModel model) {
        this.id = model.getId();
        this.uploader = model.getUploader();
        this.isAnimated = model.isAnimated();
        this.moderator = model.getModerator();
        this.status = model.getStatus();
    }
}
