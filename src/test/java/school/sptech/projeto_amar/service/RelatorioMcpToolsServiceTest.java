package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.sptech.projeto_amar.domain.enums.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.ProdutoLote;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioMcpToolsServiceTest {

  @Mock
  private MovimentacaoRepository movimentacaoRepository;

  @Mock
  private ProdutoLoteRepository produtoLoteRepository;

  private RelatorioMcpToolsService service;

  private LocalDate inicio;
  private LocalDate fim;

  @BeforeEach
  void setUp() {
    service = new RelatorioMcpToolsService(movimentacaoRepository, produtoLoteRepository);
    inicio = LocalDate.of(2026, 1, 1);
    fim = LocalDate.of(2026, 1, 31);
  }

  @Test
  @DisplayName("consultarItensEntradaPorPeriodo: retorna quantidade da base e 0 para datas nulas")
  void deveConsultarItensEntradaPorPeriodo() {
    when(movimentacaoRepository.sumQuantidadeByTipoAndPeriodo(
        TipoMovimentacao.ENTRADA, inicio, fim)).thenReturn(10);

    assertEquals(10, service.consultarItensEntradaPorPeriodo(inicio, fim));
    assertEquals(0, service.consultarItensEntradaPorPeriodo(null, fim));
  }

  @Test
  @DisplayName("consultarItensSaidaPorPeriodo: retorna quantidade da base e 0 para datas nulas")
  void deveConsultarItensSaidaPorPeriodo() {
    when(movimentacaoRepository.sumQuantidadeByTipoAndPeriodo(
        TipoMovimentacao.SAIDA, inicio, fim)).thenReturn(4);

    assertEquals(4, service.consultarItensSaidaPorPeriodo(inicio, fim));
    assertEquals(0, service.consultarItensSaidaPorPeriodo(inicio, null));
  }

  @Test
  @DisplayName("consultarItensEmEstoque: retorna soma total dos lotes")
  void deveConsultarItensEmEstoque() {
    when(produtoLoteRepository.sumQuantidadeTotal()).thenReturn(25);

    assertEquals(25, service.consultarItensEmEstoque());
  }

  @Test
  @DisplayName("consultarProdutosEmRiscoVencimento: formata lista de lotes quando houver")
  void deveListarProdutosEmRisco() {
    Produto produto = new Produto();
    produto.setNome("Arroz");
    Categoria categoria = Categoria.ALIMENTOS;
    produto.setCategoria(categoria);

    ProdutoLote lote = new ProdutoLote();
    lote.setProduto(produto);
    lote.setQuantidade(5);
    lote.setValidade(LocalDate.of(2026, 1, 15));

    when(produtoLoteRepository.findAlimentosComRiscoDeVencimento(inicio, fim))
        .thenReturn(List.of(lote));

    String resultado = service.consultarProdutosEmRiscoVencimento(inicio, fim);

    assertTrue(resultado.contains("Arroz"));
    assertTrue(resultado.contains("5"));
  }

  @Test
  @DisplayName("consultarProdutosEmRiscoVencimento: indica ausencia de risco quando nao houver lotes")
  void deveIndicarSemRiscoQuandoVazio() {
    when(produtoLoteRepository.findAlimentosComRiscoDeVencimento(inicio, fim))
        .thenReturn(List.of());

    String resultado = service.consultarProdutosEmRiscoVencimento(inicio, fim);

    assertTrue(resultado.contains("Nenhum produto em risco"));
    verify(produtoLoteRepository).findAlimentosComRiscoDeVencimento(inicio, fim);
  }

  @Test
  @DisplayName("consultarProdutosEmRiscoVencimento: retorna texto padrao para datas nulas sem consultar base")
  void deveRetornarTextoSemDatas() {
    String resultado = service.consultarProdutosEmRiscoVencimento(null, null);

    assertEquals("Sem dados.", resultado);
    verify(produtoLoteRepository, never())
        .findAlimentosComRiscoDeVencimento(any(), any());
  }
}
