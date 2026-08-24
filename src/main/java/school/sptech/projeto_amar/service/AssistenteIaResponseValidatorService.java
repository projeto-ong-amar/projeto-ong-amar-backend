package school.sptech.projeto_amar.service;

import school.sptech.projeto_amar.dto.assistente.RelatorioIaResponseDTO;
import school.sptech.projeto_amar.dto.assistente.TabelaRelatorioDTO;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AssistenteIaResponseValidatorService {

  private static final int MAX_COLUNAS_TABELA = 6;
  private static final int MAX_LINHAS_TABELA = 15;

  public String normalizarSaidaIa(String texto) {
    if (texto == null || texto.isBlank()) {
      return "";
    }

    String normalizado = texto
        .replace("**", "")
        .replace("`", "")
        .replace("#", "");

    normalizado = normalizado.replaceAll("(?m)^\\s*[-*]+\\s*", "");
    normalizado = normalizado.replaceAll("\\r", "");
    return normalizado.trim();
  }

  public boolean isRespostaAderenteAosDados(RelatorioIaResponseDTO resposta, String dados) {
    if (resposta == null || dados == null || dados.isBlank()) {
      return false;
    }

    if (resposta.resumoExecutivo() == null || resposta.resumoExecutivo().isBlank()) {
      return false;
    }

    if (!isPrioridadeValida(resposta.prioridade())) {
      return false;
    }

    if (!possuiListaComConteudo(resposta.analise()) || !possuiListaComConteudo(resposta.acoesRecomendadas())) {
      return false;
    }

    String conteudo = montarTextoConsolidado(resposta);

    Set<String> numerosDados = extrairNumeros(dados);
    Set<String> numerosResposta = extrairNumeros(conteudo);
    boolean temNumeroEmComum = numerosResposta.stream().anyMatch(numerosDados::contains);
    if (!temNumeroEmComum) {
      return false;
    }

    String conteudoLower = conteudo.toLowerCase();
    String dadosLower = dados.toLowerCase();
    List<String> termosExternos = List.of(
        "doador",
        "doadores",
        "campanha",
        "campanhas",
        "fifo",
        "sustentabilidade",
        "armazenamento otimizado");

    for (String termo : termosExternos) {
      if (conteudoLower.contains(termo) && !dadosLower.contains(termo)) {
        return false;
      }
    }

    return true;
  }

  public boolean isPrioridadeValida(String prioridade) {
    if (prioridade == null) {
      return false;
    }

    String valor = removerAcentos(prioridade).toLowerCase().trim();
    return valor.equals("baixa")
        || valor.equals("media")
        || valor.equals("alta")
        || valor.equals("critica");
  }

  public boolean isTabelaValida(TabelaRelatorioDTO tabela) {
    if (tabela == null
        || tabela.titulo() == null
        || tabela.titulo().isBlank()
        || tabela.colunas() == null
        || tabela.colunas().isEmpty()
        || tabela.linhas() == null
        || tabela.linhas().isEmpty()) {
      return false;
    }

    if (tabela.colunas().size() > MAX_COLUNAS_TABELA) {
      return false;
    }

    for (String coluna : tabela.colunas()) {
      if (coluna == null || coluna.isBlank()) {
        return false;
      }
    }

    if (tabela.linhas().size() > MAX_LINHAS_TABELA) {
      return false;
    }

    for (List<String> linha : tabela.linhas()) {
      if (linha == null || linha.size() != tabela.colunas().size()) {
        return false;
      }
    }

    return true;
  }

  private boolean possuiListaComConteudo(List<String> lista) {
    return lista != null && !lista.isEmpty();
  }

  private String montarTextoConsolidado(RelatorioIaResponseDTO resposta) {
    StringBuilder construtor = new StringBuilder();
    construtor.append(resposta.resumoExecutivo()).append(" ");
    construtor.append(resposta.prioridade()).append(" ");

    if (resposta.analise() != null) {
      construtor.append(String.join(" ", resposta.analise())).append(" ");
    }
    if (resposta.acoesRecomendadas() != null) {
      construtor.append(String.join(" ", resposta.acoesRecomendadas()));
    }

    return construtor.toString();
  }

  private Set<String> extrairNumeros(String texto) {
    Set<String> numeros = new HashSet<>();
    Matcher matcher = Pattern.compile("\\b\\d+[\\d.,]*\\b").matcher(texto);
    while (matcher.find()) {
      numeros.add(matcher.group());
    }
    return numeros;
  }

  private String removerAcentos(String texto) {
    if (texto == null) {
      return "";
    }
    return Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");
  }
}
