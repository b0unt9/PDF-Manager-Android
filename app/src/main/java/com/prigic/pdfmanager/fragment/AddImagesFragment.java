package com.prigic.pdfmanager.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.dd.morphingbutton.MorphingButton;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.adapter.MergeFilesAdapter;
import com.prigic.pdfmanager.interfaces.BottomSheetPopulate;
import com.prigic.pdfmanager.util.BottomSheetCallback;
import com.prigic.pdfmanager.util.BottomSheetUtils;
import com.prigic.pdfmanager.util.FileUtils;
import com.prigic.pdfmanager.util.MorphButtonUtility;
import com.prigic.pdfmanager.util.PDFUtils;
import com.prigic.pdfmanager.util.StringUtils;

import static com.prigic.pdfmanager.util.CommonCodeUtils.populateUtil;
import static com.prigic.pdfmanager.util.Constants.ADD_IMAGES;
import static com.prigic.pdfmanager.util.Constants.BUNDLE_DATA;
import static com.prigic.pdfmanager.util.DialogUtils.createAnimationDialog;
import static com.prigic.pdfmanager.util.DialogUtils.createCustomDialog;
import static com.prigic.pdfmanager.util.DialogUtils.createOverwriteDialog;
import static com.prigic.pdfmanager.util.FileUriUtils.getFilePath;
import static com.prigic.pdfmanager.util.StringUtils.hideKeyboard;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class AddImagesFragment extends Fragment implements BottomSheetPopulate, MergeFilesAdapter.OnClickListener {

    private Activity mActivity;
    private String mPath;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private BottomSheetUtils mBottomSheetUtils;
    private PDFUtils mPDFUtils;
    private static final int INTENT_REQUEST_PICKFILE_CODE = 10;
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private String mOperation;
    private MaterialDialog mMaterialDialog;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT = 1;
    public static ArrayList<String> mImagesUri = new ArrayList<>();

    @BindView(R.id.lottie_progress)
    LottieAnimationView mLottieProgress;
    @BindView(R.id.selectFile)
    MorphingButton selectFileButton;
    @BindView(R.id.pdfCreate)
    MorphingButton mCreatePdf;
    @BindView(R.id.addImages)
    MorphingButton addImages;
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
    @BindView(R.id.tvNoOfImages)
    TextView mNoOfImages;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_add_images, container, false);
        ButterKnife.bind(this, rootview);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setBottomSheetCallback(new BottomSheetCallback(mUpArrow, isAdded()));
        mOperation = getArguments().getString(BUNDLE_DATA);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        switch (requestCode) {
            case INTENT_REQUEST_GET_IMAGES:
                mImagesUri.clear();
                mImagesUri.addAll(Matisse.obtainPathResult(data));
                if (mImagesUri.size() > 0) {
                    mNoOfImages.setText(String.format(mActivity.getResources()
                            .getString(R.string.images_selected), mImagesUri.size()));
                    mNoOfImages.setVisibility(View.VISIBLE);
                    showSnackbar(mActivity, R.string.snackbar_images_added);
                } else {
                    mNoOfImages.setVisibility(View.GONE);
                }
                mMorphButtonUtility.morphToSquare(mCreatePdf, mMorphButtonUtility.integer());
                break;
            case INTENT_REQUEST_PICKFILE_CODE:
                if (!(data.getData()==null)){
                    setTextAndActivateButtons(getFilePath(data.getData()));
                }
                break;
        }
    }

    @OnClick(R.id.pdfCreate)
    public void parse() {
        hideKeyboard(mActivity);
        if (mOperation.equals(ADD_IMAGES)) {
            getFileName();
        }
    }

    private void getFileName() {
        MaterialDialog.Builder builder = createCustomDialog(mActivity,
                R.string.creating_pdf, R.string.enter_file_name);
        builder.input(getString(R.string.example), null, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                if (StringUtils.isEmpty(input)) {
                    showSnackbar(mActivity, R.string.snackbar_name_not_blank);
                } else {
                    final String filename = input.toString();
                    FileUtils utils = new FileUtils(mActivity);
                    if (!utils.isFileExist(filename + AddImagesFragment.this.getString(R.string.pdf_ext))) {
                        AddImagesFragment.this.addImagesToPdf(filename);
                    } else {
                        MaterialDialog.Builder builder2 = createOverwriteDialog(mActivity);
                        builder2.onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog2, @NonNull DialogAction which) {
                                AddImagesFragment.this.addImagesToPdf(filename);
                            }
                        }).onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog1, @NonNull DialogAction which) {
                                AddImagesFragment.this.getFileName();
                            }
                        }).show();
                    }
                }
            }
        }).show();
    }

    private void addImagesToPdf(String output) {
        int index = mPath.lastIndexOf("/");
        String outputPath = mPath.replace(mPath.substring(index + 1),
                output + mActivity.getString(R.string.pdf_ext));

        if (mImagesUri.size() > 0) {
            mMaterialDialog = createAnimationDialog(mActivity);
            mMaterialDialog.show();
            mPDFUtils.addImagesToPdf(mPath, outputPath, mImagesUri);
            mMorphButtonUtility.morphToSuccess(mCreatePdf);
            resetValues();
            mMaterialDialog.dismiss();
        } else {
            showSnackbar(mActivity, R.string.no_images_selected);
        }
    }

    private void resetValues() {
        mPath = null;
        mImagesUri.clear();
        mMorphButtonUtility.initializeButton(selectFileButton, mCreatePdf);
        mNoOfImages.setVisibility(View.GONE);
    }

    @OnClick(R.id.addImages)
    void startAddingImages() {
        if (getRuntimePermissions())
            selectImages();
    }

    private boolean getRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT);
                return false;
            }
        }
        return true;
    }

    public void selectImages() {
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, ""))
                .maxSelectable(1000)
                .imageEngine(new PicassoEngine())
                .forResult(INTENT_REQUEST_GET_IMAGES);
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
        mPath = path;
        mMorphButtonUtility.setTextAndActivateButtons(path,
                selectFileButton, mCreatePdf);
    }

    @Override
    public void onPopulate(ArrayList<String> paths) {
        populateUtil(mActivity, paths, this, mLayout, mLottieProgress, mRecyclerViewFiles);
    }
}
