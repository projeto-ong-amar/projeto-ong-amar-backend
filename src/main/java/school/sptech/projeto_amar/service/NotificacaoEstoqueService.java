package school.sptech.projeto_amar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import school.sptech.projeto_amar.domain.PreferenciaNotificacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.dashboard.AlimentoRiscoVencimentoResponseDTO;
import school.sptech.projeto_amar.event.EstoqueAlteradoEvent;
import school.sptech.projeto_amar.event.MovimentacaoCriadaEvent;
import school.sptech.projeto_amar.repository.PreferenciaNotificacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

@Service
public class NotificacaoEstoqueService {
  private static final Logger logger = LoggerFactory.getLogger(NotificacaoEstoqueService.class);

  private final PreferenciaNotificacaoRepository preferenciaRepository;
  private final ProdutoLoteRepository produtoLoteRepository;
  private final ProdutoRepository produtoRepository;
  private final ConfiguracaoNotificacaoService configuracaoService;
  private final EmailAlertaService emailService;
  private final ZoneId timeZone;
  private final Executor alertaEmailExecutor;

  public NotificacaoEstoqueService(PreferenciaNotificacaoRepository preferenciaRepository,
      ProdutoLoteRepository produtoLoteRepository, ProdutoRepository produtoRepository,
      ConfiguracaoNotificacaoService configuracaoService, EmailAlertaService emailService,
      @Value("${app.notificacoes.time-zone:America/Sao_Paulo}") String timeZone,
      @Qualifier("alertaEmailExecutor") Executor alertaEmailExecutor) {
    this.preferenciaRepository = preferenciaRepository;
    this.produtoLoteRepository = produtoLoteRepository;
    this.produtoRepository = produtoRepository;
    this.configuracaoService = configuracaoService;
    this.emailService = emailService;
    this.timeZone = ZoneId.of(timeZone);
    this.alertaEmailExecutor = alertaEmailExecutor;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void aoCriarMovimentacao(MovimentacaoCriadaEvent evento) {
    enfileirar("notificação da movimentação " + evento.tipo(), () -> processarMovimentacaoCriada(evento));
  }

  private void processarMovimentacaoCriada(MovimentacaoCriadaEvent evento) {
    boolean entrada = evento.tipo() == TipoMovimentacao.ENTRADA;
    boolean saida = evento.tipo() == TipoMovimentacao.SAIDA;
    if (!entrada && !saida) return;

    String assunto = entrada ? "Nova entrada de estoque - Projeto AMAR" : "Nova saída de estoque - Projeto AMAR";
    String tipo = entrada ? "entrada" : "saída";
    String texto = "Uma nova " + tipo + " foi registrada. Produto: " + evento.produto()
        + ". Quantidade: " + evento.quantidade() + ". Data: " + evento.data()
        + ". Motivo: " + evento.motivo() + ".";
    String html = "<h2>Nova " + tipo + " registrada</h2><p><strong>Produto:</strong> "
        + escapar(evento.produto()) + "</p><p><strong>Quantidade:</strong> " + evento.quantidade()
        + "</p><p><strong>Data:</strong> " + evento.data() + "</p><p><strong>Motivo:</strong> "
        + escapar(evento.motivo()) + "</p>";

    enviarParaInscritos(entrada ? PreferenciaNotificacao::isNovasEntradas
        : PreferenciaNotificacao::isNovasSaidas, assunto, texto, html);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void aoAlterarEstoque(EstoqueAlteradoEvent evento) {
    enfileirar("alerta de estoque do produto " + evento.produtoId(), () -> processarAlteracaoEstoque(evento));
  }

  private void processarAlteracaoEstoque(EstoqueAlteradoEvent evento) {
    var metricas = configuracaoService.obterMetricas();
    int limite = metricas.estoqueBaixo();
    Integer quantidadeAtual = produtoLoteRepository.sumQuantidadeByProdutoId(evento.produtoId());
    int estoqueAtual = quantidadeAtual == null ? 0 : quantidadeAtual;
    if (estoqueAtual >= limite || evento.quantidadeAnterior() < limite) return;

    Produto produto = produtoRepository.findById(evento.produtoId()).orElse(null);
    if (produto == null || !"ATIVO".equals(produto.getStatus())) return;

    String assunto = "Alerta de estoque baixo - Projeto AMAR";
    String texto = "O produto " + evento.produto() + " está com estoque abaixo do limite. Estoque atual: "
        + estoqueAtual + "; limite: " + limite + ".";
    String html = "<h2>Alerta de estoque baixo</h2><p><strong>Produto:</strong> "
        + escapar(evento.produto()) + "</p><p><strong>Estoque atual:</strong> " + estoqueAtual
        + "</p><p><strong>Limite configurado:</strong> " + limite + "</p>";
    enviarParaInscritos(PreferenciaNotificacao::isEstoqueBaixo, assunto, texto, html);
  }

  @Scheduled(cron = "${app.notificacoes.risco-vencimento-cron:0 0 8 * * *}",
      zone = "${app.notificacoes.time-zone:America/Sao_Paulo}")
  public void enviarResumoDiarioDeVencimentos() {
    enfileirar("resumo diário de vencimentos", this::processarResumoDiarioDeVencimentos);
  }

  private void enfileirar(String contexto, Runnable tarefa) {
    try {
      alertaEmailExecutor.execute(() -> {
        try {
          tarefa.run();
        } catch (Exception exception) {
          logger.warn("Falha ao processar {}: {}", contexto, exception.getMessage());
        }
      });
    } catch (TaskRejectedException exception) {
      logger.warn("Fila de alertas cheia; tarefa descartada ({}): {}", contexto, exception.getMessage());
    } catch (RuntimeException exception) {
      logger.warn("Não foi possível enfileirar {}: {}", contexto, exception.getMessage());
    }
  }

  private void processarResumoDiarioDeVencimentos() {
    var metricas = configuracaoService.obterMetricas();
    LocalDate hoje = LocalDate.now(timeZone);
    List<AlimentoRiscoVencimentoResponseDTO> lotes = produtoLoteRepository
        .findAlimentosComRiscoDeVencimento(hoje, hoje.plusDays(metricas.riscoVencimentoDias()))
        .stream()
        .map(lote -> new AlimentoRiscoVencimentoResponseDTO(lote.getId(), lote.getProduto().getNome(),
            lote.getProduto().getCategoria() == null ? null : lote.getProduto().getCategoria().getNome(),
            lote.getQuantidade(), lote.getValidade()))
        .toList();
    if (lotes.isEmpty()) return;

    StringBuilder texto = new StringBuilder("Lotes com risco de vencimento nos próximos ")
        .append(metricas.riscoVencimentoDias()).append(" dias:\n");
    StringBuilder html = new StringBuilder("<h2>Lotes com risco de vencimento</h2><ul>");
    for (var lote : lotes) {
      texto.append("- ").append(lote.produto()).append(": ").append(lote.quantidade())
          .append(" unidade(s), validade ").append(lote.validade()).append("\n");
      html.append("<li><strong>").append(escapar(lote.produto())).append("</strong>: ")
          .append(lote.quantidade()).append(" unidade(s), validade ").append(lote.validade()).append("</li>");
    }
    html.append("</ul><p>Período configurado: ").append(metricas.riscoVencimentoDias()).append(" dias.</p>");
    enviarParaInscritos(PreferenciaNotificacao::isRiscoVencimento,
        "Alerta diário de vencimento - Projeto AMAR", texto.toString(), html.toString());
  }

  private void enviarParaInscritos(Predicate<PreferenciaNotificacao> filtro, String assunto,
      String texto, String html) {
    preferenciaRepository.findAllDeUsuariosAtivos().stream()
        .filter(filtro)
        .map(PreferenciaNotificacao::getEmailNotificacao)
        .filter(StringUtils::hasText)
        .map(String::trim)
        .distinct()
        .forEach(email -> enviarComTratamento(email, assunto, texto, html));
  }

  private void enviarComTratamento(String email, String assunto, String texto, String html) {
    try {
      emailService.enviar(email, assunto, texto, html);
    } catch (Exception exception) {
      logger.warn("Não foi possível enviar uma notificação de estoque pelo SMTP Gmail: {}", exception.getMessage());
    }
  }

  private String escapar(String valor) {
    if (valor == null) return "";
    return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;");
  }
}
