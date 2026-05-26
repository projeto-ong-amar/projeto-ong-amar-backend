package school.sptech.projeto_amar.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.sptech.projeto_amar.domain.*;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.movimentacao.entrada.EntradaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.entrada.MovimentacaoHistoricoResponseDTO;
import school.sptech.projeto_amar.dto.movimentacao.perda.PerdaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.saida.SaidaRequestDTO;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.EstoqueInsuficienteException;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.MovimentacaoNaoEncontradaException;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.SemMovimentacoesException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.produtoLoteExceptions.LoteNaoEncontradoException;
import school.sptech.projeto_amar.exception.usuarioExceptions.UsuarioNaoEncontradoException;
import school.sptech.projeto_amar.repository.*;

import org.springframework.data.domain.Pageable;
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

    public Page<Movimentacao> listarHistoricoEntrada(Pageable pageable) {
        Page<Movimentacao> page = repository.findByTipoOrderByDataDesc(TipoMovimentacao.ENTRADA, pageable);
        return page;
    }

    public Page<Movimentacao> listarHistoricoSaida(Pageable pageable) {
        Page<Movimentacao> page = repository.findByTipoInOrderByDataDesc(
                List.of(TipoMovimentacao.SAIDA, TipoMovimentacao.PERDA), pageable);
        return page;
    }

    @Transactional
    public Movimentacao atualizarEntrada(EntradaRequestDTO entrada, Integer id) {
        Movimentacao m = repository.findById(id).orElseThrow(() ->
                new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

        if (m.getTipo() != TipoMovimentacao.ENTRADA) {
            throw new MovimentacaoNaoEncontradaException("A movimentação informada não é uma entrada");
        }

        Produto produto = produtoRepository.findById(entrada.fkProduto())
                .orElseThrow(ProdutoNaoEncontradoException::new);

        Usuario usuario = usuarioRepository.findById(entrada.fkUsuario())
                .orElseThrow(UsuarioNaoEncontradoException::new);

        MovimentacaoEstoque movEstoque = movimentacaoEstoqueRepository
                .findFirstByMovimentacaoId(id)
                .orElseThrow(() -> new MovimentacaoNaoEncontradaException("Item de estoque da movimentação não encontrado"));

        ProdutoLote lote = movEstoque.getProdutoLote();

        Integer diferencaQuantidade = entrada.quantidade() - m.getQuantidade();
        Integer quantidadeAtualLote = lote.getQuantidade();

        if (quantidadeAtualLote + diferencaQuantidade < 0) {
            throw new EstoqueInsuficienteException(quantidadeAtualLote);
        }

        lote.setQuantidade(quantidadeAtualLote + diferencaQuantidade);
        lote.setDataEntrada(entrada.dataEntrada());
        lote.setValidade(entrada.dataValidade());
        lote.setProduto(produto);
        produtoLoteRepository.save(lote);

        m.setMotivo(entrada.motivo());
        m.setQuantidade(entrada.quantidade());
        m.setProduto(produto);
        m.setUsuario(usuario);
        repository.save(m);

        movEstoque.setQuantidade(entrada.quantidade());
        movimentacaoEstoqueRepository.save(movEstoque);

        return m;
    }

    @Transactional
    public void removerEntrada(Integer id) {
        Movimentacao m = repository.findById(id).orElseThrow(() ->
                new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

        if (m.getTipo() != TipoMovimentacao.ENTRADA) {
            throw new MovimentacaoNaoEncontradaException("A movimentação informada não é uma entrada");
        }

        MovimentacaoEstoque movEstoque = movimentacaoEstoqueRepository
                .findFirstByMovimentacaoId(id)
                .orElseThrow(() -> new MovimentacaoNaoEncontradaException("Item de estoque da movimentação não encontrado"));

        ProdutoLote lote = movEstoque.getProdutoLote();
        int quantidadeResultante = lote.getQuantidade() - movEstoque.getQuantidade();

        if (quantidadeResultante < 0) {
            throw new EstoqueInsuficienteException(lote.getQuantidade());
        }

        lote.setQuantidade(quantidadeResultante);
        produtoLoteRepository.save(lote);

        movimentacaoEstoqueRepository.delete(movEstoque);
        repository.delete(m);
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

    @Transactional
    public Movimentacao atualizarSaida(SaidaRequestDTO saida, Integer id) {
        Movimentacao m = repository.findById(id).orElseThrow(() ->
                new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

        if (m.getTipo() != TipoMovimentacao.SAIDA) {
            throw new MovimentacaoNaoEncontradaException("A movimentação informada não é uma saída");
        }

        Produto produto = produtoRepository.findById(saida.fkProduto())
                .orElseThrow(ProdutoNaoEncontradoException::new);

        Usuario usuario = usuarioRepository.findById(saida.fkUsuario())
                .orElseThrow(UsuarioNaoEncontradoException::new);

        List<MovimentacaoEstoque> itensAntigos = movimentacaoEstoqueRepository.findByMovimentacaoId(id);

        if (itensAntigos.isEmpty()) {
            throw new MovimentacaoNaoEncontradaException("Itens de estoque da movimentação não encontrados");
        }

        for (MovimentacaoEstoque item : itensAntigos) {
            ProdutoLote loteAntigo = item.getProdutoLote();
            loteAntigo.setQuantidade(loteAntigo.getQuantidade() + item.getQuantidade());
            produtoLoteRepository.save(loteAntigo);
        }

        movimentacaoEstoqueRepository.deleteAll(itensAntigos);

        Integer estoqueTotal = calcularEstoqueTotal(produto.getId());

        if (estoqueTotal < saida.quantidade()) {
            throw new EstoqueInsuficienteException(estoqueTotal);
        }

        List<ProdutoLote> lotes = produtoLoteRepository
                .findByProdutoIdAndQuantidadeGreaterThanOrderByValidadeAsc(produto.getId(), 0);

        int quantidadeRestante = saida.quantidade();

        for (ProdutoLote lote : lotes) {
            if (quantidadeRestante <= 0) break;

            Integer retirar = Math.min(lote.getQuantidade(), quantidadeRestante);

            lote.setQuantidade(lote.getQuantidade() - retirar);
            produtoLoteRepository.save(lote);

            MovimentacaoEstoque novoItem = new MovimentacaoEstoque();
            novoItem.setMovimentacao(m);
            novoItem.setProdutoLote(lote);
            novoItem.setQuantidade(retirar);
            movimentacaoEstoqueRepository.save(novoItem);

            quantidadeRestante -= retirar;
        }

        m.setMotivo(saida.motivo());
        m.setQuantidade(saida.quantidade());
        m.setProduto(produto);
        m.setUsuario(usuario);
        repository.save(m);

        return m;
    }

    @Transactional
    public void removerSaida(Integer id) {
        Movimentacao m = repository.findById(id).orElseThrow(() ->
                new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

        if (m.getTipo() != TipoMovimentacao.SAIDA) {
            throw new MovimentacaoNaoEncontradaException("A movimentação informada não é uma saída");
        }

        List<MovimentacaoEstoque> itens = movimentacaoEstoqueRepository.findByMovimentacaoId(id);

        if (itens.isEmpty()) {
            throw new MovimentacaoNaoEncontradaException("Itens de estoque da movimentação não encontrados");
        }

        for (MovimentacaoEstoque item : itens) {
            ProdutoLote lote = item.getProdutoLote();
            lote.setQuantidade(lote.getQuantidade() + item.getQuantidade());
            produtoLoteRepository.save(lote);
        }

        movimentacaoEstoqueRepository.deleteAll(itens);
        repository.delete(m);
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

    @Transactional
    public Movimentacao atualizarPerda(PerdaRequestDTO perda, Integer id) {
        Movimentacao m = repository.findById(id).orElseThrow(() ->
                new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

        if (m.getTipo() != TipoMovimentacao.PERDA) {
            throw new MovimentacaoNaoEncontradaException("A movimentação informada não é uma perda");
        }

        Produto produto = produtoRepository.findById(perda.fkProduto())
                .orElseThrow(ProdutoNaoEncontradoException::new);

        Usuario usuario = usuarioRepository.findById(perda.fkUsuario())
                .orElseThrow(UsuarioNaoEncontradoException::new);

        ProdutoLote novoLote = produtoLoteRepository.findById(perda.fkLote())
                .orElseThrow(LoteNaoEncontradoException::new);

        MovimentacaoEstoque movEstoque = movimentacaoEstoqueRepository
                .findFirstByMovimentacaoId(id)
                .orElseThrow(() -> new MovimentacaoNaoEncontradaException("Item de estoque da movimentação não encontrado"));

        ProdutoLote loteAnterior = movEstoque.getProdutoLote();
        loteAnterior.setQuantidade(loteAnterior.getQuantidade() + movEstoque.getQuantidade());
        produtoLoteRepository.save(loteAnterior);

        Integer estoqueTotal = calcularEstoqueTotal(produto.getId());

        if (estoqueTotal < perda.quantidade()) {
            throw new EstoqueInsuficienteException(estoqueTotal);
        }

        if (novoLote.getQuantidade() < perda.quantidade()) {
            throw new EstoqueInsuficienteException(novoLote.getQuantidade());
        }

        novoLote.setQuantidade(novoLote.getQuantidade() - perda.quantidade());
        produtoLoteRepository.save(novoLote);

        m.setMotivo(perda.motivo());
        m.setQuantidade(perda.quantidade());
        m.setProduto(produto);
        m.setUsuario(usuario);
        repository.save(m);

        movEstoque.setProdutoLote(novoLote);
        movEstoque.setQuantidade(perda.quantidade());
        movimentacaoEstoqueRepository.save(movEstoque);

        return m;
    }

    @Transactional
    public void removerPerda(Integer id) {
        Movimentacao m = repository.findById(id).orElseThrow(() ->
                new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

        if (m.getTipo() != TipoMovimentacao.PERDA) {
            throw new MovimentacaoNaoEncontradaException("A movimentação informada não é uma perda");
        }

        MovimentacaoEstoque movEstoque = movimentacaoEstoqueRepository
                .findFirstByMovimentacaoId(id)
                .orElseThrow(() -> new MovimentacaoNaoEncontradaException("Item de estoque da movimentação não encontrado"));

        ProdutoLote lote = movEstoque.getProdutoLote();
        lote.setQuantidade(lote.getQuantidade() + movEstoque.getQuantidade());
        produtoLoteRepository.save(lote);

        movimentacaoEstoqueRepository.delete(movEstoque);
        repository.delete(m);
    }

    public Integer calcularEstoqueTotal(Integer produtoId) {
        Integer quantidade = produtoLoteRepository.sumQuantidadeByProdutoId(produtoId);
        return quantidade == null ? 0 : quantidade;
    }
}
