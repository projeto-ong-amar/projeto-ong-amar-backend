package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssistenteRelatorioPdfMapperServiceTest {

  private final AssistenteRelatorioPdfMapperService service = new AssistenteRelatorioPdfMapperService();

  @Test
  @DisplayName("converterTextoEmRelatorioDto: usa texto padrao quando conteudo e nulo")
  void deveConverterComTextoNulo() {
    RelatorioAssistenteDTO dto = service.converterTextoEmRelatorioDto("Titulo padrao", "historico", null);

    assertEquals("Sem conteúdo para exibição.", dto.titulo());
    assertEquals("historico", dto.tipo());
    assertEquals("media", dto.prioridade());
    assertFalse(dto.response().isEmpty());
    assertFalse(dto.acoes_recomendadas().isEmpty());
  }

  @Test
  @DisplayName("converterTextoEmRelatorioDto: fallback deve ter prioridade media e acao de tentar novamente")
  void deveConverterFallbackComPrioridadeMedia() {
    String texto = "AVISO: Este relatório não foi gerado por IA.\n"
        + "Relatório gerado em modo contingência.\n"
        + "Resumo executivo: Operacao em contingencia.";

    RelatorioAssistenteDTO dto = service.converterTextoEmRelatorioDto("Titulo", "desempenho", texto);

    assertEquals("media", dto.prioridade());
    assertTrue(dto.acoes_recomendadas().stream().anyMatch(a -> a.toLowerCase().contains("ia")));
  }

  @Test
  @DisplayName("converterTextoEmRelatorioDto: deve usar resumo e prioridade do texto quando presentes")
  void deveExtrairResumoEPrioridadeDoTexto() {
    String texto = "Resumo executivo: Cenario estavel.\n"
        + "Prioridade: critica\n"
        + "Analise: Existe risco de ruptura.";

    RelatorioAssistenteDTO dto = service.converterTextoEmRelatorioDto("Titulo", "desempenho", texto);

    assertEquals("Cenario estavel.", dto.titulo());
    assertEquals("critica", dto.prioridade());
  }

  @Test
  @DisplayName("converterTextoEmRelatorioDto: deve classificar prioridade alta por heuristica")
  void deveClassificarPrioridadeAltaPorHeuristica() {
    String texto = "Ha risco de perda e alerta em multiplos itens.";

    RelatorioAssistenteDTO dto = service.converterTextoEmRelatorioDto("Titulo", "desempenho", texto);

    assertEquals("alta", dto.prioridade());
  }

  @Test
  @DisplayName("converterTextoEmRelatorioDto: deve classificar prioridade critica por heuristica")
  void deveClassificarPrioridadeCriticaPorHeuristica() {
    String texto = "Ha ruptura de estoque e produtos vencidos no deposito.";

    RelatorioAssistenteDTO dto = service.converterTextoEmRelatorioDto("Titulo", "desempenho", texto);

    assertEquals("critica", dto.prioridade());
  }
}
