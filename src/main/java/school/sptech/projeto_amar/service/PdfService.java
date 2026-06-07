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
import java.util.Collections;
import java.util.List;

@Service
public class PdfService {

        private static final Color COR_PRIMARIA = new Color(18, 60, 108);
        private static final Color COR_TEXTO = new Color(45, 52, 64);
        private static final Color COR_MUTED = new Color(112, 125, 140);
        private static final Color COR_CARD = new Color(245, 248, 252);
        private static final Color COR_ALERTA = new Color(255, 242, 223);

        private static final Font TITULO_PRINCIPAL = new Font(Font.HELVETICA, 19, Font.BOLD, COR_PRIMARIA);
        private static final Font SUBTITULO = new Font(Font.HELVETICA, 10, Font.NORMAL, COR_MUTED);
        private static final Font TITULO_SECAO = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        private static final Font TEXTO = new Font(Font.HELVETICA, 10, Font.NORMAL, COR_TEXTO);
        private static final Font TEXTO_DESTAQUE = new Font(Font.HELVETICA, 10, Font.BOLD, COR_PRIMARIA);
        private static final Font TEXTO_SMALL = new Font(Font.HELVETICA, 9, Font.NORMAL, COR_MUTED);

        public byte[] gerarPdf(RelatorioAssistenteDTO relatorio) {

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                        Document document = new Document(
                                        PageSize.A4,
                                        42,
                                        42,
                                        95,
                                        48);

                        PdfWriter writer = PdfWriter.getInstance(
                                        document,
                                        outputStream);

                        Image logo = carregarLogo();

                        writer.setPageEvent(
                                        new school.sptech.projeto_amar.event.PdfRelatorioPageEvent(logo));

                        document.open();

                        adicionarCabecalho(document, relatorio);
                        adicionarCardsResumo(document, relatorio);
                        adicionarSeparador(document);

                        boolean modoContingencia = isModoContingencia(relatorio);
                        if (modoContingencia) {
                                adicionarAvisoContingencia(document);
                        }

                        adicionarSecaoTitulo(document, "Resumo Executivo");
                        adicionarParagrafoComCaixa(
                                        document,
                                        valorPadrao(relatorio.titulo(), "Relatório sem resumo disponível."));

                        adicionarSecaoTitulo(document, modoContingencia ? "Análise Operacional" : "Análise da IA");
                        adicionarLista(
                                        document,
                                        valorPadraoLista(relatorio.response()),
                                        "Nenhum insight disponível para este relatório.",
                                        "• ");

                        document.add(new Paragraph(" "));
                        adicionarSecaoTitulo(document, "Ações Recomendadas");
                        adicionarLista(
                                        document,
                                        valorPadraoLista(relatorio.acoes_recomendadas()),
                                        "Nenhuma ação recomendada disponível.",
                                        "✓ ");

                        document.add(new Paragraph(" "));
                        adicionarSeparador(document);
                        adicionarRodapeConteudo(document);

                        document.close();

                        return outputStream.toByteArray();

                } catch (Exception e) {

                        throw new RuntimeException(
                                        "Erro ao gerar PDF",
                                        e);
                }
        }

        private void adicionarCabecalho(Document document, RelatorioAssistenteDTO relatorio) throws DocumentException {
                String titulo = valorPadrao(relatorio.titulo(), "Relatório Operacional de Estoque");

                Paragraph tituloPrincipal = new Paragraph(titulo, TITULO_PRINCIPAL);
                tituloPrincipal.setAlignment(Element.ALIGN_LEFT);
                tituloPrincipal.setSpacingAfter(4f);
                document.add(tituloPrincipal);

                Paragraph subtitulo = new Paragraph(
                                "Documento analítico para apoio à tomada de decisão da equipe de estoque",
                                SUBTITULO);
                subtitulo.setAlignment(Element.ALIGN_LEFT);
                subtitulo.setSpacingAfter(14f);
                document.add(subtitulo);
        }

        private void adicionarCardsResumo(Document document, RelatorioAssistenteDTO relatorio)
                        throws DocumentException {
                PdfPTable tabela = new PdfPTable(3);
                tabela.setWidthPercentage(100);
                tabela.setWidths(new float[] { 1.2f, 1.0f, 1.0f });
                tabela.setSpacingAfter(8f);

                String tipo = valorPadrao(relatorio.tipo(), "indefinido");
                String prioridade = valorPadrao(relatorio.prioridade(), "media");
                String dataGeracao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                tabela.addCell(criarCardInfo("Tipo do Relatório", tipo));
                tabela.addCell(criarCardInfo("Prioridade", prioridade.toUpperCase()));
                tabela.addCell(criarCardInfo("Gerado em", dataGeracao));

                document.add(tabela);

                PdfPTable badgeTable = new PdfPTable(1);
                badgeTable.setWidthPercentage(30);
                badgeTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                badgeTable.setSpacingAfter(10f);

                PdfPCell badge = new PdfPCell(new Phrase(
                                "Prioridade " + prioridade.toUpperCase(),
                                new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
                badge.setPadding(8f);
                badge.setHorizontalAlignment(Element.ALIGN_CENTER);
                badge.setBorder(Rectangle.NO_BORDER);
                badge.setBackgroundColor(corPrioridade(prioridade));
                badgeTable.addCell(badge);

                document.add(badgeTable);
        }

        private PdfPCell criarCardInfo(String rotulo, String valor) {
                PdfPCell cell = new PdfPCell();
                cell.setPaddingTop(10f);
                cell.setPaddingBottom(10f);
                cell.setPaddingLeft(10f);
                cell.setPaddingRight(10f);
                cell.setBorderColor(new Color(222, 229, 238));
                cell.setBackgroundColor(COR_CARD);

                Paragraph pRotulo = new Paragraph(rotulo, TEXTO_SMALL);
                pRotulo.setSpacingAfter(4f);

                Paragraph pValor = new Paragraph(valor, TEXTO_DESTAQUE);
                pValor.setLeading(13f);

                cell.addElement(pRotulo);
                cell.addElement(pValor);
                return cell;
        }

        private void adicionarSecaoTitulo(Document document, String titulo) throws DocumentException {
                PdfPTable section = new PdfPTable(1);
                section.setWidthPercentage(100);
                section.setSpacingBefore(4f);
                section.setSpacingAfter(8f);

                PdfPCell cell = new PdfPCell(new Phrase(titulo.toUpperCase(), TITULO_SECAO));
                cell.setPadding(8f);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setBackgroundColor(COR_PRIMARIA);
                section.addCell(cell);

                document.add(section);
        }

        private void adicionarParagrafoComCaixa(Document document, String texto) throws DocumentException {
                PdfPTable box = new PdfPTable(1);
                box.setWidthPercentage(100);
                box.setSpacingAfter(8f);

                PdfPCell cell = new PdfPCell(new Phrase(texto, TEXTO));
                cell.setPadding(11f);
                cell.setLeading(14f, 0f);
                cell.setBorderColor(new Color(222, 229, 238));
                cell.setBackgroundColor(Color.WHITE);
                box.addCell(cell);

                document.add(box);
        }

        private void adicionarLista(
                        Document document,
                        List<String> itens,
                        String mensagemVazia,
                        String marcador) throws DocumentException {
                PdfPTable box = new PdfPTable(1);
                box.setWidthPercentage(100);
                box.setSpacingAfter(6f);

                PdfPCell cell = new PdfPCell();
                cell.setPadding(10f);
                cell.setBorderColor(new Color(222, 229, 238));
                cell.setBackgroundColor(Color.WHITE);

                if (itens.isEmpty()) {
                        cell.addElement(new Paragraph(marcador + mensagemVazia, TEXTO));
                } else {
                        for (String item : itens) {
                                Paragraph linha = new Paragraph(marcador + item, TEXTO);
                                linha.setLeading(14f);
                                linha.setSpacingAfter(3f);
                                cell.addElement(linha);
                        }
                }

                box.addCell(cell);
                document.add(box);
        }

        private void adicionarAvisoContingencia(Document document) throws DocumentException {
                PdfPTable aviso = new PdfPTable(1);
                aviso.setWidthPercentage(100);
                aviso.setSpacingBefore(2f);
                aviso.setSpacingAfter(8f);

                PdfPCell cell = new PdfPCell(new Phrase(
                                "Aviso: este relatório foi gerado em modo contingência, sem análise de IA devido a limite temporário de cota.",
                                new Font(Font.HELVETICA, 10, Font.BOLD, new Color(130, 78, 0))));
                cell.setPadding(9f);
                cell.setBorderColor(new Color(255, 205, 135));
                cell.setBackgroundColor(COR_ALERTA);
                aviso.addCell(cell);

                document.add(aviso);
        }

        private void adicionarSeparador(Document document) throws DocumentException {
                document.add(new Paragraph(" "));
                LineSeparator lineSeparator = new LineSeparator();
                lineSeparator.setLineColor(new Color(208, 216, 226));
                lineSeparator.setPercentage(100f);
                document.add(lineSeparator);
                document.add(new Paragraph(" "));
        }

        private void adicionarRodapeConteudo(Document document) throws DocumentException {
                Paragraph rodape = new Paragraph(
                                "Relatório gerado automaticamente pelo Sistema AMAR",
                                new Font(Font.HELVETICA, 9, Font.ITALIC, COR_MUTED));
                rodape.setAlignment(Element.ALIGN_CENTER);
                rodape.setSpacingBefore(2f);
                document.add(rodape);
        }

        private String valorPadrao(String valor, String defaultValue) {
                return (valor == null || valor.isBlank()) ? defaultValue : valor;
        }

        private List<String> valorPadraoLista(List<String> lista) {
                return lista == null ? Collections.emptyList() : lista;
        }

        private boolean isModoContingencia(RelatorioAssistenteDTO relatorio) {
                List<String> respostas = valorPadraoLista(relatorio.response());
                for (String linha : respostas) {
                        if (linha != null && linha.toLowerCase().contains("não foi gerado por ia")) {
                                return true;
                        }
                }
                return false;
        }

        private Color corPrioridade(String prioridade) {
                String valor = prioridade == null ? "" : prioridade.toLowerCase();
                switch (valor) {
                        case "baixa":
                                return new Color(44, 142, 93);
                        case "media":
                                return new Color(212, 145, 33);
                        case "critica":
                                return new Color(145, 32, 52);
                        default:
                                return new Color(39, 95, 165);
                }
        }

        private Image carregarLogo() {
                try {
                        return Image.getInstance(getClass().getResource("/static/logo-amar.png"));
                } catch (Exception ignored) {
                        return null;
                }
        }
}