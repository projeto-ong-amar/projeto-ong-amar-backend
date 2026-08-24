package school.sptech.projeto_amar.service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;
import school.sptech.projeto_amar.dto.assistente.RelatorioIaResponseDTO;
import school.sptech.projeto_amar.dto.assistente.TabelaRelatorioDTO;
import school.sptech.projeto_amar.exception.assistenteException.ErroAoChamarIaException;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

@Service
public class AssistenteService {
        private static final String STATUS_ATIVO = "ATIVO";
        private static final String STATUS_INATIVO = "INATIVO";
        private static final int MAX_ITENS_LISTA_IA = 8;
        private static final int MAX_TABELAS_IA = 3;
        private static final int MAX_LINHAS_TABELA_DETERMINISTICA = 15;
        private static final int MAX_CARACTERES_CELULA = 120;
        private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private final ChatModel chatModel;
        private final MovimentacaoRepository movimentacaoRepository;
        private final ProdutoRepository produtoRepository;
        private final ProdutoLoteRepository produtoLoteRepository;
        private final AssistenteFallbackService assistenteFallbackService;
        private final AssistenteIaResponseValidatorService assistenteIaResponseValidatorService;
        private final AssistenteIaJsonParserService assistenteIaJsonParserService;

        public AssistenteService(
                        ChatModel chatModel,
                        MovimentacaoRepository movimentacaoRepository,
                        ProdutoRepository produtoRepository,
                        ProdutoLoteRepository produtoLoteRepository,
                        AssistenteFallbackService assistenteFallbackService,
                        AssistenteIaResponseValidatorService assistenteIaResponseValidatorService,
                        AssistenteIaJsonParserService assistenteIaJsonParserService) {
                this.chatModel = chatModel;
                this.movimentacaoRepository = movimentacaoRepository;
                this.produtoRepository = produtoRepository;
                this.produtoLoteRepository = produtoLoteRepository;
                this.assistenteFallbackService = assistenteFallbackService;
                this.assistenteIaResponseValidatorService = assistenteIaResponseValidatorService;
                this.assistenteIaJsonParserService = assistenteIaJsonParserService;
        }

        private record DadosRelatorio(String texto, List<TabelaRelatorioDTO> tabelas) {
        }

        private enum OrigemResultado {
                IA, DETERMINISTICO, COTA_EXCEDIDA
        }

        private record ResultadoIa(RelatorioIaResponseDTO conteudo, OrigemResultado origem, RuntimeException excecao) {
        }

        public String gerarRelatorioHistoricoEntradas(String pergunta) {
                DadosRelatorio dados = montarDadosHistoricoEntradas();
                return concluirRelatorioTexto("histórico de entradas", dados, pergunta);
        }

        public String gerarRelatorioHistoricoSaidas(String pergunta) {
                DadosRelatorio dados = montarDadosHistoricoSaidas();
                return concluirRelatorioTexto("histórico de saídas", dados, pergunta);
        }

        public String gerarRelatorioDesempenhoGeral(String pergunta) {
                DadosRelatorio dados = montarDadosDesempenhoGeral();
                return concluirRelatorioTexto("desempenho geral do estoque", dados, pergunta);
        }

        public String gerarRelatorioHistoricoProdutosCadastrados(String pergunta) {
                DadosRelatorio dados = montarDadosProdutosCadastrados();
                return concluirRelatorioTexto("histórico de produtos cadastrados", dados, pergunta);
        }

        public RelatorioAssistenteDTO gerarRelatorioHistoricoEntradasParaPdf(String pergunta) {
                return concluirRelatorioPdf(
                                "Histórico de Entradas",
                                "historico-entradas",
                                "histórico de entradas",
                                montarDadosHistoricoEntradas(),
                                pergunta);
        }

        public RelatorioAssistenteDTO gerarRelatorioHistoricoSaidasParaPdf(String pergunta) {
                return concluirRelatorioPdf(
                                "Histórico de Saídas",
                                "historico-saidas",
                                "histórico de saídas",
                                montarDadosHistoricoSaidas(),
                                pergunta);
        }

        public RelatorioAssistenteDTO gerarRelatorioDesempenhoGeralParaPdf(String pergunta) {
                return concluirRelatorioPdf(
                                "Desempenho Geral do Estoque",
                                "desempenho-geral",
                                "desempenho geral do estoque",
                                montarDadosDesempenhoGeral(),
                                pergunta);
        }

