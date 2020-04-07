package com.prigic.pdfmanager.util;

import android.content.Context;

import java.util.ArrayList;

import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.model.EnhancementOptionsEntityModel;

public class MergePdfEnhancementOptionsUtils {
    public static ArrayList<EnhancementOptionsEntityModel> getEnhancementOptions(Context context) {
        ArrayList<EnhancementOptionsEntityModel> options = new ArrayList<>();

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.baseline_enhanced_encryption_24, R.string.set_password));
        return options;
    }
}