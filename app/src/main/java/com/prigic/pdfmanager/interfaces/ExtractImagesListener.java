package com.prigic.pdfmanager.interfaces;

import java.util.ArrayList;

public interface ExtractImagesListener {
    void resetView();
    void extractionStarted();
    void updateView(int imagecount, ArrayList<String> outputFilePaths);
}