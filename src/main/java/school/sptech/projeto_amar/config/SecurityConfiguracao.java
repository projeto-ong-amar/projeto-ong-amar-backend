package school.sptech.projeto_amar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import school.sptech.projeto_amar.service.AutenticacaoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguracao {
    @Value("${security.pepper}")
    private String pepper;
    @Value("${security.cors.allowed-origins}")
    private List<String> allowedOrigins;
    @Value("${security.csrf.enabled}")
    private boolean csrfEnabled;
    @Value("${security.transport.require-https}")
    private boolean requireHttps;

    private final AutenticacaoEntryPoint autenticacaoEntryPoint;

    public SecurityConfiguracao(AutenticacaoEntryPoint autenticacaoEntryPoint) {
        this.autenticacaoEntryPoint = autenticacaoEntryPoint;
    }

    private static final String[] URLS_PERMITIDAS = {
            "/usuarios/login",
            "/usuarios/recuperacao-senha",
            "/usuarios/redefinir-senha",
            "/csrf",
            "/error",
            "/swagger-ui",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PepperPasswordEncoder(pepper);
    }

    @Bean
    public GerenciadorTokenJwt gerenciadorTokenJwt() {
        return new GerenciadorTokenJwt();
    }

    @Bean
    public AuthenticationManager authenticationManager(AutenticacaoService autenticacaoService,
            PasswordEncoder passwordEncoder) {
        AutenticacaoProvider provider = new AutenticacaoProvider(autenticacaoService, passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public AutenticacaoFilter autenticacaoFilter(GerenciadorTokenJwt gerenciadorTokenJwt,
            AutenticacaoService autenticacaoService) {
        return new AutenticacaoFilter(gerenciadorTokenJwt, autenticacaoService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AutenticacaoFilter autenticacaoFilter, CorsConfigurationSource corsConfigurationSource) throws Exception {
        if (csrfEnabled) {
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                    .ignoringRequestMatchers("/usuarios/login"));
        } else {
            http.csrf(AbstractHttpConfigurer::disable);
        }

        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(URLS_PERMITIDAS).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(autenticacaoEntryPoint))
                .addFilterBefore(autenticacaoFilter, UsernamePasswordAuthenticationFilter.class);
        if (requireHttps) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure())
                    .headers(headers -> headers.httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31_536_000)));
        }
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();

        configuracao.setAllowedOrigins(allowedOrigins);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        configuracao.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/**", configuracao);
        return origem;
    }

    private static final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final CsrfTokenRequestHandler plainTokenHandler = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xorTokenHandler = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           Supplier<CsrfToken> csrfToken) {
            xorTokenHandler.handle(request, response, csrfToken);
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());
            return StringUtils.hasText(headerValue)
                    ? plainTokenHandler.resolveCsrfTokenValue(request, csrfToken)
                    : xorTokenHandler.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}
