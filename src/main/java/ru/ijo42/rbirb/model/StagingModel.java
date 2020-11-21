package ru.ijo42.rbirb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "staging_photos")
@EqualsAndHashCode(callSuper = true)
public class StagingModel extends BaseEntity {

    private String uuid;

    private String uploader;

    private long moderator;

    @Column(name = "animated")
    private boolean isAnimated;
}
