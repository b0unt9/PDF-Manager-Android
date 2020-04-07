package com.prigic.pdfmanager.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.dd.morphingbutton.MorphingButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.adapter.FilesListAdapter;
import com.prigic.pdfmanager.adapter.MergeFilesAdapter;
import com.prigic.pdfmanager.util.DatabaseHelper;
import com.prigic.pdfmanager.interfaces.BottomSheetPopulate;
import com.prigic.pdfmanager.interfaces.OnPDFCreatedInterface;
import com.prigic.pdfmanager.util.BottomSheetCallback;
import com.prigic.pdfmanager.util.BottomSheetUtils;
import com.prigic.pdfmanager.util.FileUtils;
import com.prigic.pdfmanager.util.InvertPdf;
import com.prigic.pdfmanager.util.MorphButtonUtility;
import com.prigic.pdfmanager.util.RealPathUtil;
import com.prigic.pdfmanager.util.ViewFilesDividerItemDecoration;

import static android.app.Activity.RESULT_OK;
import static com.prigic.pdfmanager.util.DialogUtils.createAnimationDialog;
import static com.prigic.pdfmanager.util.StringUtils.getSnackbarwithAction;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class InvertPdfFragment extends Fragment implements MergeFilesAdapter.OnClickListener,
        FilesListAdapter.OnFileItemClickedListener, BottomSheetPopulate, OnPDFCreatedInterface {

    private Activity mActivity;
    private String mPath;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private BottomSheetUtils mBottomSheetUtils;
    private static final int INTENT_REQUEST_PICKFILE_CODE = 10;
    private MaterialDialog mMaterialDialog;

    @BindView(R.id.lottie_progress)
    LottieAnimationView mLottieProgress;
    @BindView(R.id.selectFile)
    MorphingButton selectFileButton;
    @BindView(R.id.invert)
    MorphingButton invertPdfButton;
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
    @BindView(R.id.view_pdf)
    Button mViewPdf;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_invert_pdf, container, false);
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


    @OnClick(R.id.invert)
    public void parse() {
        new InvertPdf(mPath, this).execute();
    }


    private void resetValues() {
        mPath = null;
        mMorphButtonUtility.initializeButton(selectFileButton, invertPdfButton);
    }

    private void setTextAndActivateButtons(String path) {
        mPath = path;
        mMorphButtonUtility.setTextAndActivateButtons(path,
                selectFileButton, invertPdfButton);
    }

    @Override
    public void onPopulate(ArrayList<String> paths) {
        if (paths == null || paths.size() == 0) {
            mLayout.setVisibility(View.GONE);
        } else {
            mRecyclerViewFiles.setVisibility(View.VISIBLE);
            MergeFilesAdapter mergeFilesAdapter = new MergeFilesAdapter(mActivity, paths, false, this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
            mRecyclerViewFiles.setLayoutManager(mLayoutManager);
            mRecyclerViewFiles.setAdapter(mergeFilesAdapter);
            mRecyclerViewFiles.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));
        }
        mLottieProgress.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        mFileUtils = new FileUtils(mActivity);
        mBottomSheetUtils = new BottomSheetUtils(mActivity);
    }

    @Override
    public void onItemClick(String path) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setTextAndActivateButtons(path);
    }

    @Override
    public void onFileItemClick(String path) {
        mFileUtils.openFile(path);
    }

    private void viewPdfButton(final String path) {
        mViewPdf.setVisibility(View.VISIBLE);
        mViewPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFileUtils.openFile(path);
            }
        });
    }

    @Override

    public void onPDFCreationStarted() {
        mMaterialDialog = createAnimationDialog(mActivity);
        mMaterialDialog.show();
    }

    @Override
    public void onPDFCreated(boolean isNewPdfCreated, final String path) {
        mMaterialDialog.dismiss();
        if (!isNewPdfCreated) {
            showSnackbar(mActivity, R.string.snackbar_invert_unsuccessful);
            mViewPdf.setVisibility(View.GONE);
            return;
        }
        new DatabaseHelper(mActivity).insertRecord(path, mActivity.getString(R.string.snackbar_invert_successfull));
        getSnackbarwithAction(mActivity, R.string.snackbar_pdfCreated)
                .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFileUtils.openFile(path);
                    }
                }).show();
        viewPdfButton(path);
        resetValues();
    }
}

