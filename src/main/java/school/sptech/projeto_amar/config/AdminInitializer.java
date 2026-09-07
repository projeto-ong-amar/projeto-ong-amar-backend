package school.sptech.projeto_amar.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.repository.UsuarioRepository;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${security.bootstrap-admin.enabled:false}")
    private boolean bootstrapEnabled;
    @Value("${security.bootstrap-admin.nome:}")
    private String nome;
    @Value("${security.bootstrap-admin.email:}")
    private String email;
    @Value("${security.bootstrap-admin.senha:}")
    private String senha;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args){
        if (!bootstrapEnabled) {
            return;
        }

        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            throw new IllegalStateException("O bootstrap do administrador exige nome, email e senha via variáveis de ambiente");
        }

        boolean existeAdmin = usuarioRepository.findAllByTipoUsuarioAndStatus(TipoUsuario.ADMINISTRADOR, "ATIVO")
                .stream().findAny().isPresent();

        if (!existeAdmin) {
            Usuario admin = new Usuario();
            admin.setNome(nome);
            admin.setEmail(email);
            admin.setSenha(passwordEncoder.encode(senha));
            admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
            admin.setStatus("ATIVO");
            usuarioRepository.save(admin);
        }
    }

}
