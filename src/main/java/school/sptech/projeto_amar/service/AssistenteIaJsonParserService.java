package school.sptech.projeto_amar.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.dto.assistente.RelatorioIaResponseDTO;

@Service
public class AssistenteIaJsonParserService {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public RelatorioIaResponseDTO parse(String saidaIa) {
    if (saidaIa == null || saidaIa.isBlank()) {
      return null;
    }

    try {
      String json = extrairJson(saidaIa);
      RelatorioIaResponseDTO resposta = objectMapper.readValue(json, RelatorioIaResponseDTO.class);
      return resposta == null ? null : resposta;
    } catch (Exception ignored) {
      return null;
    }
  }

  private String extrairJson(String texto) {
    int inicio = texto.indexOf('{');
    int fim = texto.lastIndexOf('}');

    if (inicio < 0 || fim <= inicio) {
      throw new IllegalArgumentException("Resposta da IA não contém um objeto JSON.");
    }

    return texto.substring(inicio, fim + 1);
  }
}
