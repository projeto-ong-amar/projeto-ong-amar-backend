package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioTokenDTO;
import school.sptech.projeto_amar.exception.usuarioExceptions.*;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService service;

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GerenciadorTokenJwt gerenciadorTokenJwt;

    @Mock
    private AuthenticationManager authenticationManager;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("Senha@123");
        usuario.setTipoUsuario(TipoUsuario.VOLUNTARIO);
        usuario.setStatus("ATIVO");
    }

    @Nested
    @DisplayName("cadastrar()")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar usuário com dados válidos")
        void deveCadastrarComSucesso() {
            when(repository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("senhaHasheada");
            when(repository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario resultado = service.cadastrar(usuario);

            assertNotNull(resultado);
            assertEquals("ATIVO", resultado.getStatus());
            verify(repository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já cadastrado")
        void deveLancarExcecaoEmailDuplicado() {
            when(repository.existsByEmail(anyString())).thenReturn(true);

            assertThrows(EmailJaCadastradoException.class, () -> service.cadastrar(usuario));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Deve retornar token com credenciais válidas")
        void deveRetornarTokenComSucesso() {
            Authentication auth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(gerenciadorTokenJwt.generateToken(auth)).thenReturn("token-jwt");
            when(repository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

            UsuarioTokenDTO resultado = service.login("joao@email.com", "Senha@123");

            assertNotNull(resultado);
            assertEquals("token-jwt", resultado.token());
            assertEquals("joao@email.com", resultado.email());
        }

        @Test
        @DisplayName("Deve lançar exceção com credenciais inválidas")
        void deveLancarExcecaoCredenciaisInvalidas() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            assertThrows(BadCredentialsException.class,
                    () -> service.login("joao@email.com", "senhaErrada"));
        }

        @Test
        @DisplayName("Deve retornar falha genérica quando email não encontrado após autenticação")
        void deveLancarExcecaoEmailNaoEncontrado() {
            Authentication auth = mock(Authentication.class);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(gerenciadorTokenJwt.generateToken(auth)).thenReturn("token");
            when(repository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

            assertThrows(BadCredentialsException.class,
                    () -> service.login("joao@email.com", "Senha@123"));
        }
    }

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar lista de usuários ativos")
        void deveRetornarLista() {
            when(repository.findAllByStatus("ATIVO")).thenReturn(List.of(usuario));

            List<Usuario> resultado = service.listarTodos();

            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Deve lançar exceção quando não há usuários ativos")
        void deveLancarExcecaoSemUsuarios() {
            when(repository.findAllByStatus("ATIVO")).thenReturn(Collections.emptyList());

            assertThrows(SemUsuariosCadastradosException.class, () -> service.listarTodos());
        }
    }

    @Nested
    @DisplayName("listarPorTipo()")
    class ListarPorTipo {

        @Test
        @DisplayName("Deve retornar lista filtrada por ADMINISTRADOR")
        void deveRetornarListaAdmin() {
            usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
            when(repository.findAllByTipoUsuarioAndStatus(TipoUsuario.ADMINISTRADOR, "ATIVO"))
                    .thenReturn(List.of(usuario));

            List<Usuario> resultado = service.listarPorTipo("ADMINISTRADOR");

            assertFalse(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista filtrada por VOLUNTARIO")
        void deveRetornarListaVoluntario() {
            when(repository.findAllByTipoUsuarioAndStatus(TipoUsuario.VOLUNTARIO, "ATIVO"))
                    .thenReturn(List.of(usuario));

            List<Usuario> resultado = service.listarPorTipo("VOLUNTARIO");

            assertFalse(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção para tipo inválido")
        void deveLancarExcecaoTipoInvalido() {
            assertThrows(TipoInvalidoException.class, () -> service.listarPorTipo("INVALIDO"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando tipo válido sem resultados")
        void deveLancarExcecaoSemResultados() {
            when(repository.findAllByTipoUsuarioAndStatus(TipoUsuario.VOLUNTARIO, "ATIVO"))
                    .thenReturn(Collections.emptyList());

            assertThrows(UsuarioNaoEncontradoException.class,
                    () -> service.listarPorTipo("VOLUNTARIO"));
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar usuário com ID existente")
        void deveAtualizarComSucesso() {
            when(repository.existsById(1)).thenReturn(true);
            when(repository.findById(1)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode(anyString())).thenReturn("novaSenhaHash");
            when(repository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario novosDados = new Usuario();
            novosDados.setNome("Novo Nome");
            novosDados.setEmail("novo@email.com");
            novosDados.setSenha("NovaSenha@1");

            Usuario resultado = service.atualizar(1, novosDados);

            assertNotNull(resultado);
            verify(repository).save(any(Usuario.class));
            assertEquals(1, usuario.getVersaoToken());
        }

        @Test
        @DisplayName("Deve lançar exceção para ID inexistente")
        void deveLancarExcecaoIdInexistente() {
            when(repository.existsById(99)).thenReturn(false);

            Usuario novosDados = new Usuario();
            assertThrows(UsuarioNaoEncontradoException.class,
                    () -> service.atualizar(99, novosDados));
        }
    }

    @Nested
    @DisplayName("remover()")
    class Remover {

        @Test
        @DisplayName("Deve inativar usuário VOLUNTARIO existente")
        void deveInativarVoluntario() {
            usuario.setTipoUsuario(TipoUsuario.VOLUNTARIO);
            when(repository.findById(1)).thenReturn(Optional.of(usuario));
            when(repository.save(any(Usuario.class))).thenReturn(usuario);

            service.remover(1);

            verify(repository).save(argThat(u -> "INATIVO".equals(u.getStatus())));
            assertEquals(1, usuario.getVersaoToken());
        }

        @Test
        @DisplayName("Deve invalidar tokens ao encerrar sessão")
        void deveInvalidarTokensAoEncerrarSessao() {
            when(repository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

            service.invalidarTokens("joao@email.com");

            assertEquals(1, usuario.getVersaoToken());
            verify(repository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar exceção para ID inexistente")
        void deveLancarExcecaoIdInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());

            assertThrows(UsuarioNaoEncontradoException.class, () -> service.remover(99));
        }

        @Test
        @DisplayName("Deve lançar exceção ao remover último ADMINISTRADOR")
        void deveLancarExcecaoUltimoAdmin() {
            usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
            when(repository.findById(1)).thenReturn(Optional.of(usuario));
            when(repository.findAllByTipoUsuarioAndStatus(TipoUsuario.ADMINISTRADOR, "ATIVO"))
                    .thenReturn(List.of(usuario));

            assertThrows(ExcluirAdminException.class, () -> service.remover(1));
        }

        @Test
        @DisplayName("Deve inativar ADMINISTRADOR quando existem outros admins")
        void deveInativarAdminQuandoExistemOutros() {
            usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
            Usuario outroAdmin = new Usuario();
            outroAdmin.setId(2);
            outroAdmin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

            when(repository.findById(1)).thenReturn(Optional.of(usuario));
            when(repository.findAllByTipoUsuarioAndStatus(TipoUsuario.ADMINISTRADOR, "ATIVO"))
                    .thenReturn(List.of(usuario, outroAdmin));
            when(repository.save(any(Usuario.class))).thenReturn(usuario);

            service.remover(1);

            verify(repository).save(argThat(u -> "INATIVO".equals(u.getStatus())));
        }
    }
}
