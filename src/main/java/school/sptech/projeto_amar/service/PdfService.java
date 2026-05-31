package school.sptech.projeto_amar.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] gerarPdf(RelatorioAssistenteDTO relatorio) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // TÍTULO
            document.add(new Paragraph(relatorio.titulo()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Tipo: " + relatorio.tipo()));
            document.add(new Paragraph("Prioridade: " + relatorio.prioridade()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Resumo:"));

            if (relatorio.response() != null) {
                for (String item : relatorio.response()) {
                    document.add(new Paragraph("- " + item));
                }
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Ações recomendadas:"));

            if (relatorio.acoes_recomendadas() != null) {
                for (String acao : relatorio.acoes_recomendadas()) {
                    document.add(new Paragraph("- " + acao));
                }
            }

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }
}