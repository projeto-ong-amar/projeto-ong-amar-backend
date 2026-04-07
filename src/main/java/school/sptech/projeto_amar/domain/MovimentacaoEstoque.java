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


    // precisa ser criado
//    @ManyToOne
//    @JoinColumn(name="fk_produto_lote")
//    private ProdutoLote produtoLote;
}
