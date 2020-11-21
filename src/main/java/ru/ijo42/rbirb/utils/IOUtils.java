package ru.ijo42.rbirb.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.HttpClientErrorException;
import ru.ijo42.rbirb.model.PhotoModel;
import ru.ijo42.rbirb.model.PicType;
import ru.ijo42.rbirb.model.StagingModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

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

    @SneakyThrows
    public boolean transferStaging(StagingModel stagingModel, PhotoModel photoModel) {
        Path staging = getStagingPhotoFile(stagingModel).toPath(), photo = getPhotoFile(photoModel).toPath();
        try {
            return Files.move(staging, photo).toFile().exists();
        } catch (IOException e) {
            e.printStackTrace();
            Files.deleteIfExists(staging);
            Files.deleteIfExists(photo);
            return false;
        }
    }

    public byte[] toByteArray(File file) {
        if (file.length() > maxFileSize.toBytes()) {
            throw new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        byte[] buffer = new byte[ (int) file.length() ];
        try (InputStream ios = new FileInputStream(file)) {
            if (ios.read(buffer) == -1) {
                throw new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buffer;
    }
}
