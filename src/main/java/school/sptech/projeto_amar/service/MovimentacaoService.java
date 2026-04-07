package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.*;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.request.EntradaRequestDTO;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.SemMovimentacoesException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.usuarioExceptions.UsuarioNaoEncontradoException;
import school.sptech.projeto_amar.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
public class MovimentacaoService {
    private MovimentacaoRepository repository;
    private ProdutoRepository produtoRepository;
    private UsuarioRepository usuarioRepository;
    private ProdutoLoteRepository produtoLoteRepository;
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public MovimentacaoService(MovimentacaoRepository repository, ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository, ProdutoLoteRepository produtoLoteRepository, MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoLoteRepository = produtoLoteRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    public List<Movimentacao> listar() {
        List<Movimentacao> retorno = repository.findAll();

        if (retorno.isEmpty()) {
            throw new SemMovimentacoesException();
        }

        return retorno;
    }

    public Movimentacao entrada(EntradaRequestDTO entrada) {
        Produto produto =
                produtoRepository.findById(entrada.fkProduto()).orElseThrow(ProdutoNaoEncontradoException::new);

        Usuario usuario =
                usuarioRepository.findById(entrada.fkUsuario()).orElseThrow(UsuarioNaoEncontradoException::new);

        ProdutoLote lote = new ProdutoLote();
        lote.setProduto(produto);
        lote.setDataEntrada(entrada.dataEntrada());
        lote.setQuantidade(entrada.quantidade());
        lote.setValidade(entrada.dataValidade());
        produtoLoteRepository.save(lote);

        Movimentacao mov = new Movimentacao();
        mov.setData(LocalDate.now());
        mov.setMotivo(entrada.motivo());
        mov.setTipo(TipoMovimentacao.ENTRADA);
        mov.setProduto(produto);
        mov.setQuantidade(entrada.quantidade());
        mov.setUsuario(usuario);
        repository.save(mov);

        MovimentacaoEstoque movEstoque = new MovimentacaoEstoque();
        movEstoque.setMovimentacao(mov);
        movEstoque.setProdutoLote(lote);
        movEstoque.setQuantidade(entrada.quantidade());
        movimentacaoEstoqueRepository.save(movEstoque);

        return mov;

    }
}
