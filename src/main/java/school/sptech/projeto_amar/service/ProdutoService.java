package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.enums.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.produtoExceptions.ExisteProdutoByNomeException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.produtoExceptions.SemProdutosCadastradosException;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.util.List;

@Service
public class ProdutoService {
    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_INATIVO = "INATIVO";

    private final ProdutoRepository repository;
    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto cadastrar(Produto p) {
        if (repository.existsByNomeAndStatus(p.getNome(), STATUS_ATIVO)) {
            throw new ExisteProdutoByNomeException();
        }

        p.setStatus(STATUS_ATIVO);

        return repository.save(p);
    }

    public List<Produto> listar() {
        List<Produto> retorno = repository.findAllByStatus(STATUS_ATIVO);
        if (retorno.isEmpty()) {
            throw new SemProdutosCadastradosException();
        }

        return retorno;
    }

    public List<Produto> listarPorCategoria(String categoria) {
        Categoria tipo;
        try {
            tipo = Categoria.fromValor(categoria);
        } catch (IllegalArgumentException ex) {
            throw new CategoriaNaoEncontradaException();
        }

        List<Produto> retorno = repository.findAllByCategoriaAndStatus(tipo, STATUS_ATIVO);
        if (retorno.isEmpty()) {
            throw new SemProdutosCadastradosException();
        }
        return retorno;
    }

    public Produto atualizar(Integer id, Produto p) {
        Produto produtoExistente = repository.findById(id)
                .orElseThrow(ProdutoNaoEncontradoException::new);

        if (STATUS_INATIVO.equals(produtoExistente.getStatus())) {
            throw new ProdutoNaoEncontradoException();
        }

        produtoExistente.setNome(p.getNome());
        produtoExistente.setUnidadeMedida(p.getUnidadeMedida());
        produtoExistente.setCategoria(p.getCategoria());

        return repository.save(produtoExistente);
    }

    public void removerPorId(Integer id) {
        Produto produto = repository.findById(id).orElseThrow(
                ProdutoNaoEncontradoException::new
        );

        if (STATUS_INATIVO.equals(produto.getStatus())) {
            throw new ProdutoNaoEncontradoException();
        }

        produto.setStatus(STATUS_INATIVO);
        repository.save(produto);
    }
}
