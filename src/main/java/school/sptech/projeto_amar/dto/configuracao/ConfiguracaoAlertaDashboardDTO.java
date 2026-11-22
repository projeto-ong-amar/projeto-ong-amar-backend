package school.sptech.projeto_amar.dto.configuracao;

import jakarta.validation.constraints.NotNull;

public record ConfiguracaoAlertaDashboardDTO(
    @NotNull Boolean vencimentoProximo,
    @NotNull Boolean estoqueBaixo,
    @NotNull Boolean semDoacoes,
    @NotNull Boolean semMovimentacao) {
}
