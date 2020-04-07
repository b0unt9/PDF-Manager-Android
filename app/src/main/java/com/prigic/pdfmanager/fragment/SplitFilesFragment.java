package com.prigic.pdfmanager.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.dd.morphingbutton.MorphingButton;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.adapter.FilesListAdapter;
import com.prigic.pdfmanager.adapter.MergeFilesAdapter;
import com.prigic.pdfmanager.interfaces.BottomSheetPopulate;
import com.prigic.pdfmanager.util.BottomSheetCallback;
import com.prigic.pdfmanager.util.BottomSheetUtils;
import com.prigic.pdfmanager.util.FileUtils;
import com.prigic.pdfmanager.util.MorphButtonUtility;
import com.prigic.pdfmanager.util.PDFUtils;
import com.prigic.pdfmanager.util.RealPathUtil;
import com.prigic.pdfmanager.util.ViewFilesDividerItemDecoration;

import static android.app.Activity.RESULT_OK;
import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static com.prigic.pdfmanager.util.CommonCodeUtils.populateUtil;
import static com.prigic.pdfmanager.util.StringUtils.hideKeyboard;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class SplitFilesFragment extends Fragment implements MergeFilesAdapter.OnClickListener,
        FilesListAdapter.OnFileItemClickedListener, BottomSheetPopulate {

    private Activity mActivity;
    private String mPath;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private PDFUtils mPDFUtils;
    private BottomSheetUtils mBottomSheetUtils;
    private static final int INTENT_REQUEST_PICKFILE_CODE = 10;

    @BindView(R.id.lottie_progress)
    LottieAnimationView mLottieProgress;
    @BindView(R.id.selectFile)
    MorphingButton selectFileButton;
    @BindView(R.id.splitFiles)
    MorphingButton splitFilesButton;
    BottomSheetBehavior sheetBehavior;
    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.upArrow)
    ImageView mUpArrow;
    @BindView(R.id.downArrow)
    ImageView mDownArrow;
    @BindView(R.id.layout)
    RelativeLayout mLayout;
    @BindView(R.id.recyclerViewFiles)
    RecyclerView mRecyclerViewFiles;
    @BindView(R.id.splitted_files)
    RecyclerView mSplittedFiles;
    @BindView(R.id.splitfiles_text)
    TextView splitFilesSuccessText;
    @BindView(R.id.split_config)
    EditText mSplitConfitEditText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_split_files, container, false);
        ButterKnife.bind(this, rootview);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setBottomSheetCallback(new BottomSheetCallback(mUpArrow, isAdded()));
        mLottieProgress.setVisibility(View.VISIBLE);
        mBottomSheetUtils.populateBottomSheetWithPDFs(this);

        resetValues();
        return rootview;
    }

    @OnClick(R.id.viewFiles)
    void onViewFilesClick(View view) {
        mBottomSheetUtils.showHideSheet(sheetBehavior);
    }

    @OnClick(R.id.selectFile)
    public void showFileChooser() {
        startActivityForResult(mFileUtils.getFileChooser(),
                INTENT_REQUEST_PICKFILE_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) throws NullPointerException {
        if (data == null || resultCode != RESULT_OK || data.getData() == null)
            return;
        if (requestCode == INTENT_REQUEST_PICKFILE_CODE) {
            String path = RealPathUtil.getRealPath(getContext(), data.getData());
            setTextAndActivateButtons(path);
        }
    }

    @OnClick(R.id.splitFiles)
    public void parse() {
        hideKeyboard(mActivity);

        ArrayList<String> outputFilePaths = mPDFUtils.splitPDFByConfig(mPath,
                mSplitConfitEditText.getText().toString());
        int numberOfPages = outputFilePaths.size();
        if (numberOfPages > 0) {
            String output = String.format(mActivity.getString(R.string.split_success), numberOfPages);
            showSnackbar(mActivity, output);
            splitFilesSuccessText.setVisibility(View.VISIBLE);
            splitFilesSuccessText.setText(output);

            FilesListAdapter splitFilesAdapter = new FilesListAdapter(mActivity, outputFilePaths, this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
            mSplittedFiles.setVisibility(View.VISIBLE);
            mSplittedFiles.setLayoutManager(mLayoutManager);
            mSplittedFiles.setAdapter(splitFilesAdapter);
            mSplittedFiles.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));
            resetValues();
        }
    }

    private void resetValues() {
        mPath = null;
        mMorphButtonUtility.initializeButton(selectFileButton, splitFilesButton);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        mFileUtils = new FileUtils(mActivity);
        mPDFUtils = new PDFUtils(mActivity);
        mBottomSheetUtils = new BottomSheetUtils(mActivity);
    }

    @Override
    public void onItemClick(String path) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setTextAndActivateButtons(path);
    }

    private void setTextAndActivateButtons(String path) {
        mSplittedFiles.setVisibility(View.GONE);
        splitFilesSuccessText.setVisibility(View.GONE);
        mPath = path;
        mMorphButtonUtility.setTextAndActivateButtons(path,
                selectFileButton, splitFilesButton);
        mSplitConfitEditText.setVisibility(View.VISIBLE);
        mSplitConfitEditText.setText(getDefaultSplitConfig(mPath));
    }


    private String getDefaultSplitConfig(String mPath) {
        String splitConfig = "";
        ParcelFileDescriptor fileDescriptor = null;
        try {
            if (mPath != null)
                fileDescriptor = ParcelFileDescriptor.open(new File(mPath), MODE_READ_ONLY);
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                final int pageCount = renderer.getPageCount();
                for (int i = 1; i <= pageCount; i++) {
                    splitConfig = splitConfig + i + ",";
                }
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
        return splitConfig;
    }

    @Override
    public void onFileItemClick(String path) {
        mFileUtils.openFile(path);
    }

    @Override
    public void onPopulate(ArrayList<String> paths) {
        populateUtil(mActivity, paths, this, mLayout, mLottieProgress, mRecyclerViewFiles);
    }
}
