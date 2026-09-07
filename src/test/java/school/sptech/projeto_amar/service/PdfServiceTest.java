package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;
import school.sptech.projeto_amar.dto.assistente.TabelaRelatorioDTO;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfServiceTest {

  private final PdfService service = new PdfService();

  @Test
  @DisplayName("gerarPdf: deve gerar bytes validos para relatorio comum")
  void deveGerarPdfParaRelatorioComum() {
    RelatorioAssistenteDTO dto = new RelatorioAssistenteDTO(
        "Resumo executivo do estoque",
        "desempenho-geral",
        "alta",
        List.of("Analise 1", "Analise 2"),
        List.of("Acao 1", "Acao 2"),
        null);

    byte[] bytes = service.gerarPdf(dto);

    assertTrue(bytes.length > 0);
    assertTrue(isPdf(bytes));
  }

  @Test
  @DisplayName("gerarPdf: deve gerar PDF com tabelas quando o relatorio as possui")
  void deveGerarPdfComTabelas() {
    TabelaRelatorioDTO tabelaIndicadores = new TabelaRelatorioDTO(
        "Indicadores gerais",
        List.of("Indicador", "Valor"),
        List.of(
            List.of("Produtos cadastrados", "120"),
            List.of("Lotes vencidos", "3")));
    TabelaRelatorioDTO tabelaTopProdutos = new TabelaRelatorioDTO(
        "Top produtos por saídas",
        List.of("Produto", "Quantidade movimentada"),
        List.of(List.of("Arroz", "80")));

    RelatorioAssistenteDTO dto = new RelatorioAssistenteDTO(
        "Estoque exige atenção por lotes vencidos.",
        "desempenho-geral",
        "alta",
        List.of("Analise 1"),
        List.of("Acao 1"),
        List.of(tabelaIndicadores, tabelaTopProdutos));

    byte[] bytes = service.gerarPdf(dto);

    assertTrue(bytes.length > 0);
    assertTrue(isPdf(bytes));
  }

  @Test
  @DisplayName("gerarPdf: deve gerar PDF em modo contingencia com campos nulos e tabelas invalidas")
  void deveGerarPdfModoContingenciaComCamposNulos() {
    TabelaRelatorioDTO semLinhas = new TabelaRelatorioDTO("Vazia", List.of("Coluna"), List.of());

    RelatorioAssistenteDTO dto = new RelatorioAssistenteDTO(
        null,
        null,
        null,
        List.of("Este relatorio nao foi gerado por IA devido a limite de cota"),
        null,
        List.of(semLinhas));

    byte[] bytes = service.gerarPdf(dto);

    assertTrue(bytes.length > 0);
    assertTrue(isPdf(bytes));
    assertFalse(new String(bytes, StandardCharsets.ISO_8859_1).isBlank());
  }

  private boolean isPdf(byte[] bytes) {
    String prefixo = new String(bytes, 0, Math.min(bytes.length, 8), StandardCharsets.ISO_8859_1);
    return prefixo.startsWith("%PDF");
  }
}
