package com.smartshare.security.firebase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticatedUser {
    private String uid;
    private String email;
}
