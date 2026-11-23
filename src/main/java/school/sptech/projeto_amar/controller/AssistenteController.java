package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.sptech.projeto_amar.dto.assistente.PerguntaRequestDTO;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;
import school.sptech.projeto_amar.service.AssistenteService;
import school.sptech.projeto_amar.service.PdfService;

@RestController
@RequestMapping("/assistente")
@Tag(name = "Assistente", description = "Endpoints para geração de relatórios com apoio de IA")
public class AssistenteController {
    private final AssistenteService assistenteService;
    private final PdfService pdfService;

    public AssistenteController(AssistenteService assistenteService, PdfService pdfService) {
        this.assistenteService = assistenteService;
        this.pdfService = pdfService;
    }

    @PostMapping("/relatorio/entradas")
    @Operation(summary = "Gerar relatório de entradas", description = "Gera um relatório textual do histórico de entradas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"), @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public String gerarRelatorioHistoricoEntradas(@RequestBody PerguntaRequestDTO body) {
        return assistenteService.gerarRelatorioHistoricoEntradas(
                body.pergunta(), body.dataInicio(), body.dataFim());
    }

    @PostMapping("/relatorio/saidas")
    @Operation(summary = "Gerar relatório de saídas", description = "Gera um relatório textual do histórico de saídas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"), @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public String gerarRelatorioHistoricoSaidas(@RequestBody PerguntaRequestDTO body) {
        return assistenteService.gerarRelatorioHistoricoSaidas(
                body.pergunta(), body.dataInicio(), body.dataFim());
    }

    @PostMapping("/relatorio/desempenho-geral")
    @Operation(summary = "Gerar relatório de desempenho geral", description = "Gera um relatório textual com o desempenho geral do estoque")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"), @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public String gerarRelatorioDesempenhoGeral(@RequestBody PerguntaRequestDTO body) {
        return assistenteService.gerarRelatorioDesempenhoGeral(
                body.pergunta(), body.dataInicio(), body.dataFim());
    }

    @PostMapping("/relatorio/produtos-cadastrados")
    @Operation(summary = "Gerar relatório de produtos cadastrados", description = "Gera um relatório textual sobre produtos cadastrados")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"), @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public String gerarRelatorioHistoricoProdutosCadastrados(@RequestBody PerguntaRequestDTO body) {
        return assistenteService.gerarRelatorioHistoricoProdutosCadastrados(
                body.pergunta(), body.dataInicio(), body.dataFim());
    }

    @PostMapping(value = "/relatorio/entradas/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Baixar relatório de entradas em PDF", description = "Gera e retorna em PDF o relatório do histórico de entradas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "PDF gerado com sucesso", content = @Content(mediaType = "application/pdf")), @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public ResponseEntity<byte[]> baixarRelatorioHistoricoEntradasPdf(@RequestBody PerguntaRequestDTO body) {
        RelatorioAssistenteDTO relatorio = assistenteService.gerarRelatorioHistoricoEntradasParaPdf(
                body.pergunta(), body.dataInicio(), body.dataFim());
        return gerarRespostaPdf(relatorio, "relatorio-historico-entradas.pdf");
    }

    @PostMapping(value = "/relatorio/saidas/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Baixar relatório de saídas em PDF", description = "Gera e retorna em PDF o relatório do histórico de saídas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "PDF gerado com sucesso", content = @Content(mediaType = "application/pdf")), @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public ResponseEntity<byte[]> baixarRelatorioHistoricoSaidasPdf(@RequestBody PerguntaRequestDTO body) {
        RelatorioAssistenteDTO relatorio = assistenteService.gerarRelatorioHistoricoSaidasParaPdf(
                body.pergunta(), body.dataInicio(), body.dataFim());
        return gerarRespostaPdf(relatorio, "relatorio-historico-saidas.pdf");
    }

    @PostMapping(value = "/relatorio/desempenho-geral/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Baixar relatório de desempenho geral em PDF", description = "Gera e retorna em PDF o relatório de desempenho geral")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "PDF gerado com sucesso", content = @Content(mediaType = "application/pdf")), @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public ResponseEntity<byte[]> baixarRelatorioDesempenhoGeralPdf(@RequestBody PerguntaRequestDTO body) {
        RelatorioAssistenteDTO relatorio = assistenteService.gerarRelatorioDesempenhoGeralParaPdf(
                body.pergunta(), body.dataInicio(), body.dataFim());
        return gerarRespostaPdf(relatorio, "relatorio-desempenho-geral.pdf");
    }

    @PostMapping(value = "/relatorio/produtos-cadastrados/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Baixar relatório de produtos cadastrados em PDF", description = "Gera e retorna em PDF o relatório de produtos cadastrados")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "PDF gerado com sucesso", content = @Content(mediaType = "application/pdf")), @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public ResponseEntity<byte[]> baixarRelatorioProdutosCadastradosPdf(@RequestBody PerguntaRequestDTO body) {
        RelatorioAssistenteDTO relatorio = assistenteService.gerarRelatorioProdutosCadastradosParaPdf(
                body.pergunta(), body.dataInicio(), body.dataFim());
        return gerarRespostaPdf(relatorio, "relatorio-produtos-cadastrados.pdf");
    }

    @PostMapping("/relatorio/entradas/preview")
    @Operation(summary = "Visualizar relatório de entradas", description = "Gera o relatório estruturado de entradas para exibição na tela")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a ADMINISTRADOR")
    })
    public RelatorioAssistenteDTO visualizarRelatorioHistoricoEntradas(@RequestBody PerguntaRequestDTO body) {
        return assistenteService.gerarRelatorioHistoricoEntradasParaPdf(
                body.pergunta(), body.dataInicio(), body.dataFim());
    }

    @PostMapping("/relatorio/saidas/preview")
    @Operation(summary = "Visualizar relatório de saídas", description = "Gera o relatório estruturado de saídas para exibição na tela")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a ADMINISTRADOR")
    })
    public RelatorioAssistenteDTO visualizarRelatorioHistoricoSaidas(@RequestBody PerguntaRequestDTO body) {
        return assistenteService.gerarRelatorioHistoricoSaidasParaPdf(
                body.pergunta(), body.dataInicio(), body.dataFim());
    }

    @PostMapping("/relatorio/desempenho-geral/preview")
    @Operation(summary = "Visualizar relatório de desempenho", description = "Gera o relatório estruturado de desempenho para exibição na tela")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a ADMINISTRADOR")
    })
    public RelatorioAssistenteDTO visualizarRelatorioDesempenhoGeral(@RequestBody PerguntaRequestDTO body) {
        return assistenteService.gerarRelatorioDesempenhoGeralParaPdf(
                body.pergunta(), body.dataInicio(), body.dataFim());
    }

    @PostMapping("/relatorio/produtos-cadastrados/preview")
    @Operation(summary = "Visualizar relatório de produtos", description = "Gera o relatório estruturado de produtos cadastrados para exibição na tela")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a ADMINISTRADOR")
    })
    public RelatorioAssistenteDTO visualizarRelatorioProdutosCadastrados(@RequestBody PerguntaRequestDTO body) {
        return assistenteService.gerarRelatorioProdutosCadastradosParaPdf(
                body.pergunta(), body.dataInicio(), body.dataFim());
    }

    private ResponseEntity<byte[]> gerarRespostaPdf(RelatorioAssistenteDTO relatorio, String nomeArquivo) {
        byte[] pdf = pdfService.gerarPdf(relatorio);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeArquivo).contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}