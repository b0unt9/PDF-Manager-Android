package com.prigic.pdfmanager.model;

public class PreviewImageOptionItemModel {
    private int mOptionImageId;
    private String mOptionName;

    public PreviewImageOptionItemModel(int mOptionImageId, String mOptionName) {
        this.mOptionImageId = mOptionImageId;
        this.mOptionName = mOptionName;
    }

    public int getOptionImageId() {
        return mOptionImageId;
    }

    public void setOptionImageId(int mOptionImageId) {
        this.mOptionImageId = mOptionImageId;
    }

    public String getOptionName() {
        return mOptionName;
    }

    public void setOptionName(String mOptionName) {
        this.mOptionName = mOptionName;
    }
}
