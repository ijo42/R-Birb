package ru.ijo42.rbirb.service;

import ru.ijo42.rbirb.model.TokenModel;

import java.util.List;
import java.util.Optional;

public interface TokenService {
    List<TokenModel> findAll();

    Optional<TokenModel> findByToken(String token);

    Optional<TokenModel> findById(Long id);

    TokenModel register(TokenModel acceptor, String extendedInformation);

    void deleteById(Long id);

    void deleteByToken(String token);
}
