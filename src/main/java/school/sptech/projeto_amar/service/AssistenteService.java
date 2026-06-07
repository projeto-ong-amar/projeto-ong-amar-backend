package school.sptech.projeto_amar.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;
import school.sptech.projeto_amar.exception.assistenteException.ErroAoChamarIaException;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

@Service
public class AssistenteService {
        private static final String STATUS_ATIVO = "ATIVO";
        private static final String STATUS_INATIVO = "INATIVO";

        private final ChatModel chatModel;
        private final MovimentacaoRepository movimentacaoRepository;
        private final ProdutoRepository produtoRepository;
        private final ProdutoLoteRepository produtoLoteRepository;
        private final AssistenteFallbackService assistenteFallbackService;
        private final AssistenteIaResponseValidatorService assistenteIaResponseValidatorService;
        private final AssistenteRelatorioPdfMapperService assistenteRelatorioPdfMapperService;

        public AssistenteService(
                        ChatModel chatModel,
                        MovimentacaoRepository movimentacaoRepository,
                        ProdutoRepository produtoRepository,
                        ProdutoLoteRepository produtoLoteRepository,
                        AssistenteFallbackService assistenteFallbackService,
                        AssistenteIaResponseValidatorService assistenteIaResponseValidatorService,
                        AssistenteRelatorioPdfMapperService assistenteRelatorioPdfMapperService) {
                this.chatModel = chatModel;
                this.movimentacaoRepository = movimentacaoRepository;
                this.produtoRepository = produtoRepository;
                this.produtoLoteRepository = produtoLoteRepository;
                this.assistenteFallbackService = assistenteFallbackService;
                this.assistenteIaResponseValidatorService = assistenteIaResponseValidatorService;
                this.assistenteRelatorioPdfMapperService = assistenteRelatorioPdfMapperService;
        }

        public String gerarRelatorioHistoricoEntradas(String pergunta) {
                List<Movimentacao> entradas = movimentacaoRepository
                                .findTop20ByTipoOrderByDataDescIdDesc(TipoMovimentacao.ENTRADA);

                Long totalEntradas = movimentacaoRepository.countByTipo(TipoMovimentacao.ENTRADA);
                Integer quantidadeEntradas = movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.ENTRADA);

                StringBuilder dados = new StringBuilder();
                dados.append("Total de movimentações de entrada registradas: ").append(totalEntradas).append("\n");
                dados.append("Total de itens que entraram no estoque: ").append(quantidadeEntradas).append("\n\n");
                dados.append("Últimas entradas registradas (até 20):\n");

                if (entradas.isEmpty()) {
                        dados.append("- Nenhuma entrada cadastrada até o momento.\n");
                } else {
                        for (Movimentacao entrada : entradas) {
                                dados.append("- Data: ").append(entrada.getData())
                                                .append(" | Produto: ").append(entrada.getProduto().getNome())
                                                .append(" | Quantidade: ").append(entrada.getQuantidade())
                                                .append(" | Responsável: ").append(entrada.getUsuario().getNome())
                                                .append(" | Motivo: ").append(entrada.getMotivo())
                                                .append("\n");
                        }
                }

