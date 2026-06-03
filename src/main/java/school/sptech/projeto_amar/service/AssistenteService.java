package school.sptech.projeto_amar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class AssistenteService {

    private static final String MODEL = "phi3:mini";

    private static final String CONTEXTO_ESTOQUE = """
            Produtos críticos:
            Arroz: 2
            Feijão: 1
            Leite: 0
            Óleo: 3
            Café: 2

            Sem reposição:
            Leite, Café, Açúcar, Macarrão

            Próximos do vencimento:
            Pão(45), Iogurte(28), Leite(17)

            Vencidos:
            Leite(22), Pão(14), Presunto(8)

            Perdas:
            Vencimento(54)
            Transporte(31)
            Extravio(18)

            Excesso:
            Molho de tomate(380)
            Ervilha(290)
            Milho(310)

            Divergência:
            27 unidades

            Lotes sem validade:
            2

            Demanda:
            +42%
            """;

    public RelatorioAssistenteDTO responder(String pergunta) {

        String prompt = """
                Você é um analista de estoque.

                Responda SOMENTE em JSON válido.

                {
                  "titulo":"string",
                  "tipo":"insight",
                  "prioridade":"baixa|media|alta|critica",
                  "response":["string"],
                  "acoes_recomendadas":["string"]
                }

                Regras:
                - Não escreva nada fora do JSON.
                - Não utilize markdown.
                - Não utilize ```json.
                - Cite números reais.
                - Cite produtos reais.
                - Não gere informações genéricas.
                - Prioridade crítica se houver falta de produtos essenciais ou produtos vencidos.
                - Cada item dos arrays deve ser uma string simples.

                Contexto:
                %s

                Pergunta:
                %s
                """.formatted(
                CONTEXTO_ESTOQUE,
                pergunta
        );

        try {

            String respostaJson = chamarOllama(prompt);

            return new ObjectMapper().readValue(
                    respostaJson,
                    RelatorioAssistenteDTO.class
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new RelatorioAssistenteDTO(
                    "Falha na análise",
                    "insight",
                    "alta",
                    java.util.List.of(
                            "Não foi possível gerar a análise."
                    ),
                    java.util.List.of(
                            "Tentar novamente."
                    )
            );
        }
    }

    private String chamarOllama(String prompt) {

        try {

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> payload = Map.of(
                    "model", MODEL,
                    "prompt", prompt,
                    "stream", false,
                    "format", "json",
                    "options", Map.of(
                            "temperature", 0
                    )
            );

            String json =
                    mapper.writeValueAsString(payload);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "http://localhost:11434/api/generate"
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(json)
                            )
                            .build();

            HttpClient client =
                    HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            JsonNode node =
                    mapper.readTree(response.body());

            String resposta =
                    node.get("response").asText();

            System.out.println("JSON GERADO:");
            System.out.println(resposta);

            return resposta;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Erro ao chamar IA",
                    e
            );
        }
    }
}