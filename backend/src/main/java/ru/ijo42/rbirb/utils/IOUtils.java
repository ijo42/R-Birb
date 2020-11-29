package ru.ijo42.rbirb.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.ijo42.rbirb.model.PhotoModel;
import ru.ijo42.rbirb.model.PicType;
import ru.ijo42.rbirb.model.StagingModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Instant;

@Slf4j
@Service
@Scope("singleton")
public class IOUtils {
    protected File uploadDir;
    protected File photoDir;
    @Value("${upload.path}")
    private String mainPath;

    @Value("${upload.path.staging}")
    private String staging;

    @Value("${upload.path.photo}")
    private String picture;

    public File getUploadDir() {
        if (uploadDir != null)
            return uploadDir;

        uploadDir = new File(mainPath + staging);
        if (!uploadDir.exists()) log.error("Upload dir not exists...creating... {}", uploadDir.mkdirs());
        return uploadDir;
    }

    public File getPhotoDir() {
        if (photoDir != null)
            return photoDir;

        photoDir = new File(mainPath + picture);
        if (!photoDir.exists()) log.error("Photo dir not exists...creating... {}", photoDir.mkdirs());

        return photoDir;
    }

    public File getStagingPhotoFile(StagingModel stagingModel) {
        return getUploadDir().toPath().resolve(stagingModel.getUuid() + (stagingModel.isAnimated() ?
                PicType.GIF.toString() : PicType.PNG.toString())).toFile();
    }

    public File getPhotoFile(PhotoModel photoModel) {
        return getPhotoDir().toPath().resolve(photoModel.getId() + (photoModel.isAnimated() ?
                PicType.GIF.toString() : PicType.PNG.toString())).toFile();
    }

    public boolean eraseStagingPhoto(StagingModel stagingModel) {
        return getStagingPhotoFile(stagingModel).delete();
    }

    public boolean erasePhoto(PhotoModel photoModel) {
        return getPhotoFile(photoModel).delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean transferStaging(StagingModel stagingModel, PhotoModel photoModel) {
        Path staging = getStagingPhotoFile(stagingModel).toPath(), photo = getPhotoFile(photoModel).toPath();
        try {
            return Files.move(staging, photo).toFile().exists();
        } catch (IOException e) {
            e.printStackTrace();
            if (staging.toFile().exists())
                staging.toFile().delete();
            if (photo.toFile().exists())
                photo.toFile().delete();
            return false;
        }
    }

    public static java.util.Date getNow() {
        return Date.from(Instant.now());
    }
}