                return gerarTextoRelatorio(
                                "histórico de entradas",
                                dados.toString(),
                                pergunta);
        }

        public String gerarRelatorioHistoricoSaidas(String pergunta) {
                List<Movimentacao> saidas = movimentacaoRepository
                                .findTop20ByTipoOrderByDataDescIdDesc(TipoMovimentacao.SAIDA);

                Long totalSaidas = movimentacaoRepository.countByTipo(TipoMovimentacao.SAIDA);
                Integer quantidadeSaidas = movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.SAIDA);

                StringBuilder dados = new StringBuilder();
                dados.append("Total de movimentações de saída registradas: ").append(totalSaidas).append("\n");
                dados.append("Total de itens que saíram do estoque: ").append(quantidadeSaidas).append("\n\n");
                dados.append("Últimas saídas registradas (até 20):\n");

                if (saidas.isEmpty()) {
                        dados.append("- Nenhuma saída cadastrada até o momento.\n");
                } else {
                        for (Movimentacao saida : saidas) {
                                dados.append("- Data: ").append(saida.getData())
                                                .append(" | Produto: ").append(saida.getProduto().getNome())
                                                .append(" | Quantidade: ").append(saida.getQuantidade())
                                                .append(" | Responsável: ").append(saida.getUsuario().getNome())
                                                .append(" | Motivo: ").append(saida.getMotivo())
                                                .append("\n");
                        }
                }

                return gerarTextoRelatorio(
                                "histórico de saídas",
                                dados.toString(),
                                pergunta);
        }

        public String gerarRelatorioDesempenhoGeral(String pergunta) {
                Long totalProdutos = produtoRepository.count();
                Long produtosAtivos = produtoRepository.countByStatus(STATUS_ATIVO);
                Long produtosInativos = produtoRepository.countByStatus(STATUS_INATIVO);
                Integer estoqueTotal = produtoLoteRepository.sumQuantidadeTotal();

                Long totalEntradas = movimentacaoRepository.countByTipo(TipoMovimentacao.ENTRADA);
                Long totalSaidas = movimentacaoRepository.countByTipo(TipoMovimentacao.SAIDA);
                Long totalPerdas = movimentacaoRepository.countByTipo(TipoMovimentacao.PERDA);

                Integer quantidadeEntradas = movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.ENTRADA);
                Integer quantidadeSaidas = movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.SAIDA);
                Integer quantidadePerdas = movimentacaoRepository.sumQuantidadeByTipo(TipoMovimentacao.PERDA);

                LocalDate hoje = LocalDate.now();
                Long lotesVencidos = produtoLoteRepository.countLotesVencidos(hoje);
                Long lotesProximosVencimento = produtoLoteRepository
                                .countLotesProximosDoVencimento(hoje, hoje.plusDays(7));

                List<Object[]> topEntradas = movimentacaoRepository
                                .findTopProdutosPorTipo(TipoMovimentacao.ENTRADA, PageRequest.of(0, 5));
                List<Object[]> topSaidas = movimentacaoRepository
                                .findTopProdutosPorTipo(TipoMovimentacao.SAIDA, PageRequest.of(0, 5));

                StringBuilder dados = new StringBuilder();
                dados.append("Visão geral operacional:\n");
                dados.append("- Total de produtos cadastrados: ").append(totalProdutos).append("\n");
                dados.append("- Produtos ativos: ").append(produtosAtivos).append("\n");
                dados.append("- Produtos inativos: ").append(produtosInativos).append("\n");
                dados.append("- Itens totais em estoque (soma dos lotes): ").append(estoqueTotal).append("\n\n");

                dados.append("Movimentações:\n");
                dados.append("- Entradas registradas: ").append(totalEntradas)
                                .append(" (itens: ").append(quantidadeEntradas).append(")\n");
                dados.append("- Saídas registradas: ").append(totalSaidas)
                                .append(" (itens: ").append(quantidadeSaidas).append(")\n");
                dados.append("- Perdas registradas: ").append(totalPerdas)
                                .append(" (itens: ").append(quantidadePerdas).append(")\n\n");

                dados.append("Alertas de validade:\n");
                dados.append("- Lotes vencidos com saldo: ").append(lotesVencidos).append("\n");
                dados.append("- Lotes com vencimento em até 7 dias: ").append(lotesProximosVencimento).append("\n\n");

                dados.append("Top 5 produtos por entradas:\n");
                appendTopProdutos(dados, topEntradas);
                dados.append("\nTop 5 produtos por saídas:\n");
                appendTopProdutos(dados, topSaidas);

                return gerarTextoRelatorio(
                                "desempenho geral do estoque",
                                dados.toString(),
                                pergunta);
        }

        public String gerarRelatorioHistoricoProdutosCadastrados(String pergunta) {
                List<Produto> produtos = produtoRepository.findTop30ByOrderByIdDesc();

                Long totalProdutos = produtoRepository.count();
                Long produtosAtivos = produtoRepository.countByStatus(STATUS_ATIVO);
                Long produtosInativos = produtoRepository.countByStatus(STATUS_INATIVO);

                StringBuilder dados = new StringBuilder();
                dados.append("Resumo de cadastros de produtos:\n");
                dados.append("- Total de produtos cadastrados: ").append(totalProdutos).append("\n");
                dados.append("- Produtos ativos: ").append(produtosAtivos).append("\n");
                dados.append("- Produtos inativos: ").append(produtosInativos).append("\n\n");
                dados.append("Últimos produtos cadastrados (ordenados por ID desc, até 30):\n");

                if (produtos.isEmpty()) {
                        dados.append("- Nenhum produto cadastrado até o momento.\n");
                } else {
                        for (Produto produto : produtos) {
                                dados.append("- ID: ").append(produto.getId())
                                                .append(" | Nome: ").append(produto.getNome())
                                                .append(" | Categoria: ").append(produto.getCategoria().getNome())
                                                .append(" | Unidade: ").append(produto.getUnidadeMedida())
                                                .append(" | Status: ").append(produto.getStatus())
                                                .append("\n");
                        }
                }

                return gerarTextoRelatorio(
                                "histórico de produtos cadastrados",
                                dados.toString(),
                                pergunta);
        }

        public RelatorioAssistenteDTO gerarRelatorioHistoricoEntradasParaPdf(String pergunta) {
                String texto = gerarRelatorioHistoricoEntradas(pergunta);
                return assistenteRelatorioPdfMapperService.converterTextoEmRelatorioDto(
                                "Histórico de Entradas",
                                "historico-entradas",
                                texto);
        }

        public RelatorioAssistenteDTO gerarRelatorioHistoricoSaidasParaPdf(String pergunta) {
                String texto = gerarRelatorioHistoricoSaidas(pergunta);
                return assistenteRelatorioPdfMapperService.converterTextoEmRelatorioDto(
                                "Histórico de Saídas",
                                "historico-saidas",
                                texto);
        }

        public RelatorioAssistenteDTO gerarRelatorioDesempenhoGeralParaPdf(String pergunta) {
                String texto = gerarRelatorioDesempenhoGeral(pergunta);
                return assistenteRelatorioPdfMapperService.converterTextoEmRelatorioDto(
                                "Desempenho Geral do Estoque",
                                "desempenho-geral",
                                texto);
        }

        public RelatorioAssistenteDTO gerarRelatorioProdutosCadastradosParaPdf(String pergunta) {
                String texto = gerarRelatorioHistoricoProdutosCadastrados(pergunta);
                return assistenteRelatorioPdfMapperService.converterTextoEmRelatorioDto(
                                "Histórico de Produtos Cadastrados",
                                "produtos-cadastrados",
                                texto);
        }

        private String gerarTextoRelatorio(String tipoRelatorio, String dados, String pergunta) {
                String perguntaFinal = (pergunta == null || pergunta.isBlank())
                                ? "Analise os dados e destaque os principais pontos de atenção e ações recomendadas."
                                : pergunta;

                String msg = """
                                Você é um assistente logístico especializado em gerenciamento de alimentos e produtos de higiene.
                                Gere um relatório em texto puro sobre %s com base apenas nos dados abaixo.
                                Seja objetivo, inclua insights e recomendações práticas de ação.
                                Regras obrigatórias de formatação da resposta:
                                - Não use Markdown.
                                - Não use asteriscos (*), hífens de lista (-), cerquilha (#), crases (`) ou qualquer marcador visual.
                                - Não use listas com bullets, numeração ou símbolos especiais no início das linhas.
                                - Retorne somente texto corrido simples, com frases claras separadas por quebra de linha.
                                Estrutura obrigatória da resposta (texto simples):
                                Resumo executivo: [uma frase objetiva sobre o cenário]
                                Prioridade: [baixa|media|alta|critica]
                                Análise: [parágrafo com principais pontos]
                                Ações recomendadas: [parágrafo com ações práticas]
                                Restrições de conteúdo:
                                - Use somente informações existentes nos dados do banco enviados.
                                - Não invente processos, contextos ou entidades externas.
                                - Não mencione doadores, campanhas, FIFO, sustentabilidade ou estratégias externas se isso não estiver nos dados.
                                - A análise deve citar explicitamente números presentes nos dados.

                                Pergunta do usuário:
                                %s

                                Dados do banco:
                                %s
                                """
                                .formatted(tipoRelatorio, perguntaFinal, dados);

                PromptTemplate template = new PromptTemplate(msg);
                Prompt prompt = template.create(Map.of(
                                "tipo", tipoRelatorio,
                                "pergunta", perguntaFinal,
                                "dados", dados));

                try {
                        String respostaIa = chatModel.call(prompt).getResult().getOutput().getText();
                        String respostaNormalizada = assistenteIaResponseValidatorService.normalizarSaidaIa(respostaIa);

                        if (!assistenteIaResponseValidatorService.isRespostaAderenteAosDados(respostaNormalizada,
                                        dados)) {
                                return assistenteFallbackService.gerarRelatorioDeterministico(tipoRelatorio, dados);
                        }

                        return respostaNormalizada;
                } catch (RuntimeException ex) {
                        if (isQuotaExceeded(ex)) {
                                return assistenteFallbackService.gerarRelatorioFallback(tipoRelatorio, perguntaFinal,
                                                dados, ex);
                        }
                        throw new ErroAoChamarIaException("Erro ao chamar serviço de IA para gerar relatório.");
                }
        }

        private boolean isQuotaExceeded(Throwable throwable) {
                Throwable atual = throwable;
                while (atual != null) {
                        String mensagem = atual.getMessage();
                        if (mensagem != null) {
                                String texto = mensagem.toLowerCase();
                                if (texto.contains("429")
                                                || texto.contains("quota")
                                                || texto.contains("rate limit")
                                                || texto.contains("exceeded your current quota")) {
                                        return true;
                                }
                        }
                        atual = atual.getCause();
                }
                return false;
        }

        private void appendTopProdutos(StringBuilder dados, List<Object[]> topProdutos) {
                if (topProdutos.isEmpty()) {
                        dados.append("- Sem dados suficientes para ranking.\n");
                        return;
                }

                for (Object[] item : topProdutos) {
                        dados.append("- Produto: ").append(item[0])
                                        .append(" | Quantidade movimentada: ").append(item[1])
                                        .append("\n");
                }
        }
}