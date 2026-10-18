package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;
import school.sptech.projeto_amar.domain.PreferenciaNotificacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.ProdutoLote;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.configuracao.ConfiguracaoMetricaDTO;
import school.sptech.projeto_amar.event.EstoqueAlteradoEvent;
import school.sptech.projeto_amar.event.MovimentacaoCriadaEvent;
import school.sptech.projeto_amar.repository.PreferenciaNotificacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoEstoqueServiceTest {
  @Mock private PreferenciaNotificacaoRepository preferenciaRepository;
  @Mock private ProdutoLoteRepository produtoLoteRepository;
  @Mock private ProdutoRepository produtoRepository;
  @Mock private ConfiguracaoNotificacaoService configuracaoService;
  @Mock private EmailAlertaService emailService;

  private NotificacaoEstoqueService service;
  private PreferenciaNotificacao preferencia;

  @BeforeEach
  void setUp() {
    service = new NotificacaoEstoqueService(preferenciaRepository, produtoLoteRepository,
        produtoRepository, configuracaoService, emailService, "America/Sao_Paulo", Runnable::run);
    Usuario usuario = new Usuario();
    usuario.setStatus("ATIVO");
    preferencia = new PreferenciaNotificacao();
    preferencia.setUsuario(usuario);
    preferencia.setEmailNotificacao("alertas@amar.org");
  }

  @Test
  void entradaDisparaSomentePreferenciasDeNovasEntradas() {
    when(preferenciaRepository.findAllDeUsuariosAtivos()).thenReturn(List.of(preferencia));

    service.aoCriarMovimentacao(new MovimentacaoCriadaEvent(TipoMovimentacao.ENTRADA,
        "Arroz", 12, LocalDate.of(2026, 10, 14), "Doação"));

    verify(emailService).enviar(eq("alertas@amar.org"), contains("Nova entrada"), anyString(), anyString());
  }

  @Test
  void listenerEnfileiraEmailESoProcessaDepoisSemAguardarSmtp() {
    List<Runnable> fila = new ArrayList<>();
    Executor executorDeTeste = fila::add;
    NotificacaoEstoqueService serviceAssincrono = new NotificacaoEstoqueService(preferenciaRepository,
        produtoLoteRepository, produtoRepository, configuracaoService, emailService,
        "America/Sao_Paulo", executorDeTeste);
    when(preferenciaRepository.findAllDeUsuariosAtivos()).thenReturn(List.of(preferencia));

    serviceAssincrono.aoCriarMovimentacao(new MovimentacaoCriadaEvent(TipoMovimentacao.ENTRADA,
        "Arroz", 12, LocalDate.of(2026, 10, 14), "Doação"));

    verifyNoInteractions(emailService);
    fila.getFirst().run();

    verify(emailService).enviar(eq("alertas@amar.org"), contains("Nova entrada"), anyString(), anyString());
  }

  @Test
  void filaCheiaNaoBloqueiaNemPropagaErroParaMovimentacao() {
    Executor filaCheia = tarefa -> { throw new TaskRejectedException("fila cheia"); };
    NotificacaoEstoqueService serviceComFilaCheia = new NotificacaoEstoqueService(preferenciaRepository,
        produtoLoteRepository, produtoRepository, configuracaoService, emailService,
        "America/Sao_Paulo", filaCheia);

    assertDoesNotThrow(() -> serviceComFilaCheia.aoCriarMovimentacao(new MovimentacaoCriadaEvent(
        TipoMovimentacao.ENTRADA, "Arroz", 12, LocalDate.of(2026, 10, 14), "Doação")));
    verifyNoInteractions(preferenciaRepository, emailService);
  }

  @Test
  void perdaNaoDisparaEmailDeNovaSaida() {
    service.aoCriarMovimentacao(new MovimentacaoCriadaEvent(TipoMovimentacao.PERDA,
        "Arroz", 2, LocalDate.of(2026, 10, 14), "Avaria"));

    verifyNoInteractions(preferenciaRepository, emailService);
  }

  @Test
  void estoqueBaixoNotificaNaTransicaoAbaixoDoLimite() {
    when(configuracaoService.obterMetricas()).thenReturn(new ConfiguracaoMetricaDTO(10, 30, 7, 60, 30));
    when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(8);
    Produto produto = new Produto();
    produto.setId(1);
    produto.setNome("Arroz");
    produto.setStatus("ATIVO");
    when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
    when(preferenciaRepository.findAllDeUsuariosAtivos()).thenReturn(List.of(preferencia));

    service.aoAlterarEstoque(new EstoqueAlteradoEvent(1, "Arroz", 10));

    verify(emailService).enviar(eq("alertas@amar.org"), contains("estoque baixo"), anyString(), anyString());
  }

  @Test
  void estoqueQueJaEstavaAbaixoDoLimiteNaoGeraAlertaDuplicado() {
    when(configuracaoService.obterMetricas()).thenReturn(new ConfiguracaoMetricaDTO(10, 30, 7, 60, 30));
    when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(7);

    service.aoAlterarEstoque(new EstoqueAlteradoEvent(1, "Arroz", 8));

    verifyNoInteractions(emailService);
  }

  @Test
  void resumoDeVencimentosEnviaUmEmailQuandoExistemLotes() {
    when(configuracaoService.obterMetricas()).thenReturn(new ConfiguracaoMetricaDTO(10, 30, 7, 60, 30));
    Produto produto = new Produto();
    produto.setNome("Feijão");
    ProdutoLote lote = new ProdutoLote();
    lote.setId(3);
    lote.setProduto(produto);
    lote.setQuantidade(4);
    lote.setValidade(LocalDate.now().plusDays(5));
    when(produtoLoteRepository.findAlimentosComRiscoDeVencimento(any(), any())).thenReturn(List.of(lote));
    when(preferenciaRepository.findAllDeUsuariosAtivos()).thenReturn(List.of(preferencia));

    service.enviarResumoDiarioDeVencimentos();

    verify(emailService).enviar(eq("alertas@amar.org"), contains("vencimento"), contains("Feijão"), contains("Feijão"));
  }
}
