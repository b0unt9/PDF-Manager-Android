package com.prigic.pdfmanager.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.adapter.ViewFilesAdapter;
import com.prigic.pdfmanager.interfaces.MergeFilesListener;

import static com.prigic.pdfmanager.util.Constants.MASTER_PWD_STRING;
import static com.prigic.pdfmanager.util.Constants.STORAGE_LOCATION;
import static com.prigic.pdfmanager.util.Constants.appName;
import static com.prigic.pdfmanager.util.DialogUtils.createAnimationDialog;
import static com.prigic.pdfmanager.util.DialogUtils.createOverwriteDialog;
import static com.prigic.pdfmanager.util.StringUtils.getDefaultStorageLocation;
import static com.prigic.pdfmanager.util.StringUtils.getSnackbarwithAction;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class MergeHelper implements MergeFilesListener {
    private MaterialDialog mMaterialDialog;
    private Activity mActivity;
    private FileUtils mFileUtils;
    private boolean mPasswordProtected = false;
    private String mPassword;
    private String mHomePath;
    private Context mContext;
    private ViewFilesAdapter mViewFilesAdapter;
    private SharedPreferences mSharedPrefs;

    public MergeHelper(Activity activity, ViewFilesAdapter viewFilesAdapter) {
        mActivity = activity;
        mFileUtils = new FileUtils(mActivity);
        mHomePath = PreferenceManager.getDefaultSharedPreferences(mActivity)
                .getString(STORAGE_LOCATION,
                        getDefaultStorageLocation());
        mContext = mActivity;
        mViewFilesAdapter = viewFilesAdapter;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    public void mergeFiles() {
        final String[] pdfpaths = mViewFilesAdapter.getSelectedFilePath().toArray(new String[0]);
        final String masterpwd = mSharedPrefs.getString(MASTER_PWD_STRING, appName);
        new MaterialDialog.Builder(mActivity)
                .title(R.string.creating_pdf)
                .content(R.string.enter_file_name)
                .input(mContext.getResources().getString(R.string.example), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, final CharSequence input) {
                        if (StringUtils.isEmpty(input)) {
                            showSnackbar(mActivity, R.string.snackbar_name_not_blank);
                        } else {
                            if (!mFileUtils.isFileExist(input + mContext.getResources().getString(R.string.pdf_ext))) {
                                new MergePdf(input.toString(), mHomePath, mPasswordProtected,
                                        mPassword, MergeHelper.this, masterpwd).execute(pdfpaths);
                            } else {
                                MaterialDialog.Builder builder = createOverwriteDialog(mActivity);
                                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog12, @NonNull DialogAction which) {
                                        new MergePdf(input.toString(),
                                                mHomePath, mPasswordProtected, mPassword,
                                                MergeHelper.this, masterpwd).execute(pdfpaths);
                                    }
                                })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog1, @NonNull DialogAction which) {
                                                MergeHelper.this.mergeFiles();
                                            }
                                        }).show();
                            }
                        }
                    }
                })
                .show();
    }
    @Override
    public void resetValues(boolean isPDFMerged, final String path) {
        mMaterialDialog.dismiss();
        if (isPDFMerged) {
            getSnackbarwithAction(mActivity, R.string.pdf_merged)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFileUtils.openFile(path);
                        }
                    }).show();
            new DatabaseHelper(mActivity).insertRecord(path,
                    mActivity.getString(R.string.created));
        } else
            showSnackbar(mActivity, R.string.file_access_error);
        mViewFilesAdapter.updateDataset();
    }

    @Override
    public void mergeStarted() {
        mMaterialDialog = createAnimationDialog(mActivity);
        mMaterialDialog.show();
    }
}
