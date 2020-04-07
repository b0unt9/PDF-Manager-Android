package com.prigic.pdfmanager.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.prigic.pdfmanager.adapter.ViewFilesAdapter;
import com.prigic.pdfmanager.interfaces.EmptyStateChangeListener;
import com.prigic.pdfmanager.model.PDFFileModel;

public class PopulateList extends AsyncTask<Void, Void, Void> {

    private final int mCurrentSortingIndex;
    private final EmptyStateChangeListener mEmptyStateChangeListener;
    private final DirectoryUtils mDirectoryUtils;
    private final ViewFilesAdapter mAdapter;
    private final Handler mHandler;
    @Nullable
    private String mQuery;

    public PopulateList(ViewFilesAdapter adapter,
                        EmptyStateChangeListener emptyStateChangeListener,
                        DirectoryUtils directoryUtils, int index, @Nullable String mQuery) {
        this.mAdapter = adapter;
        mCurrentSortingIndex = index;
        mEmptyStateChangeListener = emptyStateChangeListener;
        this.mQuery = mQuery;
        mHandler = new Handler(Looper.getMainLooper());
        mDirectoryUtils = directoryUtils;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        populateListView();
        return null;
    }

    private void populateListView() {
        ArrayList<File> pdfFiles;
        if (TextUtils.isEmpty(mQuery)) {
            pdfFiles = mDirectoryUtils.getPdfFromOtherDirectories();
        } else {
            pdfFiles = mDirectoryUtils.searchPDF(mQuery);
        }
        if (pdfFiles == null)
            mHandler.post(mEmptyStateChangeListener::showNoPermissionsView);
        else if (pdfFiles.size() == 0) {
            mHandler.post(mEmptyStateChangeListener::setEmptyStateVisible);
        } else {
            FileSortUtils.performSortOperation(mCurrentSortingIndex, pdfFiles);
            final List<PDFFileModel> pdfFilesWithEncryptionStatusModels = getPdfFilesWithEncryptionStatus(pdfFiles);
            mHandler.post(mEmptyStateChangeListener::hideNoPermissionsView);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.setData(pdfFilesWithEncryptionStatusModels);
                }
            });
        }
    }

    @WorkerThread
    private List<PDFFileModel> getPdfFilesWithEncryptionStatus(@NonNull List<File> files) {
        List<PDFFileModel> pdfFileModels = new ArrayList<>(files.size());
        for (File file : files) {
            pdfFileModels.add(new PDFFileModel(file, mAdapter.getPDFUtils().isPDFEncrypted(file.getPath())));
        }
        return pdfFileModels;
    }
}