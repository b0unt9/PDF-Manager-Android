package com.prigic.pdfmanager.model;

import android.net.Uri;

import com.itextpdf.text.Font;

public class TextToPDFOptionsModel extends PDFOptionsModel {
    private final Uri mInFileUri;
    private final int mFontSize;
    private final Font.FontFamily mFontFamily;

    public TextToPDFOptionsModel(String mFileName, String mPageSize, boolean mPasswordProtected,
                                 String mPassword, Uri mInFileUri, int mFontSize, Font.FontFamily mFontFamily) {
        super(mFileName, mPageSize, mPasswordProtected, mPassword, 0, false, null);
        this.mInFileUri = mInFileUri;
        this.mFontSize = mFontSize;
        this.mFontFamily = mFontFamily;
    }

    public Uri getInFileUri() {
        return mInFileUri;
    }

    public int getFontSize() {
        return mFontSize;
    }

    public Font.FontFamily getFontFamily() {
        return mFontFamily;
    }
}
