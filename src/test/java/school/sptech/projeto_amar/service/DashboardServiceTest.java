package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ProdutoLoteRepository produtoLoteRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private DashboardService service;

    @Test
    void listarEstoqueAtualPorProdutoRetornaCategoriaQuantidadeAtualEZeroSemLotes() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentos");

        Produto arroz = produto(1, "Arroz", categoria);
        Produto feijao = produto(2, "Feijão", categoria);

        when(produtoLoteRepository.sumQuantidadePorProduto())
                .thenReturn(List.<Object[]>of(new Object[]{1, 20L}));
        when(produtoRepository.findAllByStatusOrderByNomeAsc("ATIVO"))
                .thenReturn(List.of(arroz, feijao));

        var resultado = service.listarEstoqueAtualPorProduto();

        assertEquals(2, resultado.size());
        assertEquals("Arroz", resultado.get(0).produto());
        assertEquals("Alimentos", resultado.get(0).categoria());
        assertEquals(20L, resultado.get(0).quantidade());
        assertEquals("Feijão", resultado.get(1).produto());
        assertEquals("Alimentos", resultado.get(1).categoria());
        assertEquals(0L, resultado.get(1).quantidade());
    }

    private Produto produto(Integer id, String nome, Categoria categoria) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setCategoria(categoria);
        return produto;
    }
}
