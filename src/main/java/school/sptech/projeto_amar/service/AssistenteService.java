package school.sptech.projeto_amar.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.dto.assistente.OllamaResponseDTO;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class AssistenteService {

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final String MODEL = "phi3:mini";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public RelatorioAssistenteDTO responder(String pergunta) {

        String resumoEstoqueMockado = """
                Total de produtos cadastrados: 125
                Total de itens em estoque: 1840

                Produtos abaixo do estoque mínimo:
                - Feijão: 12 unidades restantes
                - Leite: 5 unidades restantes
                - Café: 3 unidades restantes

                Produtos com maior saída nos últimos 30 dias:
                - Leite
                - Pão
                - Café
                - Arroz

                Produtos parados há mais de 60 dias:
                - Molho de tomate premium
                - Ervilha em conserva

                Produtos com excesso de estoque:
                - Arroz: 240 unidades
                - Macarrão: 180 unidades

                Perdas registradas:
                - 12 unidades de pão vencidas
                - 5 unidades de leite descartadas
                - 8 frutas danificadas no transporte
                """;

        String prompt = """
                Você é um analista operacional de estoque.

                Responda SOMENTE em JSON válido.

                Formato obrigatório:

                {
                  "titulo":"string",
                  "tipo":"insight|alerta|relatorio|analise",
                  "prioridade":"baixa|media|alta|critica",
                  "response":["string"],
                  "acoes_recomendadas":["string"]
                }

                Dados:
                %s

                Pergunta:
                %s
                """.formatted(resumoEstoqueMockado, pergunta);

        try {

            String jsonResposta = chamarOllama(prompt);

            return mapper.readValue(
                    jsonResposta,
                    RelatorioAssistenteDTO.class
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao converter resposta da IA",
                    e
            );
        }
    }

    private String chamarOllama(String prompt) {

        try {

            Map<String, Object> payload = Map.of(
                    "model", MODEL,
                    "prompt", prompt,
                    "stream", false,
                    "format", "json"
            );

            String json = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(response.body());

            OllamaResponseDTO resposta =
                    mapper.readValue(
                            response.body(),
                            OllamaResponseDTO.class
                    );

            return resposta.response();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao chamar Ollama",
                    e
            );
        }
    }
}