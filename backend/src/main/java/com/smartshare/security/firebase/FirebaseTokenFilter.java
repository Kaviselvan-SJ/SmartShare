package com.smartshare.security.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.smartshare.model.entity.UserEntity;
import com.smartshare.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                String uid = decodedToken.getUid();
                String email = decodedToken.getEmail();

                // Auto-create user on first login (supports both email/password and Google OAuth)
                userRepository.findByFirebaseUid(uid).orElseGet(() -> {
                    String displayName = decodedToken.getName();
                    String photoUrl = decodedToken.getPicture();

                    UserEntity newUser = UserEntity.builder()
                            .firebaseUid(uid)
                            .email(email)
                            .displayName(displayName)
                            .profileImageUrl(photoUrl)
                            .build();
                    return userRepository.save(newUser);
                });

                AuthenticatedUser user = new AuthenticatedUser(uid, email);
                FirebaseAuthenticationToken authentication = new FirebaseAuthenticationToken(user, token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (FirebaseAuthException e) {
                logger.error("Failed to verify Firebase token", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (Exception e) {
                logger.error("Error authenticating user", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
