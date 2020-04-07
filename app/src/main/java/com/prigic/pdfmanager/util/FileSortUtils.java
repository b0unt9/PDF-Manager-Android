package com.prigic.pdfmanager.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileSortUtils {

    public static final int NAME_INDEX = 0;
    public static final int DATE_INDEX = 1;
    public static final int SIZE_INCREASING_ORDER_INDEX = 2;
    public static final int SIZE_DECREASING_ORDER_INDEX = 3;

    public static void performSortOperation(int option, ArrayList<File> pdf) {
        switch (option) {
            case DATE_INDEX:
                sortFilesByDateNewestToOldest(pdf);
                break;
            case NAME_INDEX:
                sortByNameAlphabetical(pdf);
                break;
            case SIZE_INCREASING_ORDER_INDEX:
                sortFilesBySizeIncreasingOrder(pdf);
                break;
            case SIZE_DECREASING_ORDER_INDEX:
                sortFilesBySizeDecreasingOrder(pdf);
                break;
        }
    }
    private static void sortByNameAlphabetical(ArrayList<File> filesList) {
        Collections.sort(filesList);
    }

    private static void sortFilesByDateNewestToOldest(ArrayList<File> filesList) {
        Collections.sort(filesList, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                return Long.compare(file2.lastModified(), file.lastModified());
            }
        });
    }

    private static void sortFilesBySizeIncreasingOrder(ArrayList<File> filesList) {
        Collections.sort(filesList, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return Long.compare(file1.length(), file2.length());
            }
        });
    }

    private static void sortFilesBySizeDecreasingOrder(ArrayList<File> filesList) {
        Collections.sort(filesList, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return Long.compare(file2.length(), file1.length());
            }
        });
    }


}
