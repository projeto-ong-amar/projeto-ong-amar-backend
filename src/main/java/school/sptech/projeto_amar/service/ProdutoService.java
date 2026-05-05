package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.produtoExceptions.ExisteProdutoByNomeException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.produtoExceptions.SemProdutosCadastradosException;
import school.sptech.projeto_amar.repository.CategoriaRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.util.List;

@Service
public class ProdutoService {
    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_INATIVO = "INATIVO";

    private final ProdutoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    public Produto cadastrar(Produto p) {
        if (repository.existsByNomeAndStatus(p.getNome(), STATUS_ATIVO)) {
            throw new ExisteProdutoByNomeException();
        }

        Categoria categoria = categoriaRepository.findById(p.getCategoria().getId())
                .orElseThrow(() -> new CategoriaNaoEncontradaException(p.getCategoria().getId()));
        p.setCategoria(categoria);
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
        if (!categoriaRepository.existsByNome(categoria)) {
            throw new CategoriaNaoEncontradaException();
        }

        List<Produto> retorno = repository.findAllByCategoriaNomeAndStatus(categoria, STATUS_ATIVO);
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

        Categoria categoria = categoriaRepository.findById(p.getCategoria().getId())
                .orElseThrow(() -> new CategoriaNaoEncontradaException(p.getCategoria().getId()));

        produtoExistente.setNome(p.getNome());
        produtoExistente.setUnidadeMedida(p.getUnidadeMedida());
        produtoExistente.setCategoria(categoria);

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
