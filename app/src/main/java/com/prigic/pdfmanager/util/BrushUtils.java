package com.prigic.pdfmanager.util;

import java.util.ArrayList;

import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.model.BrushItemModel;

public class BrushUtils {

    public static ArrayList<BrushItemModel> getBrushItems() {
        ArrayList<BrushItemModel> brushItemModels = new ArrayList<>();
        brushItemModels.add(new BrushItemModel(R.color.mb_white));
        brushItemModels.add(new BrushItemModel(R.color.red));
        brushItemModels.add(new BrushItemModel(R.color.mb_blue));
        brushItemModels.add(new BrushItemModel(R.color.mb_green));
        brushItemModels.add(new BrushItemModel(R.color.colorPrimary));
        brushItemModels.add(new BrushItemModel(R.color.colorAccent));
        brushItemModels.add(new BrushItemModel(R.color.light_gray));
        brushItemModels.add(new BrushItemModel(R.color.black));
        brushItemModels.add(new BrushItemModel(R.drawable.color_palette));
        return brushItemModels;
    }
}