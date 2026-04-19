package school.sptech.projeto_amar.config;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PepperPasswordEncoder implements PasswordEncoder {
    private final Argon2PasswordEncoder argon2;
    private final byte[] pepperBytes;

    public PepperPasswordEncoder(String pepper) {
        this.argon2 = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        this.pepperBytes = pepper.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return argon2.encode(applyPepper(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return argon2.matches(applyPepper(rawPassword), encodedPassword);
    }

    private String applyPepper(CharSequence rawPassword) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepperBytes, "HmacSHA256"));
            byte[] hmac = mac.doFinal(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao aplicar pepper na senha", e);
        }
    }
}
