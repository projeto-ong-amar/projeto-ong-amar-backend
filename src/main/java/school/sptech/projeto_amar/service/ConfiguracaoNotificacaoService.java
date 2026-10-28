package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.sptech.projeto_amar.domain.ConfiguracaoMetrica;
import school.sptech.projeto_amar.domain.PreferenciaNotificacao;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.configuracao.ConfiguracaoMetricaDTO;
import school.sptech.projeto_amar.dto.configuracao.PreferenciaNotificacaoDTO;
import school.sptech.projeto_amar.repository.ConfiguracaoMetricaRepository;
import school.sptech.projeto_amar.repository.PreferenciaNotificacaoRepository;
import school.sptech.projeto_amar.repository.UsuarioRepository;

@Service
public class ConfiguracaoNotificacaoService {
  private static final ConfiguracaoMetricaDTO METRICAS_PADRAO =
      new ConfiguracaoMetricaDTO(10, 30, 7, 60, 30);

  private final PreferenciaNotificacaoRepository preferenciaRepository;
  private final ConfiguracaoMetricaRepository metricaRepository;
  private final UsuarioRepository usuarioRepository;

  public ConfiguracaoNotificacaoService(PreferenciaNotificacaoRepository preferenciaRepository,
      ConfiguracaoMetricaRepository metricaRepository, UsuarioRepository usuarioRepository) {
    this.preferenciaRepository = preferenciaRepository;
    this.metricaRepository = metricaRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @Transactional(readOnly = true)
  public PreferenciaNotificacaoDTO obterPreferencias(Integer usuarioId) {
    return preferenciaRepository.findByUsuarioId(usuarioId)
        .map(this::paraDTO)
        .orElse(new PreferenciaNotificacaoDTO(true, true, true, true, ""));
  }

  @Transactional
  public PreferenciaNotificacaoDTO atualizarPreferencias(Integer usuarioId, PreferenciaNotificacaoDTO dto) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    PreferenciaNotificacao preferencia = preferenciaRepository.findByUsuarioId(usuarioId)
        .orElseGet(PreferenciaNotificacao::new);

    preferencia.setUsuario(usuario);
    preferencia.setEstoqueBaixo(dto.estoqueBaixo());
    preferencia.setRiscoVencimento(dto.riscoVencimento());
    preferencia.setNovasEntradas(dto.novasEntradas());
    preferencia.setNovasSaidas(dto.novasSaidas());
    preferencia.setEmailNotificacao(normalizarEmail(dto.emailNotificacao()));
    return paraDTO(preferenciaRepository.save(preferencia));
  }

  @Transactional(readOnly = true)
  public ConfiguracaoMetricaDTO obterMetricas() {
    return metricaRepository.findFirstByOrderByIdAsc()
        .map(this::paraDTO)
        .orElse(METRICAS_PADRAO);
  }

  @Transactional
  public ConfiguracaoMetricaDTO atualizarMetricas(ConfiguracaoMetricaDTO dto, Integer usuarioId) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    ConfiguracaoMetrica metrica = metricaRepository.findFirstByOrderByIdAsc()
        .orElseGet(ConfiguracaoMetrica::new);
    metrica.setEstoqueBaixo(dto.estoqueBaixo());
    metrica.setRiscoVencimentoDias(dto.riscoVencimentoDias());
    metrica.setVencimentoProximoDias(dto.vencimentoProximoDias());
    metrica.setSemMovimentacaoDias(dto.semMovimentacaoDias());
    metrica.setPeriodoIndicadoresDias(dto.periodoIndicadoresDias());
    metrica.setUsuarioAtualizacao(usuario);
    return paraDTO(metricaRepository.save(metrica));
  }

  private PreferenciaNotificacaoDTO paraDTO(PreferenciaNotificacao preferencia) {
    return new PreferenciaNotificacaoDTO(preferencia.isEstoqueBaixo(), preferencia.isRiscoVencimento(),
        preferencia.isNovasEntradas(), preferencia.isNovasSaidas(),
        preferencia.getEmailNotificacao() == null ? "" : preferencia.getEmailNotificacao());
  }

  private ConfiguracaoMetricaDTO paraDTO(ConfiguracaoMetrica metrica) {
    return new ConfiguracaoMetricaDTO(metrica.getEstoqueBaixo(), metrica.getRiscoVencimentoDias(),
        metrica.getVencimentoProximoDias(), metrica.getSemMovimentacaoDias(),
        metrica.getPeriodoIndicadoresDias());
  }

  private String normalizarEmail(String email) {
    return email == null || email.isBlank() ? null : email.trim();
  }
}
