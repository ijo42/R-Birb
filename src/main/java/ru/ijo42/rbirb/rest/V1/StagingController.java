package ru.ijo42.rbirb.rest.V1;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import ru.ijo42.rbirb.model.dto.PhotoDTO;
import ru.ijo42.rbirb.model.dto.StagingDTO;
import ru.ijo42.rbirb.repository.StagingRepository;
import ru.ijo42.rbirb.service.StagingService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/staging")
public class StagingController {

    private final StagingRepository stagingRepository;
    private final StagingService stagingService;

    public StagingController(StagingRepository stagingRepository, StagingService stagingService) {
        this.stagingRepository = stagingRepository;
        this.stagingService = stagingService;
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StagingDTO>> getList() {
        return new ResponseEntity<>(stagingRepository.findAll().stream().
                map(StagingDTO::new).collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StagingDTO> getUnprocessedByID(@PathVariable("id") long id) {
        StagingDTO dto;
        try {
            dto = new StagingDTO(stagingRepository.getOne(id));
        } catch (HttpClientErrorException ex) {
            return new ResponseEntity<>(ex.getStatusCode());
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
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
