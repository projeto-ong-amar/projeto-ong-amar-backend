package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.response.UsuarioResponseDTO;
import school.sptech.projeto_amar.exception.EmailJaCadastradoException;
import school.sptech.projeto_amar.exception.SemUsuariosCadastradosException;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

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
  
}
