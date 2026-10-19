package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Configurações", description = "Preferências de notificação e limites das métricas do sistema")
public class ConfiguracaoNotificacaoController {
  private final ConfiguracaoNotificacaoService service;

  public ConfiguracaoNotificacaoController(ConfiguracaoNotificacaoService service) {
    this.service = service;
  }

  @GetMapping("/preferencias-notificacao")
  @Operation(summary = "Consultar preferências de notificação", description = "Retorna as preferências de notificação do usuário autenticado. "
      + "Caso ainda não existam preferências salvas, retorna todas as notificações habilitadas e o e-mail vazio.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Preferências retornadas com sucesso"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<PreferenciaNotificacaoDTO> obterPreferencias(
      @AuthenticationPrincipal UsuarioDetalhesDTO usuario) {
    return ResponseEntity.ok(service.obterPreferencias(usuario.getId()));
  }

  @PutMapping("/preferencias-notificacao")
  @Operation(summary = "Atualizar preferências de notificação", description = "Substitui as preferências de notificação do usuário autenticado. "
      + "O e-mail é opcional; um valor vazio ou nulo é armazenado sem e-mail de notificação.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Preferências atualizadas com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail em formato inválido"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
      @ApiResponse(responseCode = "403", description = "Token CSRF ausente ou inválido")
  })
  public ResponseEntity<PreferenciaNotificacaoDTO> atualizarPreferencias(
      @AuthenticationPrincipal UsuarioDetalhesDTO usuario,
      @Valid @RequestBody PreferenciaNotificacaoDTO dto) {
    return ResponseEntity.ok(service.atualizarPreferencias(usuario.getId(), dto));
  }

  @GetMapping("/metricas")
  @Operation(summary = "Consultar configurações das métricas", description = "Retorna os limites globais usados pelos indicadores de estoque. "
      + "Se ainda não houver configuração salva, retorna os valores padrão: 10, 30, 7, 60 e 30 dias, respectivamente.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Configurações das métricas retornadas com sucesso"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<ConfiguracaoMetricaDTO> obterMetricas() {
    return ResponseEntity.ok(service.obterMetricas());
  }

  @PutMapping("/metricas")
  @Operation(summary = "Atualizar configurações das métricas", description = "Atualiza os limites globais usados pelos indicadores de estoque. "
      + "Todos os valores devem ser inteiros positivos.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Configurações das métricas atualizadas com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos; todos os limites devem ser maiores que zero"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
      @ApiResponse(responseCode = "403", description = "Token CSRF ausente ou inválido")
  })
  public ResponseEntity<ConfiguracaoMetricaDTO> atualizarMetricas(
      @AuthenticationPrincipal UsuarioDetalhesDTO usuario,
      @Valid @RequestBody ConfiguracaoMetricaDTO dto) {
    return ResponseEntity.ok(service.atualizarMetricas(dto, usuario.getId()));
  }
}
