package school.sptech.projeto_amar.dto.dashboard;

public record EstoqueProdutoAtualResponseDTO(
    String produto,
    String categoria,
    Long quantidade) {
}
