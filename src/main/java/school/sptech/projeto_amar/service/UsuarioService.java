package school.sptech.projeto_amar.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioTokenDTO;
import school.sptech.projeto_amar.exception.usuarioExceptions.EmailJaCadastradoException;
import school.sptech.projeto_amar.exception.usuarioExceptions.SemUsuariosCadastradosException;
import school.sptech.projeto_amar.exception.usuarioExceptions.TipoInvalidoException;
import school.sptech.projeto_amar.exception.usuarioExceptions.UsuarioNaoEncontradoException;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.util.List;
import java.util.Objects;

@Service
public class UsuarioService {
  private final UsuarioRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final GerenciadorTokenJwt gerenciadorTokenJwt;
  private final AuthenticationManager authenticationManager;

  public UsuarioService(UsuarioRepository repository,
                        PasswordEncoder passwordEncoder,
                        GerenciadorTokenJwt gerenciadorTokenJwt,
                        AuthenticationManager authenticationManager) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.gerenciadorTokenJwt = gerenciadorTokenJwt;
    this.authenticationManager = authenticationManager;
  }

  public Usuario cadastrar(Usuario u) {
    if (repository.existsByEmail(u.getEmail())) {
      throw new EmailJaCadastradoException();
    }

    String senhaHashed = passwordEncoder.encode(u.getSenha());
    u.setSenha(senhaHashed);
    u.setStatus("ATIVO");

    return repository.save(u);
  }

  public UsuarioTokenDTO login(String email, String senha) {
    UsernamePasswordAuthenticationToken credenciais = new UsernamePasswordAuthenticationToken(email, senha);

    Authentication auth = authenticationManager.authenticate(credenciais);

    String token = gerenciadorTokenJwt.generateToken(auth);

    Usuario usuario = repository.findByEmail(email).orElseThrow(UsuarioNaoEncontradoException::new);

    return UsuarioMapping.entityAndTokenToDTO(usuario, token);
  }

  public List<Usuario> listarTodos() {
    List<Usuario> usuarios = repository.findAllByStatus("ATIVO");
    if (usuarios.isEmpty()) {
      throw new SemUsuariosCadastradosException();
    }

    return usuarios;
  }

  public List<Usuario> listarPorTipo(String tipoUsuario) {
    if (!Objects.equals(tipoUsuario, TipoUsuario.ADMINISTRADOR.toString()) && !Objects.equals(tipoUsuario, TipoUsuario.VOLUNTARIO.toString())) {
      throw new TipoInvalidoException();
    }

    TipoUsuario tipo = TipoUsuario.valueOf(tipoUsuario);
    List<Usuario> usuarios = repository.findAllByTipoUsuarioAndStatus(tipo, "ATIVO");

    if (usuarios.isEmpty()) {
      throw new UsuarioNaoEncontradoException();
    }

    return usuarios;
  }

  public Usuario atualizar(Integer id, Usuario u) {
    if (!repository.existsById(id)) {
      throw new UsuarioNaoEncontradoException();
    }

    Usuario usuarioExistente = repository.findById(id).orElseThrow(UsuarioNaoEncontradoException::new);
    usuarioExistente.setNome(u.getNome());
    usuarioExistente.setEmail(u.getEmail());
    usuarioExistente.setSenha(passwordEncoder.encode(u.getSenha()));

    return repository.save(usuarioExistente);
  }

  public void remover(Integer id) {
    Usuario usuario = repository.findById(id).orElseThrow(UsuarioNaoEncontradoException::new);
    usuario.setStatus("INATIVO");
    repository.save(usuario);
  }

}