        public RelatorioAssistenteDTO gerarRelatorioProdutosCadastradosParaPdf(String pergunta) {
                return concluirRelatorioPdf(
                                "Histórico de Produtos Cadastrados",
                                "produtos-cadastrados",
                                "histórico de produtos cadastrados",
                                montarDadosProdutosCadastrados(),
                                pergunta);
        }

        private DadosRelatorio montarDadosHistoricoEntradas() {
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

                List<TabelaRelatorioDTO> tabelas = new ArrayList<>();
                tabelas.add(new TabelaRelatorioDTO(
                                "Totais de entradas",
                                List.of("Indicador", "Valor"),
                                List.of(
                                                List.of("Movimentações de entrada registradas",
                                                                String.valueOf(valorOuZero(totalEntradas))),
                                                List.of("Itens que entraram no estoque",
                                                                String.valueOf(valorOuZero(quantidadeEntradas))))));
                tabelas.add(criarTabelaUltimasMovimentacoes("Últimas entradas registradas", entradas));

                return new DadosRelatorio(dados.toString(), tabelas);
        }

        private DadosRelatorio montarDadosHistoricoSaidas() {
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

                List<TabelaRelatorioDTO> tabelas = new ArrayList<>();
                tabelas.add(new TabelaRelatorioDTO(
                                "Totais de saídas",
                                List.of("Indicador", "Valor"),
                                List.of(
                                                List.of("Movimentações de saída registradas",
                                                                String.valueOf(valorOuZero(totalSaidas))),
                                                List.of("Itens que saíram do estoque",
                                                                String.valueOf(valorOuZero(quantidadeSaidas))))));
                tabelas.add(criarTabelaUltimasMovimentacoes("Últimas saídas registradas", saidas));

                return new DadosRelatorio(dados.toString(), tabelas);
        }

        private DadosRelatorio montarDadosDesempenhoGeral() {
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

                List<TabelaRelatorioDTO> tabelas = new ArrayList<>();
                tabelas.add(new TabelaRelatorioDTO(
                                "Indicadores gerais do estoque",
                                List.of("Indicador", "Valor"),
                                List.of(
                                                List.of("Produtos cadastrados",
                                                                String.valueOf(valorOuZero(totalProdutos))),
                                                List.of("Produtos ativos", String.valueOf(valorOuZero(produtosAtivos))),
                                                List.of("Produtos inativos",
                                                                String.valueOf(valorOuZero(produtosInativos))),
                                                List.of("Itens em estoque (soma dos lotes)",
                                                                String.valueOf(valorOuZero(estoqueTotal))),
                                                List.of("Entradas registradas", String.valueOf(valorOuZero(totalEntradas))),
                                                List.of("Itens de entrada", String.valueOf(valorOuZero(quantidadeEntradas))),
                                                List.of("Saídas registradas", String.valueOf(valorOuZero(totalSaidas))),
                                                List.of("Itens de saída", String.valueOf(valorOuZero(quantidadeSaidas))),
                                                List.of("Perdas registradas", String.valueOf(valorOuZero(totalPerdas))),
                                                List.of("Itens perdidos", String.valueOf(valorOuZero(quantidadePerdas))),
                                                List.of("Lotes vencidos com saldo",
                                                                String.valueOf(valorOuZero(lotesVencidos))),
                                                List.of("Lotes com vencimento em até 7 dias",
                                                                String.valueOf(valorOuZero(lotesProximosVencimento))))));
                tabelas.add(criarTabelaTopProdutos("Top produtos por entradas", topEntradas));
                tabelas.add(criarTabelaTopProdutos("Top produtos por saídas", topSaidas));

                return new DadosRelatorio(dados.toString(), tabelas);
        }

