package school.sptech.projeto_amar.event;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;

import java.awt.*;

public class PdfRelatorioPageEvent extends PdfPageEventHelper {

        private final Image logo;
        private static final Color COR_PRIMARIA = new Color(18, 60, 108);
        private static final Color COR_DESTAQUE = new Color(33, 92, 161);

        public PdfRelatorioPageEvent(Image logo) {
                this.logo = logo;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

                PdfContentByte canvas = writer.getDirectContent();

                try {

                        // CABEÇALHO PRINCIPAL
                        canvas.saveState();
                        canvas.setColorFill(COR_PRIMARIA);

                        canvas.rectangle(
                                        document.leftMargin(),
                                        document.top() + 28,
                                        document.getPageSize().getWidth()
                                                        - document.leftMargin()
                                                        - document.rightMargin(),
                                        46);
                        canvas.fill();

                        // FAIXA DE DESTAQUE
                        canvas.setColorFill(COR_DESTAQUE);
                        canvas.rectangle(
                                        document.leftMargin(),
                                        document.top() + 24,
                                        document.getPageSize().getWidth()
                                                        - document.leftMargin()
                                                        - document.rightMargin(),
                                        4);
                        canvas.fill();

                        canvas.restoreState();

                        // LOGO

                        if (logo != null) {

                                logo.scaleToFit(38, 38);

                                logo.setAbsolutePosition(
                                                document.leftMargin() + 10,
                                                document.top() + 31);

                                canvas.addImage(logo);
                        }

                        // TÍTULO CABEÇALHO

                        ColumnText.showTextAligned(
                                        canvas,
                                        Element.ALIGN_CENTER,
                                        new Phrase(
                                                        "AMAR - Sistema Inteligente de Gestão de Doações",
                                                        new Font(
                                                                        Font.HELVETICA,
                                                                        14,
                                                                        Font.BOLD,
                                                                        Color.WHITE)),
                                        document.getPageSize().getWidth() / 2,
                                        document.top() + 47,
                                        0);

                        // MARCA D'ÁGUA

                        PdfGState state = new PdfGState();
                        state.setFillOpacity(0.05f);

                        canvas.saveState();
                        canvas.setGState(state);

                        ColumnText.showTextAligned(
                                        canvas,
                                        Element.ALIGN_CENTER,
                                        new Phrase(
                                                        "PROJETO AMAR",
                                                        new Font(
                                                                        Font.HELVETICA,
                                                                        46,
                                                                        Font.BOLD,
                                                                        new Color(185, 192, 203))),
                                        document.getPageSize().getWidth() / 2,
                                        document.getPageSize().getHeight() / 2,
                                        45);

                        canvas.restoreState();

                        // NUMERAÇÃO

                        canvas.saveState();
                        canvas.setColorStroke(new Color(214, 223, 234));
                        canvas.moveTo(document.leftMargin(), 36);
                        canvas.lineTo(
                                        document.getPageSize().getWidth() - document.rightMargin(),
                                        36);
                        canvas.stroke();
                        canvas.restoreState();

                        ColumnText.showTextAligned(
                                        canvas,
                                        Element.ALIGN_CENTER,
                                        new Phrase(
                                                        "Página " + writer.getPageNumber(),
                                                        new Font(Font.HELVETICA, 9, Font.NORMAL,
                                                                        new Color(115, 128, 143))),
                                        document.getPageSize().getWidth() / 2,
                                        22,
                                        0);

                } catch (Exception ignored) {
                }
        }
}