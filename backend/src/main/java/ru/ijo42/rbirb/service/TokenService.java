package ru.ijo42.rbirb.service;

import ru.ijo42.rbirb.model.TokenModel;

import java.util.List;
import java.util.Optional;

public interface TokenService {
    List<TokenModel> findAll();

    boolean isAvailable(long id);

    Optional<TokenModel> findByToken(String token);

    Optional<TokenModel> findById(Long id);

    TokenModel register(TokenModel acceptor, String extendedInformation);

    String genToken();

    void disableById(Long id);

    void disableByToken(String token);
}
