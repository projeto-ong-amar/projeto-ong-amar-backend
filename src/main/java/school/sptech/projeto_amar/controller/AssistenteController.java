package school.sptech.projeto_amar.controller;

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
public class AssistenteController {

    private final AssistenteService assistenteService;
    private final PdfService pdfService;

    public AssistenteController(
            AssistenteService assistenteService,
            PdfService pdfService
    ) {
        this.assistenteService = assistenteService;
        this.pdfService = pdfService;
    }

    @PostMapping
    public RelatorioAssistenteDTO chat(
            @RequestBody PerguntaRequestDTO body
    ) {
        return assistenteService.responder(
                body.pergunta()
        );
    }

    @PostMapping("/relatorio")
    public ResponseEntity<byte[]> gerarRelatorio(
            @RequestBody PerguntaRequestDTO body
    ) {

        RelatorioAssistenteDTO relatorio =
                assistenteService.responder(
                        body.pergunta()
                );

        byte[] pdf =
                pdfService.gerarPdf(relatorio);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=relatorio.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}