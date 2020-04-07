package com.prigic.pdfmanager.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.dd.morphingbutton.MorphingButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.activity.ImagesPreviewActivity;
import com.prigic.pdfmanager.adapter.ExtractImagesAdapter;
import com.prigic.pdfmanager.adapter.MergeFilesAdapter;
import com.prigic.pdfmanager.interfaces.BottomSheetPopulate;
import com.prigic.pdfmanager.interfaces.ExtractImagesListener;
import com.prigic.pdfmanager.util.BottomSheetCallback;
import com.prigic.pdfmanager.util.BottomSheetUtils;
import com.prigic.pdfmanager.util.CommonCodeUtils;
import com.prigic.pdfmanager.util.ExtractImages;
import com.prigic.pdfmanager.util.FileUtils;
import com.prigic.pdfmanager.util.MorphButtonUtility;
import com.prigic.pdfmanager.util.PdfToImages;
import com.prigic.pdfmanager.util.RealPathUtil;

import static android.app.Activity.RESULT_OK;
import static com.prigic.pdfmanager.util.CommonCodeUtils.populateUtil;
import static com.prigic.pdfmanager.util.Constants.BUNDLE_DATA;
import static com.prigic.pdfmanager.util.Constants.PDF_TO_IMAGES;
import static com.prigic.pdfmanager.util.DialogUtils.createAnimationDialog;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class PdfToImageFragment extends Fragment implements BottomSheetPopulate, MergeFilesAdapter.OnClickListener,
        ExtractImagesListener, ExtractImagesAdapter.OnFileItemClickedListener {

    private static final int INTENT_REQUEST_PICKFILE_CODE = 10;
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private Activity mActivity;
    private String mPath;
    private Uri mUri;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private BottomSheetBehavior mSheetBehavior;
    private BottomSheetUtils mBottomSheetUtils;
    private ArrayList<String> mOutputFilePaths;
    private MaterialDialog mMaterialDialog;
    private String mOperation;

    @BindView(R.id.lottie_progress)
    LottieAnimationView mLottieProgress;
    @BindView(R.id.bottom_sheet)
    LinearLayout mLayoutBottomSheet;
    @BindView(R.id.upArrow)
    ImageView mUpArrow;
    @BindView(R.id.selectFile)
    MorphingButton mSelectFileButton;
    @BindView(R.id.createImages)
    MorphingButton mCreateImagesButton;
    @BindView(R.id.created_images)
    RecyclerView mCreatedImages;
    @BindView(R.id.pdfToImagesText)
    TextView mCreateImagesSuccessText;
    @BindView(R.id.options)
    LinearLayout options;
    @BindView(R.id.layout)
    RelativeLayout mLayout;
    @BindView(R.id.recyclerViewFiles)
    RecyclerView mRecyclerViewFiles;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pdf_to_image, container, false);
        ButterKnife.bind(this, rootView);
        mOperation = getArguments().getString(BUNDLE_DATA);
        mSheetBehavior = BottomSheetBehavior.from(mLayoutBottomSheet);
        mSheetBehavior.setBottomSheetCallback(new BottomSheetCallback(mUpArrow, isAdded()));
        mLottieProgress.setVisibility(View.VISIBLE);
        mBottomSheetUtils.populateBottomSheetWithPDFs(this);
        resetView();
        return rootView;
    }

    @OnClick(R.id.viewImagesInGallery)
    void onImagesInGalleryClick() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri imagesUri = Uri.parse("content:///storage/emulated/0/PDFfiles/");
        intent.setDataAndType(imagesUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    @OnClick(R.id.viewFiles)
    void onViewFilesClick() {
        mBottomSheetUtils.showHideSheet(mSheetBehavior);
    }

    @OnClick(R.id.viewImages)
    void onViewImagesClicked() {
        mActivity.startActivity(ImagesPreviewActivity.getStartIntent(mActivity, mOutputFilePaths));
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
            mUri = data.getData();
            String path = RealPathUtil.getRealPath(getContext(), data.getData());
                setTextAndActivateButtons(path);
        }
    }

    @OnClick(R.id.createImages)
    public void parse() {
        if (mOperation.equals(PDF_TO_IMAGES))
            new PdfToImages(mPath, mUri, this).execute();
        else
            new ExtractImages(mPath, this).execute();
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
        mUri = null;
        mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setTextAndActivateButtons(path);
    }

    private void setTextAndActivateButtons(String path) {
        mCreatedImages.setVisibility(View.GONE);
        options.setVisibility(View.GONE);
        mCreateImagesSuccessText.setVisibility(View.GONE);
        mPath = path;
        mMorphButtonUtility.setTextAndActivateButtons(path,
                mSelectFileButton, mCreateImagesButton);
    }

    @Override
    public void onFileItemClick(String path) {
        mFileUtils.openImage(path);
    }

    @Override
    public void resetView() {
        mPath = null;
        mMorphButtonUtility.initializeButton(mSelectFileButton, mCreateImagesButton);
    }

    @Override
    public void extractionStarted() {
        mMaterialDialog = createAnimationDialog(mActivity);
        mMaterialDialog.show();
    }

    @Override
    public void updateView(int imageCount, ArrayList<String> outputFilePaths) {

        mMaterialDialog.dismiss();
        resetView();
        mOutputFilePaths = outputFilePaths;

        CommonCodeUtils.updateView(mActivity, imageCount, outputFilePaths,
                mCreateImagesSuccessText, options, mCreatedImages, this);
    }


    @Override
    public void onPopulate(ArrayList<String> paths) {
        populateUtil(mActivity, paths, this, mLayout, mLottieProgress, mRecyclerViewFiles);
    }
}
