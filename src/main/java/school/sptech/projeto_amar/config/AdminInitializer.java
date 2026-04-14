package school.sptech.projeto_amar.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.repository.UsuarioRepository;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args){
        boolean existeAdmin = usuarioRepository.findAllByTipoUsuarioAndStatus(TipoUsuario.ADMINISTRADOR, "ATIVO")
                .stream().findAny().isPresent();

        if (!existeAdmin) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail("admin@admin.com");
            admin.setSenha(passwordEncoder.encode("Admin123@"));
            admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
            admin.setStatus("ATIVO");
            usuarioRepository.save(admin);
            System.out.println(">>> PRIMEIRO USUÁRIO ADMINISTRADOR CRIADO COM SUCESSO");
        }
    }

}
