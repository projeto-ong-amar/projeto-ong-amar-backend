package school.sptech.projeto_amar.mapper;

import org.junit.jupiter.api.Test;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.ProdutoLote;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProdutoLoteMappingTest {

    @Test
    void toAlimentoRiscoVencimentoResponseIncluiCategoriaEPreservaDadosDoLote() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentos");

        Produto produto = new Produto();
        produto.setNome("Arroz");
        produto.setCategoria(categoria);

        ProdutoLote lote = new ProdutoLote();
        lote.setId(1);
        lote.setProduto(produto);
        lote.setQuantidade(10);
        lote.setValidade(LocalDate.of(2026, 10, 20));

        var resultado = ProdutoLoteMapping.toAlimentoRiscoVencimentoResponse(lote);

        assertEquals(1, resultado.loteId());
        assertEquals("Arroz", resultado.produto());
        assertEquals("Alimentos", resultado.categoria());
        assertEquals(10, resultado.quantidade());
        assertEquals(LocalDate.of(2026, 10, 20), resultado.validade());
    }
}
