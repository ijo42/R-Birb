package ru.ijo42.rbirb.rest.V1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import ru.ijo42.rbirb.model.PhotoModel;
import ru.ijo42.rbirb.model.StagingModel;
import ru.ijo42.rbirb.model.Status;
import ru.ijo42.rbirb.model.dto.PhotoDTO;
import ru.ijo42.rbirb.model.dto.StagingDTO;
import ru.ijo42.rbirb.repository.PhotoRepository;
import ru.ijo42.rbirb.service.StagingService;
import ru.ijo42.rbirb.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1")
public class PublicController {

    private final IOUtils ioUtils;
    private final StagingService stagingService;
    private final PhotoRepository photoRepository;
    @Value("${upload.accepted}")
    private String[] acceptable;

    public PublicController(IOUtils ioUtils, StagingService stagingService, PhotoRepository photoRepository) {
        this.ioUtils = ioUtils;
        this.stagingService = stagingService;
        this.photoRepository = photoRepository;
    }

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StagingDTO> uploadFile(StagingModel stagingModel,
                                                 @RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename;
        if (file == null || (originalFilename = file.getOriginalFilename()) == null)
            return new ResponseEntity<>(HttpStatus.LENGTH_REQUIRED);
        if (Arrays.stream(acceptable).map(originalFilename::endsWith).findAny().isEmpty())
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        StagingModel model;
        try {
            model = stagingService.upload(stagingModel, file);
        } catch (HttpClientErrorException ex) {
            return new ResponseEntity<>(new StagingDTO(ex.getStatusText()), ex.getStatusCode());
        }
        return new ResponseEntity<>(new StagingDTO(model), HttpStatus.ACCEPTED);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") long id) {
        Optional<PhotoModel> modelOptional = photoRepository.findById(id);
        if (modelOptional.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        PhotoModel photoModel = modelOptional.get();
        if (photoModel.getStatus() != Status.ACTIVE) {
            log.debug("IN getImage - Photo #{} not available", photoModel.getId());
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }
        HttpHeaders headers = new HttpHeaders();
        File photo = ioUtils.getPhotoFile(photoModel);
        if (photo == null || !photo.exists()) {
            log.error("IN getImage - physically Photo #{} not available", photoModel.getId());
            return new ResponseEntity<>(HttpStatus.GONE);
        }
        headers.setContentType(photoModel.isAnimated() ? MediaType.IMAGE_GIF : MediaType.IMAGE_PNG);
        headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).getHeaderValue());

        return new ResponseEntity<>(ioUtils.toByteArray(photo),
                headers, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PhotoDTO> getInfo(@PathVariable("id") long id) {
        Optional<PhotoModel> modelOptional = photoRepository.findById(id);
        if (modelOptional.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        PhotoModel photoModel = modelOptional.get();
        if (photoModel.getStatus() != Status.ACTIVE) {
            log.debug("IN getImage - Photo #{} not available", photoModel.getId());
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }
        return new ResponseEntity<>(new PhotoDTO(modelOptional.get()), HttpStatus.OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PhotoDTO>> getModels() {
        List<PhotoModel> models = photoRepository.findAll().stream().
                filter(x -> x.getStatus() == Status.ACTIVE).collect(Collectors.toList());
        if (models.size() == 0)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        return new ResponseEntity<>(models.stream().map(PhotoDTO::new).
                collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping("/random")
    public ResponseEntity<byte[]> getRandomly() {
        return getImage(getRandomByPredicate(x -> true).getId());
    }

    @GetMapping(value = "/random/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PhotoDTO> getInfoRandomly() {
        PhotoModel photoModel = getRandomByPredicate(x -> true);
        return new ResponseEntity<>(new PhotoDTO(photoModel), HttpStatus.OK);
    }

    @GetMapping("/random/png")
    public ResponseEntity<byte[]> getRandomlyPng() {
        return getImage(getRandomByPredicate(photoModel -> !photoModel.isAnimated()).getId());
    }

    @GetMapping("/random/gif")
    public ResponseEntity<byte[]> getRandomlyGif() {
        return getImage(getRandomByPredicate(PhotoModel::isAnimated).getId());
    }

    public PhotoModel getRandomByPredicate(Predicate<? super PhotoModel> predicate) {
        List<PhotoModel> photos = photoRepository.findAll().stream().
                filter(x -> x.getStatus() == Status.ACTIVE).
                filter(predicate).collect(Collectors.toList());

        Collections.shuffle(photos);
        if (photos.size() < 1)
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT);

        return photos.get(0);
    }

}
