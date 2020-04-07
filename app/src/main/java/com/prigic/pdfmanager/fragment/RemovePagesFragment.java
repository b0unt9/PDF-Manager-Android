package com.prigic.pdfmanager.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.dd.morphingbutton.MorphingButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.activity.RearrangePdfPagesActivity;
import com.prigic.pdfmanager.adapter.MergeFilesAdapter;
import com.prigic.pdfmanager.util.DatabaseHelper;
import com.prigic.pdfmanager.interfaces.BottomSheetPopulate;
import com.prigic.pdfmanager.interfaces.OnPDFCompressedInterface;
import com.prigic.pdfmanager.util.BottomSheetCallback;
import com.prigic.pdfmanager.util.BottomSheetUtils;
import com.prigic.pdfmanager.util.FileUtils;
import com.prigic.pdfmanager.util.MorphButtonUtility;
import com.prigic.pdfmanager.util.PDFEncryptionUtility;
import com.prigic.pdfmanager.util.PDFUtils;
import com.prigic.pdfmanager.util.RealPathUtil;

import static android.app.Activity.RESULT_OK;
import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static com.prigic.pdfmanager.util.CommonCodeUtils.populateUtil;
import static com.prigic.pdfmanager.util.Constants.ADD_PWD;
import static com.prigic.pdfmanager.util.Constants.BUNDLE_DATA;
import static com.prigic.pdfmanager.util.Constants.COMPRESS_PDF;
import static com.prigic.pdfmanager.util.Constants.REMOVE_PAGES;
import static com.prigic.pdfmanager.util.Constants.REMOVE_PWd;
import static com.prigic.pdfmanager.util.Constants.REORDER_PAGES;
import static com.prigic.pdfmanager.util.Constants.RESULT;
import static com.prigic.pdfmanager.util.DialogUtils.createAnimationDialog;
import static com.prigic.pdfmanager.util.FileUtils.getFormattedSize;
import static com.prigic.pdfmanager.util.StringUtils.getSnackbarwithAction;
import static com.prigic.pdfmanager.util.StringUtils.hideKeyboard;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class RemovePagesFragment extends Fragment implements MergeFilesAdapter.OnClickListener,
        OnPDFCompressedInterface, BottomSheetPopulate {

    private Activity mActivity;
    private String mPath;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private BottomSheetUtils mBottomSheetUtils;
    private PDFUtils mPDFUtils;
    private static final int INTENT_REQUEST_PICKFILE_CODE = 10;
    private static final int INTENT_REQUEST_REARRANGE_PDF = 11;
    private String mOperation;
    private MaterialDialog mMaterialDialog;

    @BindView(R.id.lottie_progress)
    LottieAnimationView mLottieProgress;
    @BindView(R.id.selectFile)
    MorphingButton selectFileButton;
    @BindView(R.id.pdfCreate)
    MorphingButton createPdf;
    BottomSheetBehavior sheetBehavior;
    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.upArrow)
    ImageView mUpArrow;
    @BindView(R.id.downArrow)
    ImageView mDownArrow;
    @BindView(R.id.layout)
    RelativeLayout mLayout;
    @BindView(R.id.pages)
    EditText pagesInput;
    @BindView(R.id.recyclerViewFiles)
    RecyclerView mRecyclerViewFiles;
    @BindView(R.id.infoText)
    TextView mInfoText;
    @BindView(R.id.compressionInfoText)
    TextView mCompressionInfoText;
    @BindView(R.id.view_pdf)
    Button mViewPdf;
    private Uri mUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_remove_pages, container, false);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) throws NullPointerException {
        if (data == null || resultCode != RESULT_OK)
            return;
        if (requestCode == INTENT_REQUEST_PICKFILE_CODE) {
            mUri = data.getData();
            String path = RealPathUtil.getRealPath(getContext(), data.getData());
                setTextAndActivateButtons(path);

        } else if (requestCode == INTENT_REQUEST_REARRANGE_PDF) {
            String pages = data.getStringExtra(RESULT);

            if (mPath == null)
                return;

            Log.v("output", pages + " ");
            String outputPath = mPath.replace(mActivity.getString(R.string.pdf_ext),
                    "_edited" + pages + mActivity.getString(R.string.pdf_ext));
            if (mPDFUtils.isPDFEncrypted(mPath)) {
                showSnackbar(mActivity, R.string.encrypted_pdf);
                return;
            }

            if (mPDFUtils.reorderRemovePDF(mPath, outputPath, pages)) {
                viewPdfButton(outputPath);
            }
            resetValues();
        }
    }

    @OnClick(R.id.pdfCreate)
    public void parse() {
        hideKeyboard(mActivity);
        if (mOperation.equals(COMPRESS_PDF)) {
            compressPDF();
            return;
        }

        PDFEncryptionUtility pdfEncryptionUtility = new PDFEncryptionUtility(mActivity);
        if (mOperation.equals(ADD_PWD)) {
            if (!mPDFUtils.isPDFEncrypted(mPath)) {
                pdfEncryptionUtility.setPassword(mPath, null);
            } else {
                showSnackbar(mActivity, R.string.encrypted_pdf);
            }
            return;
        }

        if (mOperation.equals(REMOVE_PWd)) {
            if (mPDFUtils.isPDFEncrypted(mPath)) {
                pdfEncryptionUtility.removePassword(mPath, null);
            } else {
                showSnackbar(mActivity, R.string.not_encrypted);
            }
            return;
        }

        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        ParcelFileDescriptor fileDescriptor = null;
        try {
            if (mUri != null)
                fileDescriptor = mActivity.getContentResolver().openFileDescriptor(mUri, "r");
            else if (mPath != null)
                fileDescriptor = ParcelFileDescriptor.open(new File(mPath), MODE_READ_ONLY);
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                final int pageCount = renderer.getPageCount();
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                            Bitmap.Config.ARGB_8888);

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);


                    bitmaps.add(bitmap);

                    page.close();
                }


                renderer.close();
            }
        } catch (IOException | SecurityException | IllegalArgumentException | OutOfMemoryError e) {
            e.printStackTrace();
        }

        if (bitmaps.size() < 1) {
            showSnackbar(mActivity, R.string.file_access_error);
        } else {
            RearrangePdfPagesActivity.mImages = bitmaps;
            startActivityForResult(RearrangePdfPagesActivity.getStartIntent(mActivity),
                    INTENT_REQUEST_REARRANGE_PDF);
        }

    }

    private void compressPDF() {
        String input = pagesInput.getText().toString();
        int check;
        try {
            check = Integer.parseInt(String.valueOf(input));
            if (check > 100 || check <= 0) {
                showSnackbar(mActivity, R.string.invalid_entry);
            } else {
                String outputPath = mPath.replace(mActivity.getString(R.string.pdf_ext),
                        "_edited" + check + mActivity.getString(R.string.pdf_ext));
                mPDFUtils.compressPDF(mPath, outputPath, 100 - check, this);
            }
        } catch (NumberFormatException e) {
            showSnackbar(mActivity, R.string.invalid_entry);
        }
    }

    private void resetValues() {
        mPath = null;
        pagesInput.setText(null);
        mMorphButtonUtility.initializeButton(selectFileButton, createPdf);
        switch (mOperation) {
            case REORDER_PAGES:
            case REMOVE_PAGES:
            case ADD_PWD:
            case REMOVE_PWd:
                mInfoText.setVisibility(View.GONE);
                pagesInput.setVisibility(View.GONE);
                break;
            case COMPRESS_PDF:
                mInfoText.setText(R.string.compress_pdf_prompt);
                break;
        }
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
        mCompressionInfoText.setVisibility(View.GONE);
        mMorphButtonUtility.setTextAndActivateButtons(path,
                selectFileButton, createPdf);
    }

    @Override
    public void pdfCompressionStarted() {
        mMaterialDialog = createAnimationDialog(mActivity);
        mMaterialDialog.show();
    }

    @Override
    public void pdfCompressionEnded(final String path, Boolean success) {
        mMaterialDialog.dismiss();
        if (success && path != null && mPath != null) {
            getSnackbarwithAction(mActivity, R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFileUtils.openFile(path);
                        }
                    }).show();
            new DatabaseHelper(mActivity).insertRecord(path,
                    mActivity.getString(R.string.created));
            File input = new File(mPath);
            File output = new File(path);
            viewPdfButton(path);
            mCompressionInfoText.setVisibility(View.VISIBLE);
            mCompressionInfoText.setText(String.format(mActivity.getString(R.string.compress_info),
                    getFormattedSize(input),
                    getFormattedSize(output)));
        } else {
            showSnackbar(mActivity, R.string.encrypted_pdf);
        }
        resetValues();
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
    public void onPopulate(ArrayList<String> paths) {
        populateUtil(mActivity, paths, this, mLayout, mLottieProgress, mRecyclerViewFiles);
    }
}
