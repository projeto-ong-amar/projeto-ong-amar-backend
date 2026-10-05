package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import school.sptech.projeto_amar.domain.enums.Categoria;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;
import school.sptech.projeto_amar.dto.assistente.RelatorioIaResponseDTO;
import school.sptech.projeto_amar.dto.assistente.TabelaRelatorioDTO;
import school.sptech.projeto_amar.exception.assistenteException.ErroAoChamarIaException;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistenteServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoLoteRepository produtoLoteRepository;

    @Mock
    private AssistenteFallbackService assistenteFallbackService;

    @Mock
    private AssistenteIaResponseValidatorService assistenteIaResponseValidatorService;

    private AssistenteService service;

    private Movimentacao movimentacaoEntrada;
    private Movimentacao movimentacaoSaida;
    private Produto produto;

    @BeforeEach
    void setUp() {
        service = new AssistenteService(
                chatModel,
                movimentacaoRepository,
                produtoRepository,
                produtoLoteRepository,
                assistenteFallbackService,
                assistenteIaResponseValidatorService,
                new AssistenteIaJsonParserService());

        Categoria categoria = Categoria.ALIMENTOS;

        produto = new Produto();
        produto.setId(1);
        produto.setNome("Arroz");
        produto.setCategoria(categoria);
        produto.setUnidadeMedida("kg");
        produto.setStatus("ATIVO");

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("Maria");

        movimentacaoEntrada = new Movimentacao();
        movimentacaoEntrada.setId(1);
        movimentacaoEntrada.setTipo(TipoMovimentacao.ENTRADA);
        movimentacaoEntrada.setQuantidade(10);
        movimentacaoEntrada.setData(LocalDate.now());
        movimentacaoEntrada.setMotivo("Doacao");
        movimentacaoEntrada.setProduto(produto);
        movimentacaoEntrada.setUsuario(usuario);

        movimentacaoSaida = new Movimentacao();
        movimentacaoSaida.setId(2);
        movimentacaoSaida.setTipo(TipoMovimentacao.SAIDA);
        movimentacaoSaida.setQuantidade(4);
        movimentacaoSaida.setData(LocalDate.now());
        movimentacaoSaida.setMotivo("Distribuicao");
        movimentacaoSaida.setProduto(produto);
        movimentacaoSaida.setUsuario(usuario);
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoEntradas: deve formatar resposta estruturada da IA quando aderente")
    void deveRetornarRespostaIaQuandoAderente() {
        mockHistoricoEntradasBasico();

        String respostaIa = """
                {
                  "resumo_executivo": "Estoque equilibrado.",
                  "prioridade": "media",
                  "analise": ["Foram registradas 1 entradas e 10 itens."],
                  "acoes_recomendadas": ["Manter acompanhamento semanal."],
                  "tabelas": [
                    {"titulo": "Ultimas entradas", "colunas": ["Data", "Produto"], "linhas": [["01/08/2026", "Arroz"]]}
                  ]
                }
                """;
        mockIaResposta(respostaIa);

        when(assistenteIaResponseValidatorService.isRespostaAderenteAosDados(
                any(RelatorioIaResponseDTO.class), anyString())).thenReturn(true);
        when(assistenteIaResponseValidatorService.normalizarSaidaIa(anyString()))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
        when(assistenteIaResponseValidatorService.isTabelaValida(any(TabelaRelatorioDTO.class))).thenReturn(true);

        String resultado = service.gerarRelatorioHistoricoEntradas("Me mostre o resumo");

        assertTrue(resultado.contains("Resumo executivo: Estoque equilibrado."));
        assertTrue(resultado.contains("Prioridade: media"));
        assertTrue(resultado.contains("Análise: Foram registradas 1 entradas e 10 itens."));
        assertTrue(resultado.contains("Ações recomendadas: Manter acompanhamento semanal."));
        verify(assistenteFallbackService, never()).gerarRelatorioDeterministico(anyString(), anyString());
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoSaidas: deve usar fallback deterministico quando resposta nao aderente")
    void deveUsarFallbackDeterministicoQuandoRespostaNaoAderente() {
        mockHistoricoSaidasBasico();

        mockIaResposta("resposta ruim sem json");
        when(assistenteFallbackService.gerarRelatorioDeterministico(eq("histórico de saídas"), anyString()))
                .thenReturn("fallback-deterministico");

        String resultado = service.gerarRelatorioHistoricoSaidas("Como estao as saidas?");

        assertEquals("fallback-deterministico", resultado);
        verify(assistenteFallbackService).gerarRelatorioDeterministico(eq("histórico de saídas"), anyString());
        verify(assistenteIaResponseValidatorService, never()).isRespostaAderenteAosDados(
                any(RelatorioIaResponseDTO.class), anyString());
    }

    @Test
    @DisplayName("gerarRelatorioDesempenhoGeral: deve usar fallback de cota quando IA retorna 429")
    void deveUsarFallbackQuandoQuotaExcedida() {
        mockDesempenhoGeralBasico();

        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("429 quota exceeded"));
        when(assistenteFallbackService.gerarRelatorioFallback(
                eq("desempenho geral do estoque"), anyString(), anyString(), any(RuntimeException.class)))
                .thenReturn("fallback-cota");

        String resultado = service.gerarRelatorioDesempenhoGeral("Analise o panorama geral");

        assertEquals("fallback-cota", resultado);
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoProdutosCadastrados: deve lançar excecao para erro nao relacionado a quota")
    void deveLancarErroAoChamarIaQuandoNaoForQuota() {
        mockProdutosCadastradosBasico();

        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("timeout de rede"));

        assertThrows(
                ErroAoChamarIaException.class,
                () -> service.gerarRelatorioHistoricoProdutosCadastrados("Liste os produtos"));
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoEntradasParaPdf: deve retornar tabelas da IA e campos mapeados quando aderente")
    void deveMapearEntradasParaPdfComTabelasDaIa() {
        mockHistoricoEntradasBasico();

        String respostaIa = """
                ```json
                {
                  "resumo_executivo": "Estoque equilibrado.",
                  "prioridade": "alta",
                  "analise": ["Foram registradas 1 entradas e 10 itens."],
                  "acoes_recomendadas": ["Manter acompanhamento semanal."],
                  "tabelas": [
                    {"titulo": "Totais de entradas", "colunas": ["Indicador", "Valor"], "linhas": [["Itens que entraram", "10"]]}
                  ]
                }
                ```
                """;
        mockIaResposta(respostaIa);

        when(assistenteIaResponseValidatorService.isRespostaAderenteAosDados(
                any(RelatorioIaResponseDTO.class), anyString())).thenReturn(true);
        when(assistenteIaResponseValidatorService.normalizarSaidaIa(anyString()))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
        when(assistenteIaResponseValidatorService.isTabelaValida(any(TabelaRelatorioDTO.class))).thenReturn(true);

        RelatorioAssistenteDTO resultado = service.gerarRelatorioHistoricoEntradasParaPdf("Pergunta");

        assertEquals("Estoque equilibrado.", resultado.titulo());
        assertEquals("historico-entradas", resultado.tipo());
        assertEquals("alta", resultado.prioridade());
        assertEquals(List.of("Foram registradas 1 entradas e 10 itens."), resultado.response());
        assertEquals(List.of("Manter acompanhamento semanal."), resultado.acoes_recomendadas());

        assertNotNull(resultado.tabelas());
        assertEquals(1, resultado.tabelas().size());
        assertEquals("Totais de entradas", resultado.tabelas().get(0).titulo());
        assertEquals(List.of("Indicador", "Valor"), resultado.tabelas().get(0).colunas());
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoEntradasParaPdf: deve usar tabelas determinísticas quando IA não retorna tabelas")
    void deveUsarTabelasDeterministicasQuandoIaNaoDevolveTabelas() {
        mockHistoricoEntradasBasico();

        String respostaIa = """
                {
                  "resumo_executivo": "Estoque equilibrado.",
                  "prioridade": "baixa",
                  "analise": ["Foram registradas 1 entradas."],
                  "acoes_recomendadas": ["Monitorar giro."]
                }
                """;
        mockIaResposta(respostaIa);

        when(assistenteIaResponseValidatorService.isRespostaAderenteAosDados(
                any(RelatorioIaResponseDTO.class), anyString())).thenReturn(true);
        when(assistenteIaResponseValidatorService.normalizarSaidaIa(anyString()))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        RelatorioAssistenteDTO resultado = service.gerarRelatorioHistoricoEntradasParaPdf("Pergunta");

        assertNotNull(resultado.tabelas());
        assertFalse(resultado.tabelas().isEmpty());
        assertEquals("Totais de entradas", resultado.tabelas().get(0).titulo());
        assertEquals("Últimas entradas registradas", resultado.tabelas().get(1).titulo());
        assertEquals(1, resultado.tabelas().get(1).linhas().size());
        assertEquals("Arroz", resultado.tabelas().get(1).linhas().get(0).get(1));
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoSaidasParaPdf: deve montar DTO de contingencia com tabelas determinísticas na cota excedida")
    void deveMapearSaidasParaPdfEmContingencia() {
        mockHistoricoSaidasBasico();

        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("429 quota exceeded"));
        when(assistenteFallbackService.gerarRelatorioContingenciaEstruturado(
                eq("histórico de saídas"), anyString(), anyString()))
                .thenReturn(new RelatorioIaResponseDTO(
                        "Relatório gerado em modo contingência (sem IA) para histórico de saídas.",
                        "media",
                        List.of("AVISO: Este relatório não foi gerado por IA."),
                        List.of("Tentar novamente mais tarde."),
                        null));

        RelatorioAssistenteDTO resultado = service.gerarRelatorioHistoricoSaidasParaPdf("Como estao as saidas?");

        assertEquals("historico-saidas", resultado.tipo());
        assertEquals("media", resultado.prioridade());
        assertTrue(resultado.response().get(0).toLowerCase().contains("não foi gerado por ia"));

        assertNotNull(resultado.tabelas());
        assertFalse(resultado.tabelas().isEmpty());
        assertEquals("Totais de saídas", resultado.tabelas().get(0).titulo());
    }

    @Test
    @DisplayName("gerarRelatorioDesempenhoGeralParaPdf: deve incluir tabelas de indicadores e top produtos em contingencia")
    void deveMapearDesempenhoParaPdfEmContingencia() {
        mockDesempenhoGeralBasico();

        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("429 quota exceeded"));
        when(assistenteFallbackService.gerarRelatorioContingenciaEstruturado(
                eq("desempenho geral do estoque"), anyString(), anyString()))
                .thenReturn(new RelatorioIaResponseDTO(
                        "Relatório gerado em modo contingência (sem IA).",
                        "media",
                        List.of("AVISO: Este relatório não foi gerado por IA."),
                        List.of("Tentar novamente mais tarde."),
                        null));

        RelatorioAssistenteDTO resultado = service.gerarRelatorioDesempenhoGeralParaPdf("Analise o panorama geral");

        assertEquals("desempenho-geral", resultado.tipo());

        assertNotNull(resultado.tabelas());
        assertEquals(3, resultado.tabelas().size());
        assertEquals("Indicadores gerais do estoque", resultado.tabelas().get(0).titulo());
        assertEquals("Top produtos por entradas", resultado.tabelas().get(1).titulo());
        assertEquals(List.of("Arroz", "30"), resultado.tabelas().get(1).linhas().get(0));
        assertEquals("Top produtos por saídas", resultado.tabelas().get(2).titulo());
    }

    @Test
    @DisplayName("gerarRelatorioProdutosCadastradosParaPdf: deve incluir tabela de produtos cadastrados em contingencia")
    void deveMapearProdutosParaPdfEmContingencia() {
        mockProdutosCadastradosBasico();

        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("429 quota exceeded"));
        when(assistenteFallbackService.gerarRelatorioContingenciaEstruturado(
                eq("histórico de produtos cadastrados"), anyString(), anyString()))
                .thenReturn(new RelatorioIaResponseDTO(
                        "Relatório gerado em modo contingência (sem IA).",
                        "media",
                        List.of("AVISO: Este relatório não foi gerado por IA."),
                        List.of("Tentar novamente mais tarde."),
                        null));

        RelatorioAssistenteDTO resultado = service.gerarRelatorioProdutosCadastradosParaPdf("Liste os produtos");

        assertEquals("produtos-cadastrados", resultado.tipo());

        TabelaRelatorioDTO tabelaProdutos = resultado.tabelas().stream()
                .filter(t -> t.titulo().equals("Últimos produtos cadastrados"))
                .findFirst()
                .orElseThrow();

        assertEquals(List.of("ID", "Nome", "Categoria", "Unidade", "Status"), tabelaProdutos.colunas());
        assertEquals(List.of("1", "Arroz", "Alimentos", "kg", "ATIVO"), tabelaProdutos.linhas().get(0));
    }

    private void mockHistoricoEntradasBasico() {
        when(movimentacaoRepository.findTop20ByTipoOrderByDataDescIdDesc(TipoMovimentacao.ENTRADA))
                .thenReturn(List.of(movimentacaoEntrada));
        when(movimentacaoRepository.countByTipo(TipoMovimentacao.ENTRADA)).thenReturn(1L);
        when(movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.ENTRADA)).thenReturn(10);
    }

    private void mockHistoricoSaidasBasico() {
        when(movimentacaoRepository.findTop20ByTipoOrderByDataDescIdDesc(TipoMovimentacao.SAIDA))
                .thenReturn(List.of(movimentacaoSaida));
        when(movimentacaoRepository.countByTipo(TipoMovimentacao.SAIDA)).thenReturn(1L);
        when(movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.SAIDA)).thenReturn(4);
    }

    private void mockDesempenhoGeralBasico() {
        when(produtoRepository.count()).thenReturn(1L);
        when(produtoRepository.countByStatus("ATIVO")).thenReturn(1L);
        when(produtoRepository.countByStatus("INATIVO")).thenReturn(0L);
        when(produtoLoteRepository.sumQuantidadeTotal()).thenReturn(25);

        when(movimentacaoRepository.countByTipo(TipoMovimentacao.ENTRADA)).thenReturn(3L);
        when(movimentacaoRepository.countByTipo(TipoMovimentacao.SAIDA)).thenReturn(2L);
        when(movimentacaoRepository.countByTipo(TipoMovimentacao.PERDA)).thenReturn(1L);

        when(movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.ENTRADA)).thenReturn(30);
        when(movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.SAIDA)).thenReturn(20);
        when(movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.PERDA)).thenReturn(2);

        when(produtoLoteRepository.countLotesVencidos(any())).thenReturn(0L);
        when(produtoLoteRepository.countLotesProximosDoVencimento(any(), any())).thenReturn(1L);

        when(movimentacaoRepository.findTopProdutosPorTipo(eq(TipoMovimentacao.ENTRADA), any()))
                .thenReturn(List.<Object[]>of(new Object[] { "Arroz", 30 }));
        when(movimentacaoRepository.findTopProdutosPorTipo(eq(TipoMovimentacao.SAIDA), any()))
                .thenReturn(List.<Object[]>of(new Object[] { "Arroz", 20 }));
    }

    private void mockProdutosCadastradosBasico() {
        when(produtoRepository.findTop30ByOrderByIdDesc()).thenReturn(List.of(produto));
        when(produtoRepository.count()).thenReturn(1L);
        when(produtoRepository.countByStatus("ATIVO")).thenReturn(1L);
        when(produtoRepository.countByStatus("INATIVO")).thenReturn(0L);
    }

    private void mockIaResposta(String texto) {
        ChatResponse chatResponse = org.mockito.Mockito.mock(ChatResponse.class);
        Generation generation = org.mockito.Mockito.mock(Generation.class);
        AssistantMessage assistantMessage = org.mockito.Mockito.mock(AssistantMessage.class);

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn(texto);
    }
}
