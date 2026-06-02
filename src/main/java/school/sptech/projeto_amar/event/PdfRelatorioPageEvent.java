package school.sptech.projeto_amar.event;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;

import java.awt.*;

public class PdfRelatorioPageEvent extends PdfPageEventHelper {

    private final Image logo;

    public PdfRelatorioPageEvent(Image logo) {
        this.logo = logo;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        PdfContentByte canvas = writer.getDirectContent();

        try {

            // CABEÇALHO AZUL
            canvas.saveState();

            canvas.setColorFill(new Color(16, 64, 128));

            canvas.rectangle(
                    document.leftMargin(),
                    document.top() + 25,
                    document.getPageSize().getWidth()
                            - document.leftMargin()
                            - document.rightMargin(),
                    55
            );

            canvas.fill();

            canvas.restoreState();

            // LOGO

            if (logo != null) {

                logo.scaleToFit(45, 45);

                logo.setAbsolutePosition(
                        document.leftMargin() + 10,
                        document.top() + 28
                );

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
                                    16,
                                    Font.BOLD,
                                    Color.WHITE
                            )
                    ),
                    document.getPageSize().getWidth() / 2,
                    document.top() + 55,
                    0
            );

            // MARCA D'ÁGUA

            PdfGState state = new PdfGState();
            state.setFillOpacity(0.08f);

            canvas.saveState();
            canvas.setGState(state);

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_CENTER,
                    new Phrase(
                            "PROJETO AMAR",
                            new Font(
                                    Font.HELVETICA,
                                    50,
                                    Font.BOLD,
                                    new Color(180,180,180)
                            )
                    ),
                    document.getPageSize().getWidth() / 2,
                    document.getPageSize().getHeight() / 2,
                    45
            );

            canvas.restoreState();

            // NUMERAÇÃO

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_CENTER,
                    new Phrase(
                            "Página " + writer.getPageNumber(),
                            new Font(Font.HELVETICA, 9)
                    ),
                    document.getPageSize().getWidth() / 2,
                    25,
                    0
            );

        } catch (Exception ignored) {
        }
    }
}