package ru.ijo42.rbirb.rest.V1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ru.ijo42.rbirb.model.PhotoModel;
import ru.ijo42.rbirb.model.Status;
import ru.ijo42.rbirb.repository.PhotoRepository;
import ru.ijo42.rbirb.security.AuthenticationProvider;
import ru.ijo42.rbirb.service.TokenService;
import ru.ijo42.rbirb.utils.IOUtils;

import java.io.File;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/v1/moderate")
public class ModerateController {
    private final TokenService tokenService;
    private final IOUtils ioUtils;
    private final PhotoRepository photoRepository;

    public ModerateController(TokenService tokenService, IOUtils ioUtils, PhotoRepository photoRepository) {
        this.tokenService = tokenService;
        this.ioUtils = ioUtils;
        this.photoRepository = photoRepository;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkToken() {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> disablePhoto(@PathVariable("id") long id) {
        Optional<PhotoModel> modelOptional = photoRepository.findById(id);
        if (modelOptional.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        PhotoModel photoModel = modelOptional.get();
        if (photoModel.getStatus() != Status.ACTIVE) {
            log.debug("IN getImage - Photo #{} not available", photoModel.getId());
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        File photo = ioUtils.getPhotoFile(photoModel);
        if (photo == null || !photo.exists()) {
            log.error("IN getImage - physically Photo #{} not available", photoModel.getId());
            return ResponseEntity.notFound().build();
        }

        delete(photoModel);

        return ResponseEntity.ok().build();
    }

    private void delete(PhotoModel photoModel) {
        if (photoModel.getStatus() == Status.DELETED) {
            log.warn("IN delete - Staging #{} already DELETED", photoModel.getId());
            throw new HttpClientErrorException(HttpStatus.ALREADY_REPORTED);
        }
        if (photoModel.getStatus() == Status.NOT_YET) {
            log.warn("IN delete - Staging #{} already in process", photoModel.getId());
            throw new HttpClientErrorException(HttpStatus.CONFLICT);
        }
        if (!ioUtils.getPhotoFile(photoModel).exists()) {
            log.error("IN delete - physically Staging #{} not available", photoModel.getId());
            throw new HttpClientErrorException(HttpStatus.GONE);
        }
        if (!ioUtils.erasePhoto(photoModel)) {
            log.error("IN delete - Staging #{}. Error on phis erasing", photoModel.getId());
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        photoModel.setStatus(Status.DELETED);
        photoModel.setUpdated(IOUtils.getNow());
        photoModel.setModerator(AuthenticationProvider.getModeratorID());
        photoRepository.save(photoModel);
        log.info("IN delete - Staging #{}", photoModel.getId());
    }

}
