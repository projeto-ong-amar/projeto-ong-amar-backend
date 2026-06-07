package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssistenteIaResponseValidatorServiceTest {

  private final AssistenteIaResponseValidatorService service = new AssistenteIaResponseValidatorService();

  @Test
  @DisplayName("normalizarSaidaIa: retorna vazio para entrada nula ou em branco")
  void deveNormalizarVazio() {
    assertEquals("", service.normalizarSaidaIa(null));
    assertEquals("", service.normalizarSaidaIa("   "));
  }

  @Test
  @DisplayName("normalizarSaidaIa: remove markdown e simbolos")
  void deveRemoverMarkdownESimbolos() {
    String entrada = "**Resumo**\n- Item 1\n#Titulo\n`codigo`";

    String resultado = service.normalizarSaidaIa(entrada);

    assertFalse(resultado.contains("**"));
    assertFalse(resultado.contains("#"));
    assertFalse(resultado.contains("`"));
    assertFalse(resultado.contains("- "));
    assertTrue(resultado.contains("Resumo"));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve retornar falso para resposta vazia")
  void deveRetornarFalsoParaRespostaVazia() {
    assertFalse(service.isRespostaAderenteAosDados("", "dados"));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve retornar falso quando secoes obrigatorias faltam")
  void deveRetornarFalsoSemSecoesObrigatorias() {
    String resposta = "Texto sem estrutura obrigatoria";
    String dados = "Total: 10";

    assertFalse(service.isRespostaAderenteAosDados(resposta, dados));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve retornar falso quando numeros nao coincidem")
  void deveRetornarFalsoSemNumeroEmComum() {
    String resposta = "Resumo executivo: x\nPrioridade: media\nAnálise: total 999\nAções recomendadas: y";
    String dados = "Total de itens: 10";

    assertFalse(service.isRespostaAderenteAosDados(resposta, dados));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve retornar falso para termo externo nao presente nos dados")
  void deveRetornarFalsoParaTermoExterno() {
    String resposta = "Resumo executivo: x\nPrioridade: media\nAnálise: total 10 e campanha ativa\nAções recomendadas: y";
    String dados = "Total de itens: 10";

    assertFalse(service.isRespostaAderenteAosDados(resposta, dados));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve retornar verdadeiro quando resposta e aderente")
  void deveRetornarVerdadeiroQuandoAderente() {
    String resposta = "Resumo executivo: x\nPrioridade: media\nAnálise: total 10 e risco moderado\nAções recomendadas: y";
    String dados = "Total de itens: 10\nRisco: moderado";

    assertTrue(service.isRespostaAderenteAosDados(resposta, dados));
  }
}
