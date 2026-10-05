package school.sptech.projeto_amar.mapper;

import org.junit.jupiter.api.Test;
import school.sptech.projeto_amar.domain.enums.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.ProdutoLote;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProdutoLoteMappingTest {

    @Test
    void aceitaProdutoSemCategoria() {
        Produto produto = new Produto();
        ProdutoLote lote = new ProdutoLote();
        lote.setProduto(produto);
        assertNull(ProdutoLoteMapping.toAlimentoRiscoVencimentoResponse(lote).categoria());
    }

    @Test
    void toAlimentoRiscoVencimentoResponseIncluiCategoriaEPreservaDadosDoLote() {
        Categoria categoria = Categoria.ALIMENTOS;

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
