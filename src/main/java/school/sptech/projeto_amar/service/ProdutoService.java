package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.exception.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.ExisteProdutoByNomeException;
import school.sptech.projeto_amar.exception.ProdutoNaoEncontradoException;
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

    public Produto atualizar(Integer id, Produto p) {
        if (!repository.existsById(id)) {
            throw new ProdutoNaoEncontradoException();
        }

        Produto produtoExistente = repository.findById(id).orElseThrow(ProdutoNaoEncontradoException::new);
        Categoria categoria = categoriaRepository.findById(p.getCategoria().getId())
                .orElseThrow(() -> new CategoriaNaoEncontradaException(p.getCategoria().getId()));

        produtoExistente.setNome(p.getNome());
        produtoExistente.setCategoria(p.getCategoria());
        produtoExistente.setUnidadeMedida(p.getUnidadeMedida());
        produtoExistente.setCategoria(categoria);

        return repository.save(produtoExistente);
    }

    public void removerPorId(Integer id) {
        if (!repository.existsById(id)) {
            throw new ProdutoNaoEncontradoException();
        }
        repository.deleteById(id);
    }
}
