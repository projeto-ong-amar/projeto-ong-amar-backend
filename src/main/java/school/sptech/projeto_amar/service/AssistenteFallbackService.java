package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AssistenteFallbackService {

  public String gerarRelatorioFallback(String tipoRelatorio, String pergunta, String dados, RuntimeException ex) {
    StringBuilder fallback = new StringBuilder();
    fallback.append("AVISO: Este relatório não foi gerado por IA.\n");
    fallback.append("Relatório gerado em modo contingência (sem IA).\n\n");
    fallback.append("Motivo: limite de cota da API de IA atingido temporariamente.\n");
    fallback.append("Tipo de relatório: ").append(tipoRelatorio).append("\n");
    fallback.append("Pergunta: ").append(pergunta).append("\n\n");
    fallback.append("Dados consolidados do banco:\n");
    fallback.append(dados);
    fallback.append("\nTente novamente mais tarde para obter uma análise textual da IA.\n");
    fallback.append("Detalhe técnico resumido: ").append(ex.getClass().getSimpleName());
    return fallback.toString();
  }

  public String gerarRelatorioDeterministico(String tipoRelatorio, String dados) {
    String prioridade = calcularPrioridadeDeterministicaPorDados(dados);
    String analise = construirAnaliseDeterministica(dados);
    String acoes = construirAcoesDeterministicas(tipoRelatorio, dados);

    return "Resumo executivo: Relatório consolidado diretamente dos dados do banco para " + tipoRelatorio + ".\n"
        + "Prioridade: " + prioridade + "\n"
        + "Análise: " + analise + "\n"
        + "Ações recomendadas: " + acoes;
  }

  private String construirAnaliseDeterministica(String dados) {
    List<String> linhas = Arrays.stream(dados.split("\\R"))
        .map(String::trim)
        .filter(linha -> !linha.isBlank())
        .filter(linha -> !linha.endsWith(":"))
        .map(linha -> linha.startsWith("- ") ? linha.substring(2).trim() : linha)
        .limit(4)
        .toList();

    if (linhas.isEmpty()) {
      return "Não há dados suficientes para análise detalhada.";
    }

    return String.join(". ", linhas) + ".";
  }

  private String construirAcoesDeterministicas(String tipoRelatorio, String dados) {
    StringBuilder acoes = new StringBuilder();
    acoes.append("Priorizar revisão dos itens destacados neste ").append(tipoRelatorio)
        .append(" e validar os registros operacionais.");

    if (dados.contains("Lotes vencidos com saldo:") || dados.contains("vencimento em até 7 dias")) {
      acoes.append(" Programar conferência de validade e ajuste de giro dos produtos com maior risco.");
    }

    if (dados.contains("Total de movimentações de saída registradas")
        || dados.contains("Total de movimentações de entrada registradas")) {
      acoes.append(" Comparar volumes de entrada e saída para ajustar reposição e evitar ruptura.");
    }

    acoes.append(" Regerar o relatório com IA após estabilização da resposta para obter texto analítico complementar.");
    return acoes.toString();
  }

  private String calcularPrioridadeDeterministicaPorDados(String dados) {
    Integer lotesVencidos = extrairNumeroPorPrefixo(dados, "- Lotes vencidos com saldo:");
    if (lotesVencidos != null && lotesVencidos > 0) {
      return "critica";
    }

    Integer lotesProximos = extrairNumeroPorPrefixo(dados, "- Lotes com vencimento em até 7 dias:");
    if (lotesProximos != null && lotesProximos > 0) {
      return "alta";
    }

    if (dados.contains("Nenhuma entrada cadastrada")
        || dados.contains("Nenhuma saída cadastrada")
        || dados.contains("Nenhum produto cadastrado")) {
      return "baixa";
    }

    return "media";
  }

  private Integer extrairNumeroPorPrefixo(String texto, String prefixo) {
    Pattern pattern = Pattern.compile("(?im)^" + Pattern.quote(prefixo) + "\\s*(\\d+)");
    Matcher matcher = pattern.matcher(texto);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }
    return null;
  }
}
