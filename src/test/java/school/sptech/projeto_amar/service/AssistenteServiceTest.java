package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;
import school.sptech.projeto_amar.exception.assistenteException.ErroAoChamarIaException;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private AssistenteRelatorioPdfMapperService assistenteRelatorioPdfMapperService;

    @InjectMocks
    private AssistenteService service;

    private Movimentacao movimentacaoEntrada;
    private Movimentacao movimentacaoSaida;
    private Produto produto;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria();
        categoria.setId(1);
        categoria.setNome("Alimentos");

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
    @DisplayName("gerarRelatorioHistoricoEntradas: deve retornar resposta da IA quando aderente")
    void deveRetornarRespostaIaQuandoAderente() {
        when(movimentacaoRepository.findTop20ByTipoOrderByDataDescIdDesc(TipoMovimentacao.ENTRADA))
                .thenReturn(List.of(movimentacaoEntrada));
        when(movimentacaoRepository.countByTipo(TipoMovimentacao.ENTRADA)).thenReturn(1L);
        when(movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.ENTRADA)).thenReturn(10);

        String respostaIa = "Resumo executivo: Estoque equilibrado.\n"
                + "Prioridade: media\n"
                + "Análise: Foram registradas 1 entradas e 10 itens.\n"
                + "Ações recomendadas: Manter acompanhamento semanal.";

        mockIaResposta(respostaIa);
        when(assistenteIaResponseValidatorService.normalizarSaidaIa(respostaIa)).thenReturn(respostaIa);
        when(assistenteIaResponseValidatorService.isRespostaAderenteAosDados(eq(respostaIa), anyString()))
                .thenReturn(true);

        String resultado = service.gerarRelatorioHistoricoEntradas("Me mostre o resumo");

        assertEquals(respostaIa, resultado);
        verify(assistenteFallbackService, never()).gerarRelatorioDeterministico(anyString(), anyString());
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoSaidas: deve usar fallback deterministico quando resposta nao aderente")
    void deveUsarFallbackDeterministicoQuandoRespostaNaoAderente() {
        when(movimentacaoRepository.findTop20ByTipoOrderByDataDescIdDesc(TipoMovimentacao.SAIDA))
                .thenReturn(List.of(movimentacaoSaida));
        when(movimentacaoRepository.countByTipo(TipoMovimentacao.SAIDA)).thenReturn(1L);
        when(movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.SAIDA)).thenReturn(4);

        mockIaResposta("resposta ruim");
        when(assistenteIaResponseValidatorService.normalizarSaidaIa("resposta ruim")).thenReturn("resposta ruim");
        when(assistenteIaResponseValidatorService.isRespostaAderenteAosDados(eq("resposta ruim"), anyString()))
                .thenReturn(false);
        when(assistenteFallbackService.gerarRelatorioDeterministico(eq("histórico de saídas"), anyString()))
                .thenReturn("fallback-deterministico");

        String resultado = service.gerarRelatorioHistoricoSaidas("Como estao as saidas?");

        assertEquals("fallback-deterministico", resultado);
        verify(assistenteFallbackService).gerarRelatorioDeterministico(eq("histórico de saídas"), anyString());
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
        when(produtoRepository.findTop30ByOrderByIdDesc()).thenReturn(List.of(produto));
        when(produtoRepository.count()).thenReturn(1L);
        when(produtoRepository.countByStatus("ATIVO")).thenReturn(1L);
        when(produtoRepository.countByStatus("INATIVO")).thenReturn(0L);
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("timeout de rede"));

        assertThrows(
                ErroAoChamarIaException.class,
                () -> service.gerarRelatorioHistoricoProdutosCadastrados("Liste os produtos"));
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoEntradasParaPdf: deve mapear texto para DTO com titulo e tipo corretos")
    void deveMapearEntradasParaPdf() {
        mockHistoricoEntradasBasico();
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("429 quota exceeded"));
        when(assistenteFallbackService.gerarRelatorioFallback(anyString(), anyString(), anyString(),
                any(RuntimeException.class)))
                .thenReturn("texto-entradas");

        RelatorioAssistenteDTO dto = new RelatorioAssistenteDTO(
                "Resumo",
                "historico-entradas",
                "media",
                List.of("linha"),
                List.of("acao"));

        when(assistenteRelatorioPdfMapperService.converterTextoEmRelatorioDto(
                "Histórico de Entradas",
                "historico-entradas",
                "texto-entradas")).thenReturn(dto);

        RelatorioAssistenteDTO resultado = service.gerarRelatorioHistoricoEntradasParaPdf("Pergunta");

        assertSame(dto, resultado);
    }

    @Test
    @DisplayName("gerarRelatorioHistoricoSaidasParaPdf: deve mapear texto para DTO com titulo e tipo corretos")
    void deveMapearSaidasParaPdf() {
        mockHistoricoSaidasBasico();
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("429 quota exceeded"));
        when(assistenteFallbackService.gerarRelatorioFallback(anyString(), anyString(), anyString(),
                any(RuntimeException.class)))
                .thenReturn("texto-saidas");

        RelatorioAssistenteDTO dto = new RelatorioAssistenteDTO(
                "Resumo",
                "historico-saidas",
                "media",
                List.of("linha"),
                List.of("acao"));

        when(assistenteRelatorioPdfMapperService.converterTextoEmRelatorioDto(
                "Histórico de Saídas",
                "historico-saidas",
                "texto-saidas")).thenReturn(dto);

        RelatorioAssistenteDTO resultado = service.gerarRelatorioHistoricoSaidasParaPdf("Pergunta");

        assertSame(dto, resultado);
    }

    @Test
    @DisplayName("gerarRelatorioDesempenhoGeralParaPdf: deve mapear texto para DTO com titulo e tipo corretos")
    void deveMapearDesempenhoParaPdf() {
        mockDesempenhoGeralBasico();
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("429 quota exceeded"));
        when(assistenteFallbackService.gerarRelatorioFallback(anyString(), anyString(), anyString(),
                any(RuntimeException.class)))
                .thenReturn("texto-desempenho");

        RelatorioAssistenteDTO dto = new RelatorioAssistenteDTO(
                "Resumo",
                "desempenho-geral",
                "media",
                List.of("linha"),
                List.of("acao"));

        when(assistenteRelatorioPdfMapperService.converterTextoEmRelatorioDto(
                "Desempenho Geral do Estoque",
                "desempenho-geral",
                "texto-desempenho")).thenReturn(dto);

        RelatorioAssistenteDTO resultado = service.gerarRelatorioDesempenhoGeralParaPdf("Pergunta");

        assertSame(dto, resultado);
    }

    @Test
    @DisplayName("gerarRelatorioProdutosCadastradosParaPdf: deve mapear texto para DTO com titulo e tipo corretos")
    void deveMapearProdutosParaPdf() {
        when(produtoRepository.findTop30ByOrderByIdDesc()).thenReturn(List.of(produto));
        when(produtoRepository.count()).thenReturn(1L);
        when(produtoRepository.countByStatus("ATIVO")).thenReturn(1L);
        when(produtoRepository.countByStatus("INATIVO")).thenReturn(0L);
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("429 quota exceeded"));
        when(assistenteFallbackService.gerarRelatorioFallback(anyString(), anyString(), anyString(),
                any(RuntimeException.class)))
                .thenReturn("texto-produtos");

        RelatorioAssistenteDTO dto = new RelatorioAssistenteDTO(
                "Resumo",
                "produtos-cadastrados",
                "media",
                List.of("linha"),
                List.of("acao"));

        when(assistenteRelatorioPdfMapperService.converterTextoEmRelatorioDto(
                "Histórico de Produtos Cadastrados",
                "produtos-cadastrados",
                "texto-produtos")).thenReturn(dto);

        RelatorioAssistenteDTO resultado = service.gerarRelatorioProdutosCadastradosParaPdf("Pergunta");

        assertSame(dto, resultado);
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
