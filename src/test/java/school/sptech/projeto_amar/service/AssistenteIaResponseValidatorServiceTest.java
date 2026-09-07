package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.sptech.projeto_amar.dto.assistente.RelatorioIaResponseDTO;
import school.sptech.projeto_amar.dto.assistente.TabelaRelatorioDTO;

import java.util.ArrayList;
import java.util.List;

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
  @DisplayName("isRespostaAderenteAosDados: deve retornar falso para resposta nula ou vazia")
  void deveRetornarFalsoParaRespostaVazia() {
    assertFalse(service.isRespostaAderenteAosDados(null, "dados"));
    assertFalse(service.isRespostaAderenteAosDados(new RelatorioIaResponseDTO(null, null, null, null, null), "dados"));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve retornar falso quando prioridade invalida ou listas vazias")
  void deveRetornarFalsoQuandoCamposObrigatoriosInvalidos() {
    RelatorioIaResponseDTO prioridadeInvalida = new RelatorioIaResponseDTO(
        "resumo", "urgente", List.of("analise 10"), List.of("acao"), null);
    String dados = "Total de itens: 10";

    assertFalse(service.isRespostaAderenteAosDados(prioridadeInvalida, dados));

    RelatorioIaResponseDTO semAnalise = new RelatorioIaResponseDTO(
        "resumo", "media", List.of(), List.of("acao"), null);

    assertFalse(service.isRespostaAderenteAosDados(semAnalise, dados));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve retornar falso quando numeros nao coincidem")
  void deveRetornarFalsoSemNumeroEmComum() {
    RelatorioIaResponseDTO resposta = new RelatorioIaResponseDTO(
        "x", "media", List.of("total 999"), List.of("y"), null);
    String dados = "Total de itens: 10";

    assertFalse(service.isRespostaAderenteAosDados(resposta, dados));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve retornar falso para termo externo nao presente nos dados")
  void deveRetornarFalsoParaTermoExterno() {
    RelatorioIaResponseDTO resposta = new RelatorioIaResponseDTO(
        "x", "media", List.of("total 10 e campanha ativa"), List.of("y"), null);
    String dados = "Total de itens: 10";

    assertFalse(service.isRespostaAderenteAosDados(resposta, dados));
  }

  @Test
  @DisplayName("isRespostaAderenteAosDados: deve aceitar prioridade com acento e retornar verdadeiro quando aderente")
  void deveRetornarVerdadeiroQuandoAderente() {
    RelatorioIaResponseDTO resposta = new RelatorioIaResponseDTO(
        "x", "média", List.of("total 10 e risco moderado"), List.of("y"),
        List.of(new TabelaRelatorioDTO("t", List.of("C"), List.of(List.of("v")))));
    String dados = "Total de itens: 10\nRisco: moderado";

    assertTrue(service.isRespostaAderenteAosDados(resposta, dados));
  }

  @Test
  @DisplayName("isTabelaValida: deve validar estrutura de colunas e linhas")
  void deveValidarEstruturaDeTabela() {
    TabelaRelatorioDTO valida = new TabelaRelatorioDTO(
        "Titulo",
        List.of("Coluna 1", "Coluna 2"),
        List.of(List.of("a", "b")));

    assertTrue(service.isTabelaValida(valida));

    assertFalse(service.isTabelaValida(null));
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO("", List.of("C"), List.of(List.of("a")))));
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO(null, List.of("C"), List.of(List.of("a")))));
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO("T", List.of(), List.of(List.of("a")))));
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO("T", List.of(" "), List.of(List.of("a")))));
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO("T", List.of("C1", "C2"), List.of(List.of("a")))));
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO("T", List.of("C"), new ArrayList<>())));
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO("T", List.of("C"), List.of(new ArrayList<>()))));
  }

  @Test
  @DisplayName("isTabelaValida: deve rejeitar tabela acima dos limites de linhas e colunas")
  void deveRejeitarTabelaAcimaDosLimites() {
    List<String> seteColunas = List.of("c1", "c2", "c3", "c4", "c5", "c6", "c7");
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO("T", seteColunas, List.of(seteColunas))));

    List<List<String>> dezesseisLinhas = new java.util.ArrayList<>();
    for (int i = 0; i < 16; i++) {
      dezesseisLinhas.add(List.of("a"));
    }
    assertFalse(service.isTabelaValida(new TabelaRelatorioDTO("T", List.of("C"), dezesseisLinhas)));
  }
}
