package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.response.UsuarioResponseDTO;
import school.sptech.projeto_amar.exception.EmailJaCadastradoException;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.repository.UsuarioRepository;

@Service
public class UsuarioService {
  private UsuarioRepository repository;

  public UsuarioService(UsuarioRepository repository) {
    this.repository = repository;
  }

  public UsuarioResponseDTO cadastrar(Usuario u) {
    if (repository.existsByEmail(u.getEmail())) {
      throw new EmailJaCadastradoException();
    }
    Usuario novoUsuario = new Usuario();
    novoUsuario.setEmail(u.getEmail());
    novoUsuario.setNome(u.getNome());
    novoUsuario.setSenha(u.getSenha());
    repository.save(novoUsuario);

    return UsuarioMapping.toResponse(novoUsuario);
  }
  
}
