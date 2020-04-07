package com.prigic.pdfmanager.util;

import android.content.Context;

import java.util.ArrayList;

import ja.burhanrashid52.photoeditor.PhotoFilter;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.model.FilterItemModel;

public class ImageFilterUtils {

    public static ArrayList<FilterItemModel> getFiltersList(Context context) {

        ArrayList<FilterItemModel> items = new ArrayList<>();

        items.add(new FilterItemModel(R.drawable.none,
                context.getString(R.string.filter_none), PhotoFilter.NONE));
        items.add(new FilterItemModel(R.drawable.none,
                context.getString(R.string.filter_brush), PhotoFilter.NONE));
        items.add(new FilterItemModel(R.drawable.auto_fix,
                context.getString(R.string.filter_autofix), PhotoFilter.AUTO_FIX));
        items.add(new FilterItemModel(R.drawable.black,
                context.getString(R.string.filter_grayscale), PhotoFilter.GRAY_SCALE));
        items.add(new FilterItemModel(R.drawable.brightness,
                context.getString(R.string.filter_brightness), PhotoFilter.BRIGHTNESS));
        items.add(new FilterItemModel(R.drawable.contrast,
                context.getString(R.string.filter_contrast), PhotoFilter.CONTRAST));
        items.add(new FilterItemModel(R.drawable.cross_process,
                context.getString(R.string.filter_cross), PhotoFilter.CROSS_PROCESS));
        items.add(new FilterItemModel(R.drawable.documentary,
                context.getString(R.string.filter_documentary), PhotoFilter.DOCUMENTARY));
        items.add(new FilterItemModel(R.drawable.due_tone,
                context.getString(R.string.filter_duetone), PhotoFilter.DUE_TONE));
        items.add(new FilterItemModel(R.drawable.fill_light,
                context.getString(R.string.filter_filllight), PhotoFilter.FILL_LIGHT));
        items.add(new FilterItemModel(R.drawable.flip_vertical,
                context.getString(R.string.filter_filpver), PhotoFilter.FLIP_VERTICAL));
        items.add(new FilterItemModel(R.drawable.flip_horizontal,
                context.getString(R.string.filter_fliphor), PhotoFilter.FLIP_HORIZONTAL));
        items.add(new FilterItemModel(R.drawable.grain,
                context.getString(R.string.filter_grain), PhotoFilter.GRAIN));
        items.add(new FilterItemModel(R.drawable.lomish,
                context.getString(R.string.filter_lomish), PhotoFilter.LOMISH));
        items.add(new FilterItemModel(R.drawable.negative,
                context.getString(R.string.filter_negative), PhotoFilter.NEGATIVE));
        items.add(new FilterItemModel(R.drawable.poster,
                context.getString(R.string.filter_poster), PhotoFilter.POSTERIZE));
        items.add(new FilterItemModel(R.drawable.rotate,
                context.getString(R.string.filter_rotate), PhotoFilter.ROTATE));
        items.add(new FilterItemModel(R.drawable.saturate,
                context.getString(R.string.filter_saturate), PhotoFilter.SATURATE));
        items.add(new FilterItemModel(R.drawable.sepia,
                context.getString(R.string.filter_sepia), PhotoFilter.SEPIA));
        items.add(new FilterItemModel(R.drawable.sharpen,
                context.getString(R.string.filter_sharpen), PhotoFilter.SHARPEN));
        items.add(new FilterItemModel(R.drawable.temp,
                context.getString(R.string.filter_temp), PhotoFilter.TEMPERATURE));
        items.add(new FilterItemModel(R.drawable.tint,
                context.getString(R.string.filter_tint), PhotoFilter.TINT));
        items.add(new FilterItemModel(R.drawable.vignette,
                context.getString(R.string.filter_vig), PhotoFilter.VIGNETTE));

        return items;
    }
}
