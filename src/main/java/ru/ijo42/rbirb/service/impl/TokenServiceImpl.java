package ru.ijo42.rbirb.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.ijo42.rbirb.model.Status;
import ru.ijo42.rbirb.model.TokenModel;
import ru.ijo42.rbirb.repository.AcceptorsRepository;
import ru.ijo42.rbirb.service.TokenService;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final AcceptorsRepository acceptorsRepository;

    public TokenServiceImpl(AcceptorsRepository acceptorsRepository) {
        this.acceptorsRepository = acceptorsRepository;
    }

    @Override
    public List<TokenModel> findAll() {
        List<TokenModel> acceptors = acceptorsRepository.findAll();
        log.debug("IN findAll - {} acceptors", acceptors.size());
        return acceptors;
    }

    @Override
    public Optional<TokenModel> findByToken(String token) {
        Optional<TokenModel> modelOptional = acceptorsRepository.findAll().stream().
                filter(model -> model.getToken().equals(token)).findFirst();
        log.debug("IN findByToken - Presented? {}", modelOptional.isPresent());
        return modelOptional;
    }

    @Override
    public Optional<TokenModel> findById(Long id) {
        Optional<TokenModel> modelOptional = acceptorsRepository.findById(id);
        log.debug("IN findById - Presented? {}", modelOptional.isPresent());

        return modelOptional;
    }

    @Override
    public TokenModel register(TokenModel newToken, String extendedInformation) {
        if (newToken.getExtendedInformation() == null)
            newToken.setExtendedInformation("");
        newToken.setToken(genToken());
        newToken.setStatus(Status.ACTIVE);
        newToken.setCreated(Date.from(Instant.now()));
        newToken.setUpdated(Date.from(Instant.now()));

        TokenModel savedToken = acceptorsRepository.save(newToken);

        Optional<TokenModel> baseToken = findById(1L);
        if (baseToken.isPresent() && baseToken.get().getStatus() == Status.ACTIVE)
            acceptorsRepository.save(baseToken.get().setStatusStreamed(Status.DEACTIVATED));

        log.info("IN registered - Token #{} '{}'", savedToken.getId(), savedToken.getExtendedInformation());
        return savedToken;
    }

    private String genToken() {
        return (UUID.randomUUID().toString().replaceAll("-", "").substring(16));
    }

    public void delete(TokenModel tokenModel) {
        tokenModel.setStatus(Status.DELETED);
        tokenModel.setUpdated(Date.from(Instant.now()));
        acceptorsRepository.save(tokenModel);
        log.info("IN delete - token #{}", tokenModel.getId());
    }

    @Override
    public void deleteById(Long id) {
        Optional<TokenModel> modelOptional = findById(id);
        modelOptional.ifPresentOrElse(this::delete, () -> {
            log.error("IN deleteById - Token #{} not found", id);
            throw new UsernameNotFoundException("Token #%d not found".formatted(id));
        });
    }

    @Override
    public void deleteByToken(String token) {
        Optional<TokenModel> modelOptional = findByToken(token);
        modelOptional.ifPresentOrElse(this::delete, () -> {
            log.error("IN deleteById - Token '{}' not found", token);
            throw new UsernameNotFoundException("Token '%s' not found".formatted(token));
        });
    }
}
