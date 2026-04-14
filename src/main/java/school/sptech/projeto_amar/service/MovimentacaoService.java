package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.*;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.movimentacao.entrada.EntradaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.perda.PerdaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.saida.SaidaRequestDTO;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.EstoqueInsuficienteException;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.SemMovimentacoesException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.produtoLoteExceptions.LoteNaoEncontradoException;
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

    public List<Movimentacao> listarPorTipoEProdutoId(TipoMovimentacao tipo, Integer produtoId) {
        List<Movimentacao> retorno = repository.findByTipoAndProdutoId(tipo, produtoId);

        if (retorno.isEmpty()) {
            throw new SemMovimentacoesException();
        }

        return retorno;
    }

    public List<Movimentacao> listarPorProdutoId(Integer produtoId) {
        List<Movimentacao> retorno = repository.findByProdutoId(produtoId);

        if (retorno.isEmpty()) {
            throw new SemMovimentacoesException();
        }

        return retorno;
    }

    public List<Movimentacao> listarPorTipo(TipoMovimentacao tipo) {
        List<Movimentacao> retorno = repository.findByTipo(tipo);

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

    public Movimentacao saida(SaidaRequestDTO saida){
        Produto produto =
                produtoRepository.findById(saida.fkProduto()).orElseThrow(ProdutoNaoEncontradoException::new);

        Usuario usuario =
                usuarioRepository.findById(saida.fkUsuario()).orElseThrow(UsuarioNaoEncontradoException::new);

        Integer estoqueTotal = calcularEstoqueTotal(produto.getId());

        if (estoqueTotal < saida.quantidade()) {
            throw new EstoqueInsuficienteException(estoqueTotal);
        }

        Movimentacao mov = new Movimentacao();
        mov.setData(LocalDate.now());
        mov.setMotivo(saida.motivo());
        mov.setTipo(TipoMovimentacao.SAIDA);
        mov.setProduto(produto);
        mov.setQuantidade(saida.quantidade());
        mov.setUsuario(usuario);
        repository.save(mov);

        List<ProdutoLote> lotes = produtoLoteRepository
                .findByProdutoIdAndQuantidadeGreaterThanOrderByValidadeAsc(produto.getId(), 0);

        int quantidadeRestante = saida.quantidade();

        for (ProdutoLote lote : lotes) {
            if (quantidadeRestante <= 0) break;

            Integer retirar = Math.min(lote.getQuantidade(), quantidadeRestante);

            lote.setQuantidade(lote.getQuantidade() - retirar);
            produtoLoteRepository.save(lote);

            MovimentacaoEstoque movEstoque = new MovimentacaoEstoque();
            movEstoque.setMovimentacao(mov);
            movEstoque.setProdutoLote(lote);
            movEstoque.setQuantidade(retirar);
            movimentacaoEstoqueRepository.save(movEstoque);

            quantidadeRestante -= retirar;
        }

        return mov;
    }

    public Movimentacao perda(PerdaRequestDTO perda) {
        Produto produto = produtoRepository.findById(perda.fkProduto()).orElseThrow(ProdutoNaoEncontradoException::new);
        Usuario usuario = usuarioRepository.findById(perda.fkUsuario()).orElseThrow(UsuarioNaoEncontradoException::new);
        
        Integer estoqueTotal = calcularEstoqueTotal(perda.fkProduto());
        
        if (estoqueTotal < perda.quantidade()) {
            throw new EstoqueInsuficienteException(estoqueTotal);
        }
        
        ProdutoLote lote = produtoLoteRepository.findById(perda.fkLote()).orElseThrow(LoteNaoEncontradoException::new);

        if (lote.getQuantidade() < perda.quantidade()) {
            throw new EstoqueInsuficienteException(lote.getQuantidade());
        }
        lote.setQuantidade(lote.getQuantidade() - perda.quantidade());
        produtoLoteRepository.save(lote);

        Movimentacao mov = new Movimentacao();
        mov.setData(LocalDate.now());
        mov.setMotivo(perda.motivo());
        mov.setTipo(TipoMovimentacao.PERDA);
        mov.setProduto(produto);
        mov.setQuantidade(perda.quantidade());
        mov.setUsuario(usuario);
        repository.save(mov);

        MovimentacaoEstoque movEstoque = new MovimentacaoEstoque();
        movEstoque.setMovimentacao(mov);
        movEstoque.setQuantidade(perda.quantidade());
        movEstoque.setProdutoLote(lote);
        movimentacaoEstoqueRepository.save(movEstoque);

        return mov;
    }

    public Integer calcularEstoqueTotal(Integer produtoId) {
        return produtoLoteRepository.sumQuantidadeByProdutoId(produtoId);
    }
}
