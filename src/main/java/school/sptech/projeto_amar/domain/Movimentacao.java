package school.sptech.projeto_amar.domain;

import jakarta.persistence.*;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Movimentacao {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo;

    private Integer quantidade;
    private LocalDate data;
    private String motivo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="fk_produto")
    private Produto produto;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
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
