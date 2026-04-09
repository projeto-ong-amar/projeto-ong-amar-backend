package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.exception.usuarioExceptions.EmailJaCadastradoException;
import school.sptech.projeto_amar.exception.usuarioExceptions.SemUsuariosCadastradosException;
import school.sptech.projeto_amar.exception.usuarioExceptions.UsuarioNaoEncontradoException;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioService {
  private UsuarioRepository repository;

  public UsuarioService(UsuarioRepository repository) {
    this.repository = repository;
  }

  public Usuario cadastrar(Usuario u) {
    if (repository.existsByEmail(u.getEmail())) {
      throw new EmailJaCadastradoException();
    }
    repository.save(u);
    return u;
  }

  public List<Usuario> listarTodos() {
    List<Usuario> usuarios = repository.findAll();
    if (usuarios.isEmpty()) {
      throw new SemUsuariosCadastradosException();
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
    usuarioExistente.setSenha(u.getSenha());

    return repository.save(usuarioExistente);
  }

  public void remover(Integer id) {
    if (!repository.existsById(id)) {
      throw new UsuarioNaoEncontradoException();
    }

    repository.deleteById(id);
  }

}
