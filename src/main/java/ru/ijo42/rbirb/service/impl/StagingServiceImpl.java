package ru.ijo42.rbirb.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import ru.ijo42.rbirb.model.PhotoModel;
import ru.ijo42.rbirb.model.PicType;
import ru.ijo42.rbirb.model.StagingModel;
import ru.ijo42.rbirb.model.Status;
import ru.ijo42.rbirb.repository.PhotoRepository;
import ru.ijo42.rbirb.repository.StagingRepository;
import ru.ijo42.rbirb.security.AuthenticationProvider;
import ru.ijo42.rbirb.service.StagingService;
import ru.ijo42.rbirb.utils.IOUtils;
import ru.ijo42.rbirb.utils.ImgConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class StagingServiceImpl implements StagingService {

    private final StagingRepository stagingRepository;
    private final PhotoRepository photoRepository;
    private final IOUtils ioUtils;

    public StagingServiceImpl(StagingRepository stagingRepository,
                              PhotoRepository photoRepository,
                              IOUtils ioUtils) {
        this.stagingRepository = stagingRepository;
        this.photoRepository = photoRepository;
        this.ioUtils = ioUtils;
    }


    @Override
    public List<StagingModel> findAll() {
        List<StagingModel> models = stagingRepository.findAll();
        log.info("IN findAll - {} StagingModel", models.size());
        return models;
    }

    @Override
    public Optional<StagingModel> findById(Long id) {
        Optional<StagingModel> stagingModel = stagingRepository.findById(id);
        log.info("IN findById - Presented? {}", stagingModel.isPresent());

        return stagingModel;
    }

    @Override
    public Optional<StagingModel> findByUUID(String uuid) {
        Optional<StagingModel> stagingModel = findAll().stream().filter(x -> x.getUuid().equals(uuid)).findFirst();
        log.info("IN findByUUID - Presented? {}", stagingModel.isPresent());

        return stagingModel;
    }

    @Override
    public StagingModel upload(StagingModel stagingModel, MultipartFile file) throws IOException, HttpClientErrorException {
        String uuid = UUID.randomUUID().toString();

        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;

        File tmp = File.createTempFile(uuid, ".img");
        log.debug("Saving temp file to {}", tmp);
        file.transferTo(tmp);

        PicType type = (originalFilename.endsWith("gif") && ImgConverter.isAnimated(tmp) ?
                PicType.GIF : PicType.PNG);

        Path dest = Paths.get(ioUtils.getUploadDir() + "/" + uuid + type);

        if (!ImgConverter.convert(tmp, dest, type)) {
            //log.warn("Unexpected (╯°□°）╯︵ ┻━┻");
            throw new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        stagingModel.setAnimated(type == PicType.GIF);
        stagingModel.setUuid(uuid);

        stagingModel.setModerator(-1);
        stagingModel.setStatus(Status.ACTIVE);
        stagingModel.setCreated(Date.from(Instant.now()));
        stagingModel.setUpdated(Date.from(Instant.now()));
        if (stagingModel.getUploader() == null)
            stagingModel.setUploader("ANONYMOUS");

        StagingModel savedModel = stagingRepository.save(stagingModel);
        log.info("IN upload - new {}! {} by {}", type.name().toUpperCase(), savedModel.getUuid(), savedModel.getUploader());

        return savedModel;
    }

    private void delete(StagingModel stagingModel) {
        if (stagingModel.getStatus() == Status.DELETED) {
            log.warn("IN delete - Staging #{} already DELETED", stagingModel.getId());
            throw new HttpClientErrorException(HttpStatus.ALREADY_REPORTED);
        }
        if (stagingModel.getStatus() == Status.NOT_YET) {
            log.warn("IN delete - Staging #{} already in process", stagingModel.getId());
            throw new HttpClientErrorException(HttpStatus.CONFLICT);
        }
        if (!ioUtils.getStagingPhotoFile(stagingModel).exists()) {
            log.error("IN delete - physically Staging #{} not available", stagingModel.getId());
            throw new HttpClientErrorException(HttpStatus.GONE);
        }
        if (!ioUtils.eraseStagingPhoto(stagingModel)) {
            log.error("IN delete - Staging #{}. Error on phis erasing", stagingModel.getId());
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        stagingModel.setStatus(Status.DELETED);
        stagingModel.setUpdated(Date.from(Instant.now()));
        stagingModel.setModerator(AuthenticationProvider.getModeratorID());
        stagingRepository.save(stagingModel);
        log.info("IN deleteById - Staging #{}", stagingModel.getId());
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresentOrElse(this::delete, () -> {
            log.warn("IN deleteById - Staging #{} not found", id);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        });
    }

    @Override
    public void deleteByUUID(String uuid) {
        findByUUID(uuid).ifPresentOrElse(this::delete, () -> {
            log.warn("IN deleteByUUID - Staging {} not found", uuid);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        });
    }

    @Override
    public PhotoModel accept(Long id) throws HttpClientErrorException {
        final PhotoModel[] out = new PhotoModel[ 1 ];
        findById(id).ifPresentOrElse(stagingModel -> {
            if (stagingModel.getModerator() != -1) {
                log.warn("IN accept - Staging #{} already moderated", id);
                throw new HttpClientErrorException(HttpStatus.CONFLICT);
            }
            if (stagingModel.getStatus() == Status.DELETED) {
                log.warn("IN accept - Staging #{} DELETED", stagingModel.getId());
                throw new HttpClientErrorException(HttpStatus.ALREADY_REPORTED);
            }
            if (stagingModel.getStatus() == Status.NOT_YET) {
                /*if(Instant.now().plusSeconds(3600).isAfter(stagingModel.getUpdated().toInstant())){
                    log.warn("IN delete - Staging #{} in process too long. deleting", stagingModel.getId());
                   TODO: продумать
                }*/
                log.warn("IN accept - Staging #{} already in process", stagingModel.getId());
                throw new HttpClientErrorException(HttpStatus.CONFLICT);
            }
            if (!ioUtils.getStagingPhotoFile(stagingModel).exists()) {
                log.error("IN accept - physically Staging #{} not available", stagingModel.getId());
                throw new HttpClientErrorException(HttpStatus.GONE);
            }

            stagingModel.setModerator(AuthenticationProvider.getModeratorID());
            stagingModel.setStatus(Status.NOT_YET);
            stagingModel.setUpdated(Date.from(Instant.now()));

            stagingModel = stagingRepository.save(stagingModel);

            PhotoModel photoModel = new PhotoModel(stagingModel.getUploader(), AuthenticationProvider.getModeratorID(), stagingModel.isAnimated());
            photoModel.setStatus(Status.NOT_YET);
            photoModel.setUpdated(Date.from(Instant.now()));
            photoModel.setAnimated(stagingModel.isAnimated());
            photoModel.setCreated(stagingModel.getCreated());

            photoModel = photoRepository.save(photoModel);

            if (!ioUtils.transferStaging(stagingModel, photoModel)) {
                log.error("IN accept - physically transfer Staging#{} to Photo #{} failed", stagingModel.getId(), photoModel.getId());
                throw new HttpClientErrorException(HttpStatus.GONE);

            }
            stagingModel.setStatus(Status.DEACTIVATED);
            stagingModel.setUpdated(Date.from(Instant.now()));
            stagingRepository.save(stagingModel);

            photoModel.setStatus(Status.ACTIVE);
            photoModel.setUpdated(Date.from(Instant.now()));
            out[ 0 ] = photoRepository.save(photoModel);
        }, () -> {
            log.error("IN accept - Staging #{} not found", id);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        });

        return out[ 0 ];
    }

    @Override
    public void decline(Long id) {
        findById(id).ifPresentOrElse(stagingModel -> {
            if (stagingModel.getModerator() != -1) {
                log.warn("IN decline - Staging #{} already moderated", id);
                throw new HttpClientErrorException(HttpStatus.CONFLICT);
            }
            if (stagingModel.getStatus() == Status.DELETED) {
                log.warn("IN decline - Staging #{} DELETED", stagingModel.getId());
                throw new HttpClientErrorException(HttpStatus.ALREADY_REPORTED);
            }
            if (stagingModel.getStatus() == Status.NOT_YET) {
                log.warn("IN decline - Staging #{} already in process", stagingModel.getId());
                throw new HttpClientErrorException(HttpStatus.CONFLICT);
            }

            stagingModel.setModerator(AuthenticationProvider.getModeratorID());
            stagingModel.setUpdated(Date.from(Instant.now()));
            stagingModel.setStatus(Status.DEACTIVATED);

            stagingRepository.save(stagingModel);
        }, () -> {
            log.warn("IN decline - Staging #{} not found", id);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        });
    }

}
