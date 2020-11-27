package ru.ijo42.rbirb.rest.V1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
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
        ResponseEntity<PhotoDTO> resp = getInfo(id);
        PhotoDTO photoDTO;
        if (resp.getStatusCode() != HttpStatus.OK || (photoDTO = resp.getBody()) == null)
            return new ResponseEntity<>(resp.getStatusCode());
        File photo = ioUtils.getPhotoFile(photoDTO.toPhotoModel());
        if (photo == null || !photo.exists()) {
            log.error("IN getImage - physically Photo #{} not available", photoDTO.getId());
            return new ResponseEntity<>(HttpStatus.GONE);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(photoDTO.isAnimated() ? MediaType.IMAGE_GIF : MediaType.IMAGE_PNG);
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
    public RedirectView getRandomly() {
        try {
            return new RedirectView("/api/v1/" +
                    getRandomByPredicate(PicturePredicate.ANY).getId());
        } catch (HttpStatusCodeException ex) {
            return new RedirectView("");
        }
    }

    @GetMapping(value = "/random/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PhotoDTO> getInfoRandomly() {
        try {
            PhotoModel photoModel = getRandomByPredicate(PicturePredicate.ANY);
            return new ResponseEntity<>(new PhotoDTO(photoModel), HttpStatus.OK);
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getStatusCode());
        }
    }

    @GetMapping("/{id}.png")
    public ResponseEntity<byte[]> getStrictPng(@PathVariable("id") long id) {
        try {
            List<PhotoModel> models = getByPredicate(PicturePredicate.STATIC);
            if (models.size() == 0)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            return getImage(models.get(operateID(id, models.size())).getId());
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getStatusCode());
        }
    }

    @GetMapping("/{id}.gif")
    public ResponseEntity<byte[]> getStrictGif(@PathVariable("id") long id) {
        try {
            List<PhotoModel> models = getByPredicate(PicturePredicate.ANIMATED);
            if (models.size() == 0)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            return getImage(models.get(operateID(id, models.size())).getId());
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getStatusCode());
        }
    }

    @GetMapping("/random/png")
    public RedirectView getRandomlyPng() {
        try {
            return new RedirectView("/api/v1/" +
                    getRandomByPredicate(PicturePredicate.STATIC).getId());
        } catch (HttpStatusCodeException ex) {
            return new RedirectView("");
        }
    }

    @GetMapping("/random/gif")
    public RedirectView getRandomlyGif() {
        try {
            return new RedirectView("/api/v1/" +
                    getRandomByPredicate(PicturePredicate.ANIMATED).getId());
        } catch (HttpStatusCodeException ex) {
            return new RedirectView("");
        }
    }

    public PhotoModel getRandomByPredicate(PicturePredicate predicate) throws HttpClientErrorException {
        List<PhotoModel> photos = getByPredicate(predicate);
        Collections.shuffle(photos);

        return photos.get(0);
    }

    public List<PhotoModel> getByPredicate(PicturePredicate predicate) throws HttpClientErrorException {
        List<PhotoModel> photos = photoRepository.findAll().stream().
                filter(PicturePredicate.ACTIVE.predicate).
                filter(predicate.predicate).collect(Collectors.toList());
        if (photos.size() < 1)
            throw new HttpClientErrorException(HttpStatus.NO_CONTENT);

        return photos;
    }

    public int operateID(long id, int size) {
        id = Math.abs(id + (id >= 0 ? -1 : +1)); // two-side shift
        id %= size;

        return Math.toIntExact(id);
    }

    enum PicturePredicate {
        ANIMATED(PhotoModel::isAnimated),
        STATIC(photoModel -> !photoModel.isAnimated()),
        ANY(m -> true),
        ACTIVE(photoModel -> photoModel.getStatus() == Status.ACTIVE);

        public Predicate<? super PhotoModel> predicate;

        PicturePredicate(Predicate<? super PhotoModel> predicate) {
            this.predicate = predicate;
        }
    }

}
