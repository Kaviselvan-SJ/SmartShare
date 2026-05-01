package com.smartshare.security.firebase;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import java.util.Collections;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthenticatedUser principal;
    private final String credentials;

    public FirebaseAuthenticationToken(AuthenticatedUser principal, String credentials) {
        super(Collections.emptyList());
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
