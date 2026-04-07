package school.sptech.projeto_amar.domain;

import jakarta.persistence.*;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Movimentacao {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo;

    private Integer quantidade;
    private LocalDateTime data;
    private String motivo;

    @ManyToOne
    @JoinColumn(name="fk_produto")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name="fk_usuario")
    private Usuario usuario;

    @OneToMany(mappedBy = "movimentacao", cascade = CascadeType.ALL)
    private List<MovimentacaoEstoque> itens;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<MovimentacaoEstoque> getItens() {
        return itens;
    }

    public void setItens(List<MovimentacaoEstoque> itens) {
        this.itens = itens;
    }
}
