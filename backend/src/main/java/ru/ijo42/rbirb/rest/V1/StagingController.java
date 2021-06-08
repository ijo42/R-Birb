package ru.ijo42.rbirb.rest.V1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ru.ijo42.rbirb.model.StagingModel;
import ru.ijo42.rbirb.model.Status;
import ru.ijo42.rbirb.model.dto.PhotoDTO;
import ru.ijo42.rbirb.model.dto.StagingDTO;
import ru.ijo42.rbirb.repository.StagingRepository;
import ru.ijo42.rbirb.service.StagingService;
import ru.ijo42.rbirb.utils.IOUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1/staging")
public class StagingController {

    private final StagingRepository stagingRepository;
    private final StagingService stagingService;
    private final IOUtils ioUtils;

    public StagingController(StagingRepository stagingRepository, StagingService stagingService, IOUtils ioUtils) {
        this.stagingRepository = stagingRepository;
        this.stagingService = stagingService;
        this.ioUtils = ioUtils;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StagingDTO>> getList() {
        return new ResponseEntity<>(stagingRepository.findAll().stream().
                map(StagingDTO::new).collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}",
            produces = { "multipart/form-data" })
    public ResponseEntity<Resource> getImage(@PathVariable("id") long id) {
        ResponseEntity<StagingDTO> resp = getUnprocessedInfoByID(id);
        StagingDTO stagingDTO;
        if (resp.getStatusCode() != HttpStatus.OK || (stagingDTO = resp.getBody()) == null)
            return new ResponseEntity<>(resp.getStatusCode());
        if (stagingDTO.getStatus() != Status.ACTIVE)
            return new ResponseEntity<>(HttpStatus.LOCKED);
        File stg = ioUtils.getStagingPhotoFile(stagingDTO.toStagingModel());
        if (stg == null || !stg.exists()) {
            log.error("IN getUnprocessedByID - physically Staging #{} not available", stagingDTO.getId());
            return new ResponseEntity<>(HttpStatus.GONE);
        }

        return ResponseEntity.ok()
                .contentType(stagingDTO.isAnimated() ? MediaType.IMAGE_GIF : MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .body(new FileSystemResource(stg));
    }

    @GetMapping(value = "/{id}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StagingDTO> getUnprocessedInfoByID(@PathVariable("id") long id) {
        StagingDTO dto;
        try {
            dto = new StagingDTO(stagingRepository.getById(id));
        } catch (HttpClientErrorException ex) {
            return new ResponseEntity<>(ex.getStatusCode());
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(value = "/next", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StagingDTO> getNextUnprocessed() {
        Optional<StagingModel> st = stagingService.getNext();
        if (st.isEmpty())
            return new ResponseEntity<>(new StagingDTO(HttpStatus.NO_CONTENT.getReasonPhrase()), HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(new StagingDTO(st.get()), HttpStatus.OK);
    }

    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PhotoDTO> acceptUnprocessedByID(@PathVariable("id") long id) {
        PhotoDTO dto;
        try {
            dto = new PhotoDTO(stagingService.accept(id));
        } catch (HttpClientErrorException ex) {
            return new ResponseEntity<>(ex.getStatusCode());
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PhotoDTO>> acceptAllUnProcessed() {
        List<PhotoDTO> photoDTOS;
        try {
            photoDTOS = stagingRepository.findAll().parallelStream().filter(m -> m.getStatus() == Status.ACTIVE)
                    .filter(m -> m.getModerator() == -1).map(StagingModel::getId).map(stagingService::accept).map(PhotoDTO::new)
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException ex) {
            return new ResponseEntity<>(ex.getStatusCode());
        }
        return new ResponseEntity<>(photoDTOS, HttpStatus.OK);

    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> declineUnprocessedByID(@PathVariable("id") long id) {
        try {
            stagingService.decline(id);
        } catch (HttpClientErrorException ex) {
            return new ResponseEntity<>(ex.getStatusCode());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
