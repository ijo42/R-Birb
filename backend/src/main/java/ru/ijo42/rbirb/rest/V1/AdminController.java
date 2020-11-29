package ru.ijo42.rbirb.rest.V1;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ijo42.rbirb.model.TokenModel;
import ru.ijo42.rbirb.service.TokenService;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final TokenService tokenService;

    public AdminController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping(value = "/gen", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenModel> genAcceptor(@ModelAttribute TokenModel tokenModel) {
        return new ResponseEntity<>(tokenService.
                register(tokenModel, tokenModel.getExtendedInformation()), HttpStatus.OK);
    }

    @DeleteMapping(value = "/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenModel> deleteAcceptor(@PathVariable("id") long id) {
        tokenService.disableById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenModel> deleteAcceptor(@PathVariable("token") String token) {
        tokenService.disableByToken(token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TokenModel>> getAll() {
        return new ResponseEntity<>(tokenService.findAll(), HttpStatus.OK);
    }
}
