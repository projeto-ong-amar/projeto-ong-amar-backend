package school.sptech.projeto_amar.domain;

import jakarta.persistence.*;

@Entity
public class MovimentacaoEstoque {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    private Integer quantidade;

    @ManyToOne
    @JoinColumn(name = "fk_movimentacao")
    private Movimentacao movimentacao;

    @ManyToOne
    @JoinColumn(name="fk_produto_lote")
    private ProdutoLote produtoLote;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Movimentacao getMovimentacao() {
        return movimentacao;
    }

    public void setMovimentacao(Movimentacao movimentacao) {
        this.movimentacao = movimentacao;
    }

    public ProdutoLote getProdutoLote() {
        return produtoLote;
    }

    public void setProdutoLote(ProdutoLote produtoLote) {
        this.produtoLote = produtoLote;
    }
}
