package school.sptech.projeto_amar.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import school.sptech.projeto_amar.service.AutenticacaoService;

import java.io.IOException;

public class AutenticacaoFilter extends OncePerRequestFilter {
    private final GerenciadorTokenJwt gerenciadorTokenJwt;
    private final AutenticacaoService autenticacaoService;

    public AutenticacaoFilter(GerenciadorTokenJwt gerenciadorTokenJwt, AutenticacaoService autenticacaoService) {
        this.gerenciadorTokenJwt = gerenciadorTokenJwt;
        this.autenticacaoService = autenticacaoService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extrairTokenDoCookie(request);

        if (token != null) {
            try {
                String email = gerenciadorTokenJwt.getUsernameFromToken(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = autenticacaoService.loadUserByUsername(email);

                    if (gerenciadorTokenJwt.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null,
                                        userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (JwtException | AuthenticationException | IllegalArgumentException e) {
                logger.debug("Token JWT inválido");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extrairTokenDoCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
