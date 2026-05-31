package school.sptech.projeto_amar.dto.assistente;

import java.util.List;

public record RelatorioAssistenteDTO(
        String titulo,
        String tipo,
        String prioridade,
        List<String> response,
        List<String> acoes_recomendadas
) {
}