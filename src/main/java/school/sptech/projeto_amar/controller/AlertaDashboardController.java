package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import school.sptech.projeto_amar.dto.configuracao.ConfiguracaoAlertaDashboardDTO;
import school.sptech.projeto_amar.dto.dashboard.AlertaDashboardDTO;
import school.sptech.projeto_amar.service.AlertaDashboardService;

@RestController
public class AlertaDashboardController {
  private final AlertaDashboardService service;

  public AlertaDashboardController(AlertaDashboardService service) {
    this.service = service;
  }

  @GetMapping("/configuracoes/alertas-dashboard")
  @Operation(summary = "Consultar preferências globais dos alertas do dashboard")
  public ConfiguracaoAlertaDashboardDTO obterConfiguracao() {
    return service.obterConfiguracao();
  }

  @PutMapping("/configuracoes/alertas-dashboard")
  @Operation(summary = "Substituir preferências globais dos alertas do dashboard")
  public ConfiguracaoAlertaDashboardDTO atualizarConfiguracao(
      @Valid @RequestBody ConfiguracaoAlertaDashboardDTO dto) {
    return service.atualizarConfiguracao(dto);
  }

  @GetMapping("/dashboard/alertas")
  @Operation(summary = "Calcular alertas habilitados com os dados e limites atuais")
  public List<AlertaDashboardDTO> listarAlertas() {
    return service.listarAlertas();
  }
}
