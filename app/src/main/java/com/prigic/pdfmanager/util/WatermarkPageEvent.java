package com.prigic.pdfmanager.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import com.prigic.pdfmanager.model.WatermarkModel;

public class WatermarkPageEvent extends PdfPageEventHelper {
    private WatermarkModel mWatermarkModel;
    private Phrase mPhrase;

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContent();
        float x = (document.getPageSize().getLeft() + document.getPageSize().getRight()) / 2;
        float y = (document.getPageSize().getTop() + document.getPageSize().getBottom()) / 2;
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, mPhrase, x, y, mWatermarkModel.getRotationAngle());
    }

    public WatermarkModel getWatermark() {
        return mWatermarkModel;
    }

    public void setWatermark(WatermarkModel watermarkModel) {
        this.mWatermarkModel = watermarkModel;
        this.mPhrase = new Phrase(mWatermarkModel.getWatermarkText(),
                new Font(mWatermarkModel.getFontFamily(), mWatermarkModel.getTextSize(),
                        mWatermarkModel.getFontStyle(), mWatermarkModel.getTextColor()));
    }
}