        private DadosRelatorio montarDadosProdutosCadastrados() {
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

                List<List<String>> linhasProdutos = new ArrayList<>();
                for (Produto produto : produtos.stream()
                                .limit(MAX_LINHAS_TABELA_DETERMINISTICA)
                                .toList()) {
                        linhasProdutos.add(List.of(
                                        produto.getId() == null ? "-" : String.valueOf(produto.getId()),
                                        valorTexto(produto.getNome()),
                                        valorTexto(produto.getCategoria() == null ? null
                                                        : produto.getCategoria().getNome()),
                                        valorTexto(produto.getUnidadeMedida()),
                                        valorTexto(produto.getStatus())));
                }

                if (linhasProdutos.isEmpty()) {
                        linhasProdutos.add(List.of("-", "Nenhum produto cadastrado", "-", "-", "-"));
                }

                List<TabelaRelatorioDTO> tabelas = new ArrayList<>();
                tabelas.add(new TabelaRelatorioDTO(
                                "Totais de cadastros",
                                List.of("Indicador", "Valor"),
                                List.of(
                                                List.of("Produtos cadastrados",
                                                                String.valueOf(valorOuZero(totalProdutos))),
                                                List.of("Produtos ativos", String.valueOf(valorOuZero(produtosAtivos))),
                                                List.of("Produtos inativos",
                                                                String.valueOf(valorOuZero(produtosInativos))))));
                tabelas.add(new TabelaRelatorioDTO(
                                "Últimos produtos cadastrados",
                                List.of("ID", "Nome", "Categoria", "Unidade", "Status"),
                                linhasProdutos));

                return new DadosRelatorio(dados.toString(), tabelas);
        }

        private TabelaRelatorioDTO criarTabelaUltimasMovimentacoes(String titulo, List<Movimentacao> movimentacoes) {
                List<List<String>> linhas = new ArrayList<>();

                for (Movimentacao movimentacao : movimentacoes.stream()
                                .limit(MAX_LINHAS_TABELA_DETERMINISTICA)
                                .toList()) {
                        linhas.add(List.of(
                                        movimentacao.getData() == null ? "-"
                                                        : movimentacao.getData().format(FORMATO_DATA),
                                        valorTexto(movimentacao.getProduto() == null ? null
                                                        : movimentacao.getProduto().getNome()),
                                        movimentacao.getQuantidade() == null ? "-"
                                                        : String.valueOf(movimentacao.getQuantidade()),
                                        valorTexto(movimentacao.getUsuario() == null ? null
                                                        : movimentacao.getUsuario().getNome()),
                                        valorTexto(movimentacao.getMotivo())));
                }

                if (linhas.isEmpty()) {
                        linhas.add(List.of("-", "Nenhuma movimentação registrada", "-", "-", "-"));
                }

                return new TabelaRelatorioDTO(
                                titulo,
                                List.of("Data", "Produto", "Quantidade", "Responsável", "Motivo"),
                                linhas);
        }

        private TabelaRelatorioDTO criarTabelaTopProdutos(String titulo, List<Object[]> topProdutos) {
                List<List<String>> linhas = new ArrayList<>();

                for (Object[] item : topProdutos) {
                        linhas.add(List.of(
                                        item[0] == null ? "-" : String.valueOf(item[0]),
                                        item[1] == null ? "-" : String.valueOf(item[1])));
                }

                if (linhas.isEmpty()) {
                        linhas.add(List.of("-", "Sem dados suficientes para ranking"));
                }

                return new TabelaRelatorioDTO(
                                titulo,
                                List.of("Produto", "Quantidade movimentada"),
                                linhas);
        }

        private String concluirRelatorioTexto(String tipoRelatorio, DadosRelatorio dados, String pergunta) {
                String perguntaFinal = resolverPergunta(pergunta);
                ResultadoIa resultado = chamarIaEstruturado(tipoRelatorio, dados, perguntaFinal);

                return switch (resultado.origem()) {
                        case COTA_EXCEDIDA -> assistenteFallbackService.gerarRelatorioFallback(
                                        tipoRelatorio, perguntaFinal, dados.texto(), resultado.excecao());
                        case DETERMINISTICO -> assistenteFallbackService.gerarRelatorioDeterministico(
                                        tipoRelatorio, dados.texto());
                        case IA -> formatarTextoRelatorio(resultado.conteudo());
                };
        }

