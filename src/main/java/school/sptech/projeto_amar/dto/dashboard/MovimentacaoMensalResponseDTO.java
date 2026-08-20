package school.sptech.projeto_amar.dto.dashboard;

public record MovimentacaoMensalResponseDTO(
    Integer ano,
    Integer mes,
    Long itensRecebidos,
    Long itensEntregues) {
}