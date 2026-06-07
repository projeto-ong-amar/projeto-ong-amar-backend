package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;

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
        List.of("Acao 1", "Acao 2"));

    byte[] bytes = service.gerarPdf(dto);

    assertTrue(bytes.length > 0);
    String prefixo = new String(bytes, 0, Math.min(bytes.length, 8), StandardCharsets.ISO_8859_1);
    assertTrue(prefixo.startsWith("%PDF"));
  }

  @Test
  @DisplayName("gerarPdf: deve gerar PDF em modo contingencia com campos nulos")
  void deveGerarPdfModoContingenciaComCamposNulos() {
    RelatorioAssistenteDTO dto = new RelatorioAssistenteDTO(
        null,
        null,
        null,
        List.of("Este relatorio nao foi gerado por IA devido a limite de cota"),
        null);

    byte[] bytes = service.gerarPdf(dto);

    assertTrue(bytes.length > 0);
    String prefixo = new String(bytes, 0, Math.min(bytes.length, 8), StandardCharsets.ISO_8859_1);
    assertTrue(prefixo.startsWith("%PDF"));
    assertFalse(new String(bytes, StandardCharsets.ISO_8859_1).isBlank());
  }
}
