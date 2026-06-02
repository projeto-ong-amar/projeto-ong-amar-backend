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
                Total de produtos cadastrados: 186
                Total de itens em estoque: 947
                
                Situação crítica identificada:
                
                Produtos abaixo do estoque mínimo:
                - Arroz: 2 unidades restantes
                - Feijão: 1 unidade restante
                - Leite: 0 unidades disponíveis
                - Óleo: 3 unidades restantes
                - Café: 2 unidades restantes
                
                Produtos sem reposição há mais de 45 dias:
                - Leite
                - Café
                - Açúcar
                - Macarrão
                
                Produtos com validade próxima (menos de 7 dias):
                - 45 unidades de pão de forma
                - 28 unidades de iogurte
                - 17 unidades de leite integral
                
                Produtos vencidos ainda armazenados:
                - 22 unidades de leite
                - 14 unidades de pão
                - 8 unidades de presunto
                
                Perdas registradas nos últimos 30 dias:
                - 54 unidades descartadas por vencimento
                - 31 unidades danificadas no transporte
                - 18 unidades extraviadas
                
                Produtos com excesso de estoque:
                - Molho de tomate: 380 unidades
                - Ervilha em conserva: 290 unidades
                - Milho em conserva: 310 unidades
                
                Produtos sem movimentação há mais de 90 dias:
                - Molho barbecue
                - Ervilha em conserva
                - Milho verde premium
                
                Maiores saídas dos últimos 30 dias:
                - Arroz
                - Feijão
                - Leite
                - Café
                - Óleo
                
                Alertas operacionais:
                - 3 movimentações de saída sem justificativa detalhada
                - Divergência de 27 unidades entre estoque físico e sistema
                - 2 lotes sem informação de validade
                - 1 usuário realizou mais de 120 movimentações em um único dia
                
                Tendências observadas:
                - Crescimento de 42% na demanda por alimentos básicos
                - Aumento de perdas por vencimento nos últimos 60 dias
                - Risco elevado de ruptura para produtos essenciais
                - Possível falha no planejamento de compras
                """;

        String prompt = """
                Você é um analista especialista em gestão de estoque para organizações sociais.
                
                Analise APENAS os dados fornecidos abaixo.
                
                IMPORTANTE:
                - Não faça comentários genéricos.
                - Cite quantidades, produtos e problemas encontrados.
                - Todas as observações devem ser baseadas nos dados informados.
                - Se existir risco de ruptura, divergência de estoque, produtos vencidos ou excesso de estoque, isso deve aparecer na análise.
                - As ações recomendadas devem resolver os problemas identificados.
                - Não invente informações.
                - Não explique sua resposta.
                - Retorne SOMENTE JSON válido.
                
                Formato obrigatório:
                
                {
                  "titulo":"string",
                  "tipo":"insight",
                  "prioridade":"baixa|media|alta|critica",
                  "response":[
                    "string",
                    "string",
                    "string"
                  ],
                  "acoes_recomendadas":[
                    "string",
                    "string",
                    "string"
                  ]
                }
                
                Exemplo de análise esperada:
                
                "response": [
                  "O leite encontra-se em situação crítica com 0 unidades disponíveis, apesar de estar entre os produtos de maior saída dos últimos 30 dias.",
                  "Existe divergência de 27 unidades entre o estoque físico e o sistema, indicando possível falha operacional ou de controle.",
                  "Foram registradas perdas de 54 unidades por vencimento e 31 por danos no transporte, demonstrando necessidade de melhoria na gestão dos lotes."
                ]
                
                Dados do estoque:
                
                %s
                
                Pergunta do usuário:
                
                %s
                
                Lembre-se:
                - Utilize os números reais apresentados.
                - Cite os nomes dos produtos.
                - Gere insights objetivos e específicos.
                - Prioridade deve ser CRÍTICA quando houver falta de produtos essenciais, produtos vencidos ou divergências relevantes de estoque.
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