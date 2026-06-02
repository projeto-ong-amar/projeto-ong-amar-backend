package school.sptech.projeto_amar.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.dto.assistente.RelatorioAssistenteDTO;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] gerarPdf(RelatorioAssistenteDTO relatorio) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Document document = new Document(
                    PageSize.A4,
                    40,
                    40,
                    85,
                    40
            );

            PdfWriter writer =
                    PdfWriter.getInstance(
                            document,
                            outputStream
                    );

            Image logo = null;

            try {

                logo = Image.getInstance(
                        getClass()
                                .getResource("/static/logo-amar.png")
                );

            } catch (Exception ignored) {
            }

            writer.setPageEvent(
                    new school.sptech.projeto_amar.service.PdfRelatorioPageEvent(logo)
            );

            document.open();

            Font subtituloFonte =
                    new Font(
                            Font.HELVETICA,
                            13,
                            Font.BOLD
                    );

            Font secaoFonte =
                    new Font(
                            Font.HELVETICA,
                            14,
                            Font.BOLD
                    );

            Font textoFonte =
                    new Font(
                            Font.HELVETICA,
                            11
                    );

            // TÍTULO

            Paragraph relatorioTitulo =
                    new Paragraph(
                            "Relatório Operacional de Estoque",
                            subtituloFonte
                    );

            relatorioTitulo.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(relatorioTitulo);

            document.add(new Paragraph(" "));

            document.add(
                    new Paragraph(
                            "Data de geração: "
                                    + LocalDateTime.now()
                                    .format(
                                            DateTimeFormatter.ofPattern(
                                                    "dd/MM/yyyy HH:mm"
                                            )
                                    ),
                            textoFonte
                    )
            );

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());

            // RESUMO EXECUTIVO

            document.add(new Paragraph(" "));

            document.add(
                    new Paragraph(
                            "RESUMO EXECUTIVO",
                            secaoFonte
                    )
            );

            document.add(
                    new Paragraph(
                            relatorio.titulo(),
                            textoFonte
                    )
            );

            document.add(new Paragraph(" "));

            // TABELA DE INFORMAÇÕES

            PdfPTable tabela = new PdfPTable(2);
            tabela.setWidthPercentage(100);

            PdfPCell header1 =
                    new PdfPCell(
                            new Phrase(
                                    "Campo",
                                    new Font(
                                            Font.HELVETICA,
                                            11,
                                            Font.BOLD
                                    )
                            )
                    );

            header1.setBackgroundColor(
                    new Color(220, 220, 220)
            );

            PdfPCell header2 =
                    new PdfPCell(
                            new Phrase(
                                    "Valor",
                                    new Font(
                                            Font.HELVETICA,
                                            11,
                                            Font.BOLD
                                    )
                            )
                    );

            header2.setBackgroundColor(
                    new Color(220, 220, 220)
            );

            tabela.addCell(header1);
            tabela.addCell(header2);

            tabela.addCell("Tipo");
            tabela.addCell(relatorio.tipo());

            tabela.addCell("Prioridade");
            tabela.addCell(relatorio.prioridade());

            document.add(tabela);

            document.add(new Paragraph(" "));

            // BADGE PRIORIDADE

            Color corPrioridade;

            switch (relatorio.prioridade().toLowerCase()) {

                case "baixa":
                    corPrioridade =
                            new Color(40, 167, 69);
                    break;

                case "media":
                    corPrioridade =
                            new Color(255, 193, 7);
                    break;

                case "critica":
                    corPrioridade =
                            new Color(128, 0, 0);
                    break;

                default:
                    corPrioridade =
                            new Color(220, 53, 69);
            }

            document.add(
                    new Paragraph(
                            "PRIORIDADE",
                            secaoFonte
                    )
            );

            PdfPTable prioridadeTable =
                    new PdfPTable(1);

            prioridadeTable.setWidthPercentage(25);

            PdfPCell badge =
                    new PdfPCell(
                            new Phrase(
                                    relatorio.prioridade().toUpperCase(),
                                    new Font(
                                            Font.HELVETICA,
                                            12,
                                            Font.BOLD,
                                            Color.WHITE
                                    )
                            )
                    );

            badge.setBackgroundColor(
                    corPrioridade
            );

            badge.setBorder(
                    Rectangle.NO_BORDER
            );

            badge.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            prioridadeTable.addCell(badge);

            document.add(prioridadeTable);

            document.add(new Paragraph(" "));

            // ANÁLISE

            document.add(
                    new Paragraph(
                            "ANÁLISE DA IA",
                            secaoFonte
                    )
            );

            if (relatorio.response() != null) {

                for (String item : relatorio.response()) {

                    document.add(
                            new Paragraph(
                                    "• " + item,
                                    textoFonte
                            )
                    );
                }
            }

            document.add(new Paragraph(" "));

            // AÇÕES RECOMENDADAS

            document.add(
                    new Paragraph(
                            "AÇÕES RECOMENDADAS",
                            secaoFonte
                    )
            );

            if (relatorio.acoes_recomendadas() != null) {

                for (String acao : relatorio.acoes_recomendadas()) {

                    document.add(
                            new Paragraph(
                                    "✓ " + acao,
                                    textoFonte
                            )
                    );
                }
            }

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());

            Paragraph rodape =
                    new Paragraph(
                            "Relatório gerado automaticamente pelo Sistema AMAR",
                            new Font(
                                    Font.HELVETICA,
                                    9,
                                    Font.ITALIC
                            )
                    );

            rodape.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(rodape);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erro ao gerar PDF",
                    e
            );
        }
    }
}