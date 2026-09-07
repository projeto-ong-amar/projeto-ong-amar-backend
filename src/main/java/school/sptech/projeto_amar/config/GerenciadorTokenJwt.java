package school.sptech.projeto_amar.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GerenciadorTokenJwt {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.validity}")
    private Long validity;

    public String generateToken(final Authentication authentication) {
        final String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .claim("tokenVersion", getTokenVersion(authentication))
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + validity * 1_000))
                .signWith(parseSecret())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimForToken(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && getTokenVersion(token) == getTokenVersion(userDetails);
    }

    public <T> T getClaimForToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Date expirationDate = getClaimForToken(token, Claims::getExpiration);
        return expirationDate.before(new Date(System.currentTimeMillis()));
    }

    private int getTokenVersion(String token) {
        Integer tokenVersion = getClaimForToken(token, claims -> claims.get("tokenVersion", Integer.class));
        return tokenVersion == null ? -1 : tokenVersion;
    }

    private int getTokenVersion(UserDetails userDetails) {
        return userDetails instanceof UsuarioDetalhesDTO usuario ? usuario.getVersaoToken() : -1;
    }

    private int getTokenVersion(Authentication authentication) {
        return authentication.getPrincipal() instanceof UsuarioDetalhesDTO usuario ? usuario.getVersaoToken() : 0;
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(parseSecret())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey parseSecret() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
    }
}
