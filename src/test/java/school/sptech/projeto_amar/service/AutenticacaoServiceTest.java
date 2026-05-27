package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @InjectMocks
    private AutenticacaoService service;

    @Mock
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("João");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("senhaHash");
        usuario.setTipoUsuario(TipoUsuario.VOLUNTARIO);
        usuario.setStatus("ATIVO");
    }

    @Test
    @DisplayName("Deve retornar UserDetails quando email existente e usuário ATIVO")
    void deveRetornarUserDetails() {
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

        UserDetails result = service.loadUserByUsername("joao@email.com");

        assertNotNull(result);
        assertEquals("joao@email.com", result.getUsername());
    }

    @Test
    @DisplayName("Deve lançar exceção quando email não encontrado")
    void deveLancarExcecaoEmailNaoEncontrado() {
        when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("naoexiste@email.com"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário INATIVO")
    void deveLancarExcecaoUsuarioInativo() {
        usuario.setStatus("INATIVO");
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("joao@email.com"));
    }
}

