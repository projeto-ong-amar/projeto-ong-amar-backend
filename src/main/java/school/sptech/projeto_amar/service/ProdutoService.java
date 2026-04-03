package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.exception.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.ExisteProdutoByNomeException;
import school.sptech.projeto_amar.exception.SemProdutosCadastradosException;
import school.sptech.projeto_amar.repository.CategoriaRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.util.List;

@Service
public class ProdutoService {
    private ProdutoRepository repository;
    private CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    public Produto cadastrar(Produto p) {
        if (repository.existsByNome(p.getNome())) {
            throw new ExisteProdutoByNomeException();
        }

        Categoria categoria = categoriaRepository.findById(p.getCategoria().getId())
                .orElseThrow(() -> new CategoriaNaoEncontradaException(p.getCategoria().getId()));
        p.setCategoria(categoria);

        return repository.save(p);
    }

    public List<Produto> listar() {
        List<Produto> retorno = repository.findAll();
        if (retorno.isEmpty()) {
            throw new SemProdutosCadastradosException();
        }

        return retorno;
    }
}
