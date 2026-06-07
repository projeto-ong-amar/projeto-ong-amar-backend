package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AssistenteRelatorioPdfMapperService {

  public RelatorioAssistenteDTO converterTextoEmRelatorioDto(String titulo, String tipoRelatorio, String texto) {
    String textoSeguro = texto == null ? "Sem conteúdo para exibição." : texto;
    boolean fallback = textoSeguro.startsWith("AVISO: Este relatório não foi gerado por IA.");
    String resumoExecutivo = extrairResumoExecutivo(textoSeguro, titulo);

    List<String> linhas = Arrays.stream(textoSeguro.split("\\R"))
        .map(String::trim)
        .filter(linha -> !linha.isBlank())
        .limit(16)
        .toList();

    List<String> analise = new ArrayList<>(linhas);
    if (analise.isEmpty()) {
      analise.add("Não foi possível obter conteúdo para este relatório.");
    }

    List<String> acoes = new ArrayList<>();
    acoes.add("Validar os dados principais deste relatório com o time responsável.");
    acoes.add("Executar as correções operacionais priorizando itens críticos.");
    if (fallback) {
      acoes.add("Tentar novamente a geração com IA quando a cota da API estiver disponível.");
    } else {
      acoes.add("Revisar este relatório periodicamente para acompanhar a evolução dos indicadores.");
    }

    return new RelatorioAssistenteDTO(
        resumoExecutivo,
        tipoRelatorio,
        calcularPrioridade(textoSeguro, fallback),
        analise,
        acoes);
  }

  private String extrairResumoExecutivo(String texto, String tituloPadrao) {
    String resumoPorCampo = extrairCampoPorPrefixo(texto, "Resumo executivo:");
    if (!resumoPorCampo.isBlank()) {
      return resumoPorCampo;
    }

    List<String> linhas = Arrays.stream(texto.split("\\R"))
        .map(String::trim)
        .filter(linha -> !linha.isBlank())
        .toList();

    for (String linha : linhas) {
      String linhaLower = linha.toLowerCase();
      if (linhaLower.startsWith("aviso:")
          || linhaLower.startsWith("relatório gerado em modo contingência")
          || linhaLower.startsWith("motivo:")
          || linhaLower.startsWith("tipo de relatório:")
          || linhaLower.startsWith("pergunta:")
          || linhaLower.startsWith("dados consolidados do banco:")
          || linhaLower.startsWith("detalhe técnico resumido:")
          || linhaLower.endsWith(":")) {
        continue;
      }

      return linha.length() > 180 ? linha.substring(0, 180) : linha;
    }

    return tituloPadrao;
  }

  private String calcularPrioridade(String texto, boolean fallback) {
    if (fallback) {
      return "media";
    }

    String prioridadePorCampo = extrairCampoPorPrefixo(texto, "Prioridade:");
    if (!prioridadePorCampo.isBlank()) {
      String prioridadeNormalizada = normalizarPrioridade(prioridadePorCampo);
      if (!prioridadeNormalizada.isBlank()) {
        return prioridadeNormalizada;
      }
    }

    String textoNormalizado = texto.toLowerCase();
    int pontosCriticos = 0;
    int pontosAltos = 0;

    if (textoNormalizado.contains("ruptura") || textoNormalizado.contains("estoque zerado")) {
      pontosCriticos += 2;
    }

    if (textoNormalizado.contains("vencido") || textoNormalizado.contains("vencidos")) {
      pontosCriticos += 1;
    }

    if (textoNormalizado.contains("crític") || textoNormalizado.contains("critic")) {
      pontosCriticos += 1;
    }

    if (textoNormalizado.contains("perda") || textoNormalizado.contains("alerta")) {
      pontosAltos += 1;
    }

    if (textoNormalizado.contains("atenção") || textoNormalizado.contains("risco")) {
      pontosAltos += 1;
    }

    if (pontosCriticos >= 2) {
      return "critica";
    }

    if (pontosAltos >= 2 || pontosCriticos == 1) {
      return "alta";
    }

    return "media";
  }

  private String extrairCampoPorPrefixo(String texto, String prefixo) {
    Pattern pattern = Pattern.compile("(?im)^" + Pattern.quote(prefixo) + "\\s*(.+)$");
    Matcher matcher = pattern.matcher(texto);
    if (matcher.find()) {
      return matcher.group(1).trim();
    }
    return "";
  }

  private String normalizarPrioridade(String prioridade) {
    String valor = prioridade.toLowerCase().trim();
    if (valor.startsWith("baixa")) {
      return "baixa";
    }
    if (valor.startsWith("média") || valor.startsWith("media")) {
      return "media";
    }
    if (valor.startsWith("alta")) {
      return "alta";
    }
    if (valor.startsWith("crítica") || valor.startsWith("critica")) {
      return "critica";
    }
    return "";
  }
}
