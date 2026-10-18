package school.sptech.projeto_amar.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import school.sptech.projeto_amar.dto.configuracao.ConfiguracaoMetricaDTO;
import school.sptech.projeto_amar.dto.configuracao.PreferenciaNotificacaoDTO;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.service.ConfiguracaoNotificacaoService;

@RestController
@RequestMapping("/configuracoes")
public class ConfiguracaoNotificacaoController {
  private final ConfiguracaoNotificacaoService service;

  public ConfiguracaoNotificacaoController(ConfiguracaoNotificacaoService service) {
    this.service = service;
  }

  @GetMapping("/preferencias-notificacao")
  public ResponseEntity<PreferenciaNotificacaoDTO> obterPreferencias(
      @AuthenticationPrincipal UsuarioDetalhesDTO usuario) {
    return ResponseEntity.ok(service.obterPreferencias(usuario.getId()));
  }

  @PutMapping("/preferencias-notificacao")
  public ResponseEntity<PreferenciaNotificacaoDTO> atualizarPreferencias(
      @AuthenticationPrincipal UsuarioDetalhesDTO usuario,
      @Valid @RequestBody PreferenciaNotificacaoDTO dto) {
    return ResponseEntity.ok(service.atualizarPreferencias(usuario.getId(), dto));
  }

  @GetMapping("/metricas")
  public ResponseEntity<ConfiguracaoMetricaDTO> obterMetricas() {
    return ResponseEntity.ok(service.obterMetricas());
  }

  @PutMapping("/metricas")
  public ResponseEntity<ConfiguracaoMetricaDTO> atualizarMetricas(
      @AuthenticationPrincipal UsuarioDetalhesDTO usuario,
      @Valid @RequestBody ConfiguracaoMetricaDTO dto) {
    return ResponseEntity.ok(service.atualizarMetricas(dto, usuario.getId()));
  }
}
