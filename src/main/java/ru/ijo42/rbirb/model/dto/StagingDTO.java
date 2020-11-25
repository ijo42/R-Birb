package ru.ijo42.rbirb.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.ijo42.rbirb.model.StagingModel;
import ru.ijo42.rbirb.model.Status;

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

    public StagingDTO(StagingModel model) {
        this.id = model.getId();
        this.uuid = model.getUuid();
        this.uploader = model.getUploader();
        this.moderator = model.getModerator();
        this.isAnimated = model.isAnimated();
        this.status = model.getStatus();
    }

    public StagingDTO(String error) {
        this.error = error;
    }

    public StagingModel toStagingModel() {
        StagingModel stagingModel = new StagingModel();
        stagingModel.setId(getId());
        stagingModel.setUuid(getUuid());
        stagingModel.setUploader(getUploader());
        stagingModel.setModerator(getModerator());
        stagingModel.setAnimated(isAnimated());
        stagingModel.setStatus(getStatus());
        return stagingModel;
    }
}
