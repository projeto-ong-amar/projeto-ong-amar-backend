package school.sptech.projeto_amar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import school.sptech.projeto_amar.service.AutenticacaoService;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguracao {
    @Value("${security.pepper}")
    private String pepper;

    @Autowired
    private AutenticacaoEntryPoint autenticacaoEntryPoint;

    private static final String[] URLS_PERMITIDAS = {
        "/usuarios/login",
            "/usuarios/logout",
            "/error",
            "/swagger-ui",
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
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(URLS_PERMITIDAS).permitAll()
                                .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                                .anyRequest().authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(autenticacaoEntryPoint))
                .addFilterBefore(autenticacaoFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();

        configuracao.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8080"
        ));
        configuracao.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setAllowCredentials(true);
        configuracao.setExposedHeaders(List.of(HttpHeaders.CONTENT_DISPOSITION));

        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/**", configuracao);
        return origem;
    }

}
