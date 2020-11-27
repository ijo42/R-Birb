package ru.ijo42.rbirb.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.ijo42.rbirb.model.Status;
import ru.ijo42.rbirb.model.TokenModel;
import ru.ijo42.rbirb.service.TokenService;

import java.util.Optional;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final TokenService tokenService;

    public AuthenticationProvider(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public static long getModeratorID() {
        long ID;
        try {
            ID = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception ex) {
            ID = -1;
        }
        return ID;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        //
    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        var token = usernamePasswordAuthenticationToken.getCredentials();
        TokenModel tokenModel = Optional
                .ofNullable(token)
                .map(String::valueOf)
                .map(s -> s.replace("Bearer ", ""))
                .flatMap(tokenService::findByToken)
                .orElseThrow(() -> new UsernameNotFoundException("Cannot find authentication token " + token));
        return new User(tokenModel.getId().toString(), "", tokenModel.getStatus().equals(Status.ACTIVE), true, true, !tokenModel.getStatus().equals(Status.DEACTIVATED),
                AuthorityUtils.createAuthorityList("USER"));
    }
}