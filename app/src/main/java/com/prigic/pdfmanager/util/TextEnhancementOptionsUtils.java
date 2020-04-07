package com.prigic.pdfmanager.util;

import android.content.Context;

import com.itextpdf.text.Font;

import java.util.ArrayList;

import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.model.EnhancementOptionsEntityModel;

public class TextEnhancementOptionsUtils {

    public static ArrayList<EnhancementOptionsEntityModel> getEnhancementOptions(Context context,
                                                                                 String fontTitle,
                                                                                 Font.FontFamily fontFamily) {
        ArrayList<EnhancementOptionsEntityModel> options = new ArrayList<>();

        options.add(new EnhancementOptionsEntityModel(
                context.getResources().getDrawable(R.drawable.ic_font_black_24dp),
                fontTitle));
        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_font_family_24dp,
                String.format(context.getString(R.string.default_font_family_text), fontFamily.name())));
        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_page_size_24dp, R.string.set_page_size_text));
        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.baseline_enhanced_encryption_24, R.string.set_password));
        return options;
    }
}
