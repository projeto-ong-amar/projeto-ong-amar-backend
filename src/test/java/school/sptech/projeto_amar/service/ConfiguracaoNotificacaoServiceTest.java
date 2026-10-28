package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.sptech.projeto_amar.domain.ConfiguracaoMetrica;
import school.sptech.projeto_amar.domain.PreferenciaNotificacao;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.configuracao.ConfiguracaoMetricaDTO;
import school.sptech.projeto_amar.dto.configuracao.PreferenciaNotificacaoDTO;
import school.sptech.projeto_amar.repository.ConfiguracaoMetricaRepository;
import school.sptech.projeto_amar.repository.PreferenciaNotificacaoRepository;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoNotificacaoServiceTest {
  @Mock private PreferenciaNotificacaoRepository preferenciaRepository;
  @Mock private ConfiguracaoMetricaRepository metricaRepository;
  @Mock private UsuarioRepository usuarioRepository;
  @InjectMocks private ConfiguracaoNotificacaoService service;

  @Test
  void preferenciasNovasUsamDefaultsDaTela() {
    when(preferenciaRepository.findByUsuarioId(5)).thenReturn(Optional.empty());

    PreferenciaNotificacaoDTO resultado = service.obterPreferencias(5);

    assertEquals(true, resultado.estoqueBaixo());
    assertEquals(true, resultado.riscoVencimento());
    assertEquals(true, resultado.novasEntradas());
    assertEquals(true, resultado.novasSaidas());
    assertEquals("", resultado.emailNotificacao());
  }

  @Test
  void atualizarPreferenciasPersisteParaUsuarioAutenticado() {
    Usuario usuario = new Usuario();
    usuario.setId(5);
    PreferenciaNotificacaoDTO dto = new PreferenciaNotificacaoDTO(false, true, false, true, " alertas@amar.org ");
    when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario));
    when(preferenciaRepository.findByUsuarioId(5)).thenReturn(Optional.empty());
    when(preferenciaRepository.save(any(PreferenciaNotificacao.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PreferenciaNotificacaoDTO resultado = service.atualizarPreferencias(5, dto);

    assertEquals(false, resultado.estoqueBaixo());
    assertEquals(true, resultado.riscoVencimento());
    assertEquals(false, resultado.novasEntradas());
    assertEquals(true, resultado.novasSaidas());
    assertEquals("alertas@amar.org", resultado.emailNotificacao());
    verify(preferenciaRepository).save(any(PreferenciaNotificacao.class));
  }

  @Test
  void metricasNovasUsamDefaultsDoSistema() {
    when(metricaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

    assertEquals(new ConfiguracaoMetricaDTO(10, 30, 7, 60, 30), service.obterMetricas());
  }

  @Test
  void atualizarMetricasPersisteValoresEUsuarioAtualizador() {
    Usuario usuario = new Usuario();
    usuario.setId(2);
    ConfiguracaoMetricaDTO dto = new ConfiguracaoMetricaDTO(8, 20, 5, 45, 15);
    when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario));
    when(metricaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
    when(metricaRepository.save(any(ConfiguracaoMetrica.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    assertEquals(dto, service.atualizarMetricas(dto, 2));
    verify(metricaRepository).save(any(ConfiguracaoMetrica.class));
  }
}
