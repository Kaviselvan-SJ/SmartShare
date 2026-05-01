package com.smartshare.util.shortlink;

import com.smartshare.repository.ShortLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class ShortCodeGenerator {

    private static final String ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRIES = 10;
    
    private final SecureRandom random = new SecureRandom();
    private final ShortLinkRepository shortLinkRepository;

    public String generateUniqueCode() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String code = generateRandomCode();
            // Check uniqueness in database
            if (shortLinkRepository.findByShortCode(code).isEmpty()) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique short code after " + MAX_RETRIES + " attempts");
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }
}
