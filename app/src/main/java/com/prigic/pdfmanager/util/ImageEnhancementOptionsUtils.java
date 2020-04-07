package com.prigic.pdfmanager.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.model.EnhancementOptionsEntityModel;
import com.prigic.pdfmanager.model.ImageToPDFOptionsModel;

public class ImageEnhancementOptionsUtils {

    public static ArrayList<EnhancementOptionsEntityModel> getEnhancementOptions(Context context,
                                                                                 ImageToPDFOptionsModel pdfOptions) {
        ArrayList<EnhancementOptionsEntityModel> options = new ArrayList<>();
        int passwordicon = R.drawable.baseline_enhanced_encryption_24;
        if (pdfOptions.isPasswordProtected())
            passwordicon = R.drawable.baseline_done_24;

        options.add(new EnhancementOptionsEntityModel(
                context, passwordicon, R.string.password_protect_pdf_text));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.baseline_crop_rotate_24, R.string.edit_images_text));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_compress_image,
                String.format(context.getResources().getString(R.string.compress_image),
                        pdfOptions.getQualityString())));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_photo_filter_black_24dp, R.string.filter_images_Text));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_page_size_24dp, R.string.set_page_size_text));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_aspect_ratio_black_24dp, R.string.image_scale_type));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_play_circle_outline_black_24dp, R.string.preview_image_to_pdf));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_border_image_black_24dp,
                String.format(context.getResources().getString(R.string.border_dialog_title),
                        pdfOptions.getBorderWidth())));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_rearrange, R.string.rearrange_images));

        Drawable iconGrayscale = context.getResources().getDrawable(R.drawable.ic_photo_filter_black_24dp);
        iconGrayscale.setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN);

        options.add(new EnhancementOptionsEntityModel(
                iconGrayscale,
                context.getResources().getString(R.string.grayscale_images)));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_page_size_24dp, R.string.add_margins));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_format_list_numbered_black_24dp, R.string.show_pg_num));

        options.add(new EnhancementOptionsEntityModel(
                context, R.drawable.ic_branding_watermark_black_24dp, R.string.add_watermark));

        return options;
    }
}