        private RelatorioAssistenteDTO concluirRelatorioPdf(
                        String tituloDto,
                        String tipoDto,
                        String tipoIa,
                        DadosRelatorio dados,
                        String pergunta) {
                String perguntaFinal = resolverPergunta(pergunta);
                ResultadoIa resultado = chamarIaEstruturado(tipoIa, dados, perguntaFinal);

                RelatorioIaResponseDTO conteudo = resultado.conteudo();
                if (resultado.origem() == OrigemResultado.COTA_EXCEDIDA) {
                        conteudo = assistenteFallbackService.gerarRelatorioContingenciaEstruturado(
                                        tipoIa, perguntaFinal, dados.texto());
                }

                List<TabelaRelatorioDTO> tabelasFinais = resultado.origem() == OrigemResultado.IA
                                && conteudo.tabelas() != null
                                && !conteudo.tabelas().isEmpty()
                                                ? conteudo.tabelas()
                                                : dados.tabelas();

                String prioridade = (conteudo.prioridade() == null || conteudo.prioridade().isBlank())
                                ? "media"
                                : conteudo.prioridade();

                return new RelatorioAssistenteDTO(
                                conteudo.resumoExecutivo(),
                                tipoDto,
                                prioridade,
                                conteudo.analise(),
                                conteudo.acoesRecomendadas(),
                                tabelasFinais);
        }

        private ResultadoIa chamarIaEstruturado(
                        String tipoRelatorio,
                        DadosRelatorio dadosRelatorio,
                        String perguntaFinal) {
                String msg = """
                                Você é um assistente logístico especializado em gerenciamento de alimentos e produtos de higiene.
                                Gere um relatório sobre %s com base apenas nos dados abaixo.
                                Seja objetivo, inclua insights e recomendações práticas de ação.
                                Retorne EXCLUSIVAMENTE um objeto JSON válido, sem markdown, sem crases e sem nenhum texto fora do JSON.
                                Formato obrigatório do JSON:
                                {
                                  "resumo_executivo": "uma frase objetiva sobre o cenário",
                                  "prioridade": "baixa|media|alta|critica",
                                  "analise": ["insight 1", "insight 2", "insight 3"],
                                  "acoes_recomendadas": ["ação prática 1", "ação prática 2"],
                                  "tabelas": [
                                    {"titulo": "Titulo da tabela", "colunas": ["Coluna 1", "Coluna 2"], "linhas": [["valor da linha 1 coluna 1", "valor da linha 1 coluna 2"]]}
                                  ]
                                }
                                Regras obrigatórias do conteúdo:
                                - Use somente informações existentes nos dados do banco enviados.
                                - Não invente processos, contextos ou entidades externas.
                                - Não mencione doadores, campanhas, FIFO, sustentabilidade ou estratégias externas se isso não estiver nos dados.
                                - A análise deve citar explicitamente números presentes nos dados.
                                - Gere de 1 a 3 tabelas curtas resumindo visualmente os dados mais relevantes do relatório.
                                - Cada tabela deve ter no máximo 10 linhas e 4 colunas, e todos os valores devem ser textos simples vindos dos dados.

                                Pergunta do usuário:
                                %s

                                Dados do banco:
                                %s
                                """
                                .formatted(tipoRelatorio, perguntaFinal, dadosRelatorio.texto());

                try {
                        String respostaIa = chatModel.call(new Prompt(msg)).getResult().getOutput().getText();

                        RelatorioIaResponseDTO respostaBruta = assistenteIaJsonParserService.parse(respostaIa);

                        if (respostaBruta == null
                                        || !assistenteIaResponseValidatorService.isRespostaAderenteAosDados(
                                                        respostaBruta, dadosRelatorio.texto())) {
                                return new ResultadoIa(
                                                assistenteFallbackService.gerarRelatorioDeterministicoEstruturado(
                                                                tipoRelatorio, dadosRelatorio.texto()),
                                                OrigemResultado.DETERMINISTICO,
                                                null);
                        }

                        return new ResultadoIa(
                                        normalizarResposta(respostaBruta),
                                        OrigemResultado.IA,
                                        null);
                } catch (RuntimeException ex) {
                        if (isQuotaExceeded(ex)) {
                                return new ResultadoIa(null, OrigemResultado.COTA_EXCEDIDA, ex);
                        }
                        throw new ErroAoChamarIaException("Erro ao chamar serviço de IA para gerar relatório.");
                }
        }

        private RelatorioIaResponseDTO normalizarResposta(RelatorioIaResponseDTO bruta) {
                List<String> analise = limitarLista(normalizarLista(bruta.analise()));
                List<String> acoes = limitarLista(normalizarLista(bruta.acoesRecomendadas()));

                return new RelatorioIaResponseDTO(
                                assistenteIaResponseValidatorService.normalizarSaidaIa(bruta.resumoExecutivo()),
                                normalizarPrioridade(bruta.prioridade()),
                                analise.isEmpty() ? List.of("Sem análise disponível para este relatório.") : analise,
                                acoes.isEmpty() ? List.of("Sem ações recomendadas para este relatório.") : acoes,
                                sanitizarTabelas(bruta.tabelas()));
        }

        private List<String> normalizarLista(List<String> itens) {
                if (itens == null) {
                        return Collections.emptyList();
                }

                return itens.stream()
                                .map(item -> assistenteIaResponseValidatorService.normalizarSaidaIa(item))
                                .filter(item -> !item.isBlank())
                                .toList();
        }

        private List<String> limitarLista(List<String> itens) {
                return itens.stream().limit(MAX_ITENS_LISTA_IA).toList();
        }

        private List<TabelaRelatorioDTO> sanitizarTabelas(List<TabelaRelatorioDTO> tabelasBrutas) {
                if (tabelasBrutas == null || tabelasBrutas.isEmpty()) {
                        return Collections.emptyList();
                }

                List<TabelaRelatorioDTO> tabelasValidas = new ArrayList<>();
                for (TabelaRelatorioDTO tabelaBruta : tabelasBrutas) {
                        TabelaRelatorioDTO sanitizada = sanitizarTabela(tabelaBruta);
                        if (assistenteIaResponseValidatorService.isTabelaValida(sanitizada)) {
                                tabelasValidas.add(sanitizada);
                        }
                        if (tabelasValidas.size() == MAX_TABELAS_IA) {
                                break;
                        }
                }
                return tabelasValidas;
        }

        private TabelaRelatorioDTO sanitizarTabela(TabelaRelatorioDTO tabela) {
                if (tabela == null) {
                        return null;
                }

                String titulo = abreviar(
                                assistenteIaResponseValidatorService.normalizarSaidaIa(tabela.titulo()), 80);

                List<String> colunas = tabela.colunas() == null ? Collections.emptyList()
                                : tabela.colunas().stream()
                                                .map(coluna -> abreviar(
                                                                assistenteIaResponseValidatorService.normalizarSaidaIa(
                                                                                coluna), 40))
                                                .toList();

                List<List<String>> linhas = tabela.linhas() == null ? Collections.emptyList()
                                : tabela.linhas().stream()
                                                .map(linha -> linha == null ? Collections.<String>emptyList()
                                                                : linha.stream()
                                                                                .map(celula -> abreviar(celula == null
                                                                                                ? ""
                                                                                                : celula.trim(),
                                                                                                MAX_CARACTERES_CELULA))
                                                                                .toList())
                                                .toList();

                return new TabelaRelatorioDTO(titulo, colunas, linhas);
        }

        private String formatarTextoRelatorio(RelatorioIaResponseDTO conteudo) {
                return "Resumo executivo: " + conteudo.resumoExecutivo() + "\n"
                                + "Prioridade: " + conteudo.prioridade() + "\n"
                                + "Análise: " + String.join(" ", conteudo.analise()) + "\n"
                                + "Ações recomendadas: " + String.join(" ", conteudo.acoesRecomendadas());
        }

        private String normalizarPrioridade(String prioridade) {
                if (prioridade == null) {
                        return "";
                }

                String valor = removerAcentos(prioridade).toLowerCase().trim();
                String[] partes = valor.split("[\\s,;]+");
                return partes.length == 0 ? valor : partes[0];
        }

        private String removerAcentos(String texto) {
                if (texto == null) {
                    return "";
                }
                return Normalizer.normalize(texto, Normalizer.Form.NFD)
                                .replaceAll("\\p{M}", "");
        }

        private String resolverPergunta(String pergunta) {
                return (pergunta == null || pergunta.isBlank())
                                ? "Analise os dados e destaque os principais pontos de atenção e ações recomendadas."
                                : pergunta;
        }

        private String abreviar(String texto, int tamanhoMaximo) {
                if (texto == null) {
                        return "";
                }
                String limpo = texto.trim();
                return limpo.length() <= tamanhoMaximo ? limpo
                                : limpo.substring(0, tamanhoMaximo - 3) + "...";
        }

        private String valorTexto(Object valor) {
                return valor == null || valor.toString().isBlank() ? "-"
                                : abreviar(valor.toString(), MAX_CARACTERES_CELULA);
        }

        private long valorOuZero(Number valor) {
                return valor == null ? 0L : valor.longValue();
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
