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
@Table(name = "photos")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PhotoModel extends BaseEntity {

    private String uploader;

    private long moderator;

    @Column(name = "animated")
    private boolean isAnimated;
}
