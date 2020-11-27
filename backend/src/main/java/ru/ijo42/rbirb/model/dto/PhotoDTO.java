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
        setId(model.getId());
        setUploader(model.getUploader());
        setAnimated(model.isAnimated());
        setModerator(model.getModerator());
        setStatus(model.getStatus());
    }

    public PhotoModel toPhotoModel() {
        PhotoModel photoModel = new PhotoModel();
        photoModel.setId(getId());
        photoModel.setUploader(getUploader());
        photoModel.setAnimated(isAnimated());
        photoModel.setModerator(getModerator());
        photoModel.setStatus(getStatus());
        return photoModel;
    }
}
