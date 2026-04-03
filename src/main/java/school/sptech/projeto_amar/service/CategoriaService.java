package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.exception.CategoriaJaExisteException;
import school.sptech.projeto_amar.exception.SemCategoriasCadastradasException;
import school.sptech.projeto_amar.repository.CategoriaRepository;

import java.util.List;

@Service
public class CategoriaService {
    private CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public Categoria cadastrar(Categoria c) {
        if (repository.existsByNome(c.getNome())) {
            throw new CategoriaJaExisteException();
        }

        repository.save(c);
        return c;
    }

    public List<Categoria> listar() {
        List<Categoria> retorno = repository.findAll();

        if (retorno.isEmpty()) {
            throw new SemCategoriasCadastradasException();
        }

        return retorno;

    }
}
