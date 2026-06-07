package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AssistenteIaResponseValidatorService {

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

  public boolean isRespostaAderenteAosDados(String resposta, String dados) {
    if (resposta == null || resposta.isBlank()) {
      return false;
    }

    if (!contemSecoesObrigatorias(resposta)) {
      return false;
    }

    Set<String> numerosDados = extrairNumeros(dados);
    Set<String> numerosResposta = extrairNumeros(resposta);
    boolean temNumeroEmComum = numerosResposta.stream().anyMatch(numerosDados::contains);
    if (!temNumeroEmComum) {
      return false;
    }

    String respostaLower = resposta.toLowerCase();
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
      if (respostaLower.contains(termo) && !dadosLower.contains(termo)) {
        return false;
      }
    }

    return true;
  }

  private boolean contemSecoesObrigatorias(String resposta) {
    String texto = resposta.toLowerCase();
    return texto.contains("resumo executivo:")
        && texto.contains("prioridade:")
        && texto.contains("análise:")
        && texto.contains("ações recomendadas:");
  }

  private Set<String> extrairNumeros(String texto) {
    Set<String> numeros = new HashSet<>();
    Matcher matcher = Pattern.compile("\\b\\d+[\\d.,]*\\b").matcher(texto);
    while (matcher.find()) {
      numeros.add(matcher.group());
    }
    return numeros;
  }
}
