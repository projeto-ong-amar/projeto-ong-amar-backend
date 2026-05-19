package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.dto.categoria.CategoriaRequestDTO;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaJaExisteException;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.categoriaExceptions.SemCategoriasCadastradasException;
import school.sptech.projeto_amar.repository.CategoriaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {
    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_INATIVO = "INATIVO";

    private CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public Categoria cadastrar(Categoria c) {
        if (repository.existsByNome(c.getNome())) {
            throw new CategoriaJaExisteException();
        }

        c.setStatus(STATUS_ATIVO);

        repository.save(c);
        return c;
    }

    public List<Categoria> listar() {
        List<Categoria> retorno = repository.findAll().stream()
                .filter(this::isAtiva)
                .collect(Collectors.toList());

        if (retorno.isEmpty()) {
            throw new SemCategoriasCadastradasException();
        }

        return retorno;

    }

    public Categoria atualizar(Integer id, CategoriaRequestDTO categoria) {
        Categoria c = repository.findById(id).orElseThrow(CategoriaNaoEncontradaException::new);

        if (!isAtiva(c)) {
            throw new CategoriaNaoEncontradaException();
        }

        c.setNome(categoria.nome());
        c.setDescricao(categoria.descricao());
        return repository.save(c);
    }

    public void remover(Integer id) {
        Categoria categoria = repository.findById(id).orElseThrow(CategoriaNaoEncontradaException::new);

        if (!isAtiva(categoria)) {
            throw new CategoriaNaoEncontradaException();
        }

        categoria.setStatus(STATUS_INATIVO);
        repository.save(categoria);
    }

    private boolean isAtiva(Categoria categoria) {
        return STATUS_ATIVO.equals(categoria.getStatus());
    }
}
