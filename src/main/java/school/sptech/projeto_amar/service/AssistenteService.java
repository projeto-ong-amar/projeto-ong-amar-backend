package school.sptech.projeto_amar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.dto.assistente.OllamaResponseDTO;
import school.sptech.projeto_amar.exception.assistenteException.ErroAoChamarIaException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class AssistenteService {
    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final String MODEL = "phi3:mini";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public String responder(String pergunta) {

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

        Tendência observada:
        - A demanda por café aumentou nas últimas semanas
        - Produtos perecíveis apresentam aumento de perdas
        """;

        String prompt = """
        Você é um assistente inteligente de gestão de estoque e operações.

        Seu comportamento deve seguir princípios modernos de automação de atendimento:
        - responder de forma natural e contextualizada
        - analisar dados antes de responder
        - identificar riscos operacionais
        - gerar insights estratégicos
        - automatizar interpretações repetitivas
        - ajudar gestores na tomada de decisão

        Você NÃO é apenas um chatbot simples.
        Você atua como um analista operacional do sistema.

        Suas respostas devem:
        - explicar problemas encontrados
        - apontar padrões
        - sugerir melhorias
        - alertar riscos
        - indicar prioridades
        - recomendar ações práticas

        Você deve observar:
        - produtos críticos
        - desperdícios
        - excesso de estoque
        - tendências de saída
        - riscos de ruptura
        - possíveis falhas operacionais

        Quando apropriado:
        - sugira reposição
        - sugira redução de compras
        - sugira auditoria
        - sugira investigação de perdas
        - sugira reorganização do estoque

        Responda SOMENTE em JSON válido.

        Formato obrigatório:
        {
          "titulo": "string",
          "tipo": "insight|alerta|relatorio|analise",
          "prioridade": "baixa|media|alta|critica",
          "response": ["string"],
          "acoes_recomendadas": ["string"]
        }

        Regras:
        - NÃO use markdown
        - NÃO escreva fora do JSON
        - Gere respostas analíticas
        - Use linguagem natural
        - Explique os motivos das conclusões
        - Seja estratégico e operacional
        - Cada item do array deve ser uma frase completa

        Dados atuais do estoque:
        %s

        Pergunta do usuário:
        %s
        """.formatted(
                resumoEstoqueMockado,
                pergunta
        );

        return chamarOllama(prompt);
    }

    private String chamarOllama(String prompt) {

        try {

            Map<String, Object> payload = Map.of(
                    "model", MODEL,
                    "prompt", prompt,
                    "stream", false,
                    "format", "json",
                    "options", Map.of(
                            "temperature", 1,
                            "num_predict", 800,
                            "num_ctx", 4096
                    )
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

            if (response.statusCode() != 200) {
                return """
                        {
                          "titulo": "Erro",
                          "response": [
                            "Erro HTTP ao chamar Ollama"
                          ]
                        }
                        """;
            }

            OllamaResponseDTO resposta = mapper.readValue(
                    response.body(),
                    OllamaResponseDTO.class
            );

            return resposta.response();

        } catch (Exception e) {

            return """
                    {
                      "titulo": "Erro",
                      "response": [
                        "Erro ao chamar IA"
                      ]
                    }
                    """;
        }
    }
}