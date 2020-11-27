package ru.ijo42.rbirb.service;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import ru.ijo42.rbirb.model.PhotoModel;
import ru.ijo42.rbirb.model.StagingModel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface StagingService {
    List<StagingModel> findAll();

    Optional<StagingModel> getNext();

    Optional<StagingModel> findById(Long id);

    Optional<StagingModel> findByUUID(String uuid);

    StagingModel upload(StagingModel stagingModel, MultipartFile file) throws IOException, HttpClientErrorException;

    void deleteById(Long id);

    void deleteByUUID(String uuid);

    PhotoModel accept(Long id);

    void decline(Long id);
}
