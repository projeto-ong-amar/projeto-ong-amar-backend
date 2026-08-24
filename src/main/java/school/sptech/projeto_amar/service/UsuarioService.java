package school.sptech.projeto_amar.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.TokenRecuperacaoSenha;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioTokenDTO;
import school.sptech.projeto_amar.exception.usuarioExceptions.*;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.repository.TokenRecuperacaoSenhaRepository;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
public class UsuarioService {
  private final UsuarioRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final GerenciadorTokenJwt gerenciadorTokenJwt;
  private final AuthenticationManager authenticationManager;
  private final TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository;
  private final EmailService emailService;
  private final String urlRedefinicaoSenha;
  private final SecureRandom secureRandom = new SecureRandom();

  public UsuarioService(UsuarioRepository repository,
      PasswordEncoder passwordEncoder,
      GerenciadorTokenJwt gerenciadorTokenJwt,
      AuthenticationManager authenticationManager,
      TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository,
      EmailService emailService,
      @org.springframework.beans.factory.annotation.Value("${app.recuperacao-senha.url}") String urlRedefinicaoSenha) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.gerenciadorTokenJwt = gerenciadorTokenJwt;
    this.authenticationManager = authenticationManager;
    this.tokenRecuperacaoSenhaRepository = tokenRecuperacaoSenhaRepository;
    this.emailService = emailService;
    this.urlRedefinicaoSenha = urlRedefinicaoSenha;
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

  @Transactional
  public void solicitarRecuperacaoSenha(String email) {
    Usuario usuario = repository.findByEmail(email).orElse(null);
    if (usuario == null || !"ATIVO".equals(usuario.getStatus())) {
      return;
    }

    String token = gerarToken();
    tokenRecuperacaoSenhaRepository.deleteByUsuarioId(usuario.getId());

    TokenRecuperacaoSenha tokenRecuperacao = new TokenRecuperacaoSenha();
    tokenRecuperacao.setUsuario(usuario);
    tokenRecuperacao.setTokenHash(gerarHash(token));
    tokenRecuperacao.setExpiraEm(LocalDateTime.now().plusMinutes(15));
    tokenRecuperacao.setUtilizado(false);
    tokenRecuperacaoSenhaRepository.save(tokenRecuperacao);

    String link = UriComponentsBuilder.fromUriString(urlRedefinicaoSenha)
        .queryParam("token", token)
        .build()
        .toUriString();
    emailService.enviarRecuperacaoSenha(usuario.getEmail(), usuario.getNome(), link);
  }

  @Transactional
  public void redefinirSenha(String token, String novaSenha) {
    TokenRecuperacaoSenha tokenRecuperacao = tokenRecuperacaoSenhaRepository
        .findByTokenHashAndExpiraEmAfterAndUtilizadoFalse(gerarHash(token), LocalDateTime.now())
        .orElseThrow(() -> new IllegalArgumentException("Token de recuperação inválido ou expirado"));

    Usuario usuario = tokenRecuperacao.getUsuario();
    if (!"ATIVO".equals(usuario.getStatus())) {
      throw new IllegalArgumentException("Token de recuperação inválido ou expirado");
    }

    usuario.setSenha(passwordEncoder.encode(novaSenha));
    tokenRecuperacao.setUtilizado(true);
    repository.save(usuario);
    tokenRecuperacaoSenhaRepository.save(tokenRecuperacao);
  }

  public List<Usuario> listarTodos() {
    List<Usuario> usuarios = repository.findAllByStatus("ATIVO");
    if (usuarios.isEmpty()) {
      throw new SemUsuariosCadastradosException();
    }

    return usuarios;
  }

  public List<Usuario> listarPorTipo(String tipoUsuario) {
    if (!Objects.equals(tipoUsuario, TipoUsuario.ADMINISTRADOR.toString())
        && !Objects.equals(tipoUsuario, TipoUsuario.VOLUNTARIO.toString())) {
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

    if (usuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)) {
      List<Usuario> administradores = repository.findAllByTipoUsuarioAndStatus(TipoUsuario.ADMINISTRADOR, "Ativo");
      if (administradores.size() <= 1) {
        throw new ExcluirAdminException();
      }
    }
    usuario.setStatus("INATIVO");
    repository.save(usuario);
  }

  private String gerarToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String gerarHash(String valor) {
    try {
      byte[] hash = MessageDigest.getInstance("SHA-256")
          .digest(valor.getBytes(StandardCharsets.UTF_8));
      return java.util.HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 não está disponível", e);
    }
  }

}
