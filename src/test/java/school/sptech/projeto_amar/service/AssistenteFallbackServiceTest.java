package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AssistenteFallbackServiceTest {

  private final AssistenteFallbackService service = new AssistenteFallbackService();

  @Test
  @DisplayName("gerarRelatorioFallback: deve incluir informacoes de contingencia")
  void deveGerarRelatorioFallbackCompleto() {
    RuntimeException ex = new RuntimeException("quota");

    String resultado = service.gerarRelatorioFallback(
        "desempenho geral",
        "Qual e a prioridade?",
        "dados consolidados",
        ex);

    assertTrue(resultado.contains("AVISO: Este relatório não foi gerado por IA."));
    assertTrue(resultado.contains("desempenho geral"));
    assertTrue(resultado.contains("Qual e a prioridade?"));
    assertTrue(resultado.contains("dados consolidados"));
    assertTrue(resultado.contains("RuntimeException"));
  }

  @Test
  @DisplayName("gerarRelatorioDeterministico: prioridade critica quando ha lotes vencidos")
  void deveDefinirPrioridadeCriticaComLotesVencidos() {
    String dados = "- Lotes vencidos com saldo: 2\n"
        + "- Lotes com vencimento em até 7 dias: 0\n"
        + "Total de movimentacoes de entrada registradas: 5";

    String resultado = service.gerarRelatorioDeterministico("desempenho", dados);

    assertTrue(resultado.contains("Prioridade: critica"));
    assertTrue(resultado.contains("Programar"));
  }

  @Test
  @DisplayName("gerarRelatorioDeterministico: prioridade alta quando ha lotes proximos")
  void deveDefinirPrioridadeAltaComLotesProximos() {
    String dados = "- Lotes vencidos com saldo: 0\n"
        + "- Lotes com vencimento em até 7 dias: 3\n";

    String resultado = service.gerarRelatorioDeterministico("historico", dados);

    assertTrue(resultado.contains("Prioridade: alta"));
  }

  @Test
  @DisplayName("gerarRelatorioDeterministico: prioridade baixa quando nao ha dados de operacao")
  void deveDefinirPrioridadeBaixaSemDadosOperacionais() {
    String dados = "- Nenhum produto cadastrado ate o momento.";

    String resultado = service.gerarRelatorioDeterministico("historico", dados);

    assertTrue(resultado.contains("Prioridade: baixa"));
  }

  @Test
  @DisplayName("gerarRelatorioDeterministico: prioridade media e analise padrao quando nao ha linhas validas")
  void deveDefinirPrioridadeMediaEAnalisePadrao() {
    String dados = "Resumo:\n\n";

    String resultado = service.gerarRelatorioDeterministico("historico", dados);

    assertTrue(resultado.contains("Prioridade: media"));
    assertTrue(resultado.contains("Não há dados suficientes para análise detalhada."));
  }
}
