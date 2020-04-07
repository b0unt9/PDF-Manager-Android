package com.prigic.pdfmanager.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.morphingbutton.MorphingButton;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.adapter.EnhancementOptionsAdapter;
import com.prigic.pdfmanager.interfaces.OnItemClickListner;
import com.prigic.pdfmanager.model.EnhancementOptionsEntityModel;
import com.prigic.pdfmanager.model.TextToPDFOptionsModel;
import com.prigic.pdfmanager.util.Constants;
import com.prigic.pdfmanager.util.FileUtils;
import com.prigic.pdfmanager.util.MorphButtonUtility;
import com.prigic.pdfmanager.util.PDFUtils;
import com.prigic.pdfmanager.util.PageSizeUtils;
import com.prigic.pdfmanager.util.StringUtils;

import static android.app.Activity.RESULT_OK;
import static com.prigic.pdfmanager.util.Constants.STORAGE_LOCATION;
import static com.prigic.pdfmanager.util.DialogUtils.createCustomDialogWithoutContent;
import static com.prigic.pdfmanager.util.DialogUtils.createOverwriteDialog;
import static com.prigic.pdfmanager.util.StringUtils.getDefaultStorageLocation;
import static com.prigic.pdfmanager.util.StringUtils.getSnackbarwithAction;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;
import static com.prigic.pdfmanager.util.TextEnhancementOptionsUtils.getEnhancementOptions;

public class TextToPdfFragment extends Fragment implements OnItemClickListner {

    private Activity mActivity;
    private FileUtils mFileUtils;

    private final int mFileSelectCode = 0;
    private Uri mTextFileUri = null;
    private String mFontTitle;
    private String mFileExtension;
    private int mFontSize = 0;
    private int mButtonClicked = 0;
    private boolean mPasswordProtected = false;
    private String mPassword;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT = 1;
    private boolean mPermissionGranted = false;

    @BindView(R.id.enhancement_options_recycle_view_text)
    RecyclerView mTextEnhancementOptionsRecycleView;
    @BindView(R.id.tv_file_name)
    TextView mTextView;
    @BindView(R.id.createtextpdf)
    MorphingButton mCreateTextPdf;

    private ArrayList<EnhancementOptionsEntityModel> mTextEnhancementOptionsEntityModelArrayList;
    private EnhancementOptionsAdapter mTextEnhancementOptionsAdapter;
    private SharedPreferences mSharedPreferences;
    private Font.FontFamily mFontFamily;
    private MorphButtonUtility mMorphButtonUtility;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_text_to_pdf, container, false);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mFontTitle = String.format(getString(R.string.edit_font_size),
                mSharedPreferences.getInt(Constants.DEFAULT_FONT_SIZE_TEXT, Constants.DEFAULT_FONT_SIZE));
        mFontFamily = Font.FontFamily.valueOf(mSharedPreferences.getString(Constants.DEFAULT_FONT_FAMILY_TEXT,
                Constants.DEFAULT_FONT_FAMILY));
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        ButterKnife.bind(this, rootview);
        showEnhancementOptions();
        mMorphButtonUtility.morphToGrey(mCreateTextPdf, mMorphButtonUtility.integer());
        mCreateTextPdf.setEnabled(false);
        PageSizeUtils.mPageSize = mSharedPreferences.getString(Constants.DEFAULT_PAGE_SIZE_TEXT ,
                Constants.DEFAULT_PAGE_SIZE);
        mFontSize = mSharedPreferences.getInt(Constants.DEFAULT_FONT_SIZE_TEXT, Constants.DEFAULT_FONT_SIZE);

        return rootview;
    }

    private void showEnhancementOptions() {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(mActivity, 2);
        mTextEnhancementOptionsRecycleView.setLayoutManager(mGridLayoutManager);
        mTextEnhancementOptionsEntityModelArrayList = getEnhancementOptions(mActivity, mFontTitle, mFontFamily);
        mTextEnhancementOptionsAdapter = new EnhancementOptionsAdapter(this, mTextEnhancementOptionsEntityModelArrayList);
        mTextEnhancementOptionsRecycleView.setAdapter(mTextEnhancementOptionsAdapter);
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                editFontSize();
                break;
            case 1:
                changeFontFamily();
                break;
            case 2:
                setPageSize();
                break;
            case 3:
                setPassword();
                break;
        }
    }

    private void setPassword() {
        MaterialDialog.Builder builder = createCustomDialogWithoutContent(mActivity,
                R.string.set_password);
        final MaterialDialog dialog = builder
                .customView(R.layout.custom_dialog, true)
                .neutralText(R.string.remove_dialog)
                .build();

        final View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        final View neutralAction = dialog.getActionButton(DialogAction.NEUTRAL);
        final EditText passwordInput = dialog.getCustomView().findViewById(R.id.password);
        passwordInput.setText(mPassword);
        passwordInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        positiveAction.setEnabled(s.toString().trim().length() > 0);
                    }

                    @Override
                    public void afterTextChanged(Editable input) {
                    }
                });

        positiveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(passwordInput.getText())) {
                    showSnackbar(mActivity, R.string.snackbar_password_cannot_be_blank);
                } else {
                    mPassword = passwordInput.getText().toString();
                    mPasswordProtected = true;
                    TextToPdfFragment.this.onPasswordAdded();
                    dialog.dismiss();
                }
            }
        });

        if (StringUtils.isNotEmpty(mPassword)) {
            neutralAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPassword = null;
                    TextToPdfFragment.this.onPasswordRemoved();
                    mPasswordProtected = false;
                    dialog.dismiss();
                    showSnackbar(mActivity, R.string.password_remove);
                }
            });
        }
        dialog.show();
        positiveAction.setEnabled(false);
    }

    private void setPageSize() {
        PageSizeUtils utils = new PageSizeUtils(mActivity);
        utils.showPageSizeDialog(false);
    }

    private void changeFontFamily() {
        String fontFamily = mSharedPreferences.getString(Constants.DEFAULT_FONT_FAMILY_TEXT,
                Constants.DEFAULT_FONT_FAMILY);
        int ordinal = Font.FontFamily.valueOf(fontFamily).ordinal();
        MaterialDialog materialDialog = new MaterialDialog.Builder(mActivity)
                .title(String.format(getString(R.string.default_font_family_text), fontFamily))
                .customView(R.layout.dialog_font_family, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        RadioGroup radioGroup = view.findViewById(R.id.radio_group_font_family);
                        int selectedId = radioGroup.getCheckedRadioButtonId();
                        RadioButton radioButton = view.findViewById(selectedId);
                        String fontFamily1 = radioButton.getText().toString();
                        mFontFamily = Font.FontFamily.valueOf(fontFamily1);
                        final CheckBox cbSetDefault = view.findViewById(R.id.cbSetDefault);
                        if (cbSetDefault.isChecked()) {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(Constants.DEFAULT_FONT_FAMILY_TEXT, fontFamily1);
                            editor.apply();
                        }
                        TextToPdfFragment.this.showFontFamily();
                    }
                })
                .build();
        RadioGroup radioGroup = materialDialog.getCustomView().findViewById(R.id.radio_group_font_family);
        RadioButton rb = (RadioButton) radioGroup.getChildAt(ordinal);
        rb.setChecked(true);
        materialDialog.show();
    }


    private void editFontSize() {
        new MaterialDialog.Builder(mActivity)
                .title(mFontTitle)
                .customView(R.layout.dialog_font_size, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final EditText fontInput = dialog.getCustomView().findViewById(R.id.fontInput);
                        final CheckBox cbSetDefault = dialog.getCustomView().findViewById(R.id.cbSetFontDefault);
                        try {
                            int check = Integer.parseInt(String.valueOf(fontInput.getText()));
                            if (check > 1000 || check < 0) {
                                showSnackbar(mActivity, R.string.invalid_entry);
                            } else {
                                mFontSize = check;
                                TextToPdfFragment.this.showFontSize();
                                showSnackbar(mActivity, R.string.font_size_changed);
                                if (cbSetDefault.isChecked()) {
                                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                                    editor.putInt(Constants.DEFAULT_FONT_SIZE_TEXT, mFontSize);
                                    editor.apply();
                                    mFontTitle = String.format(TextToPdfFragment.this.getString(R.string.edit_font_size),
                                            mSharedPreferences.getInt(Constants.DEFAULT_FONT_SIZE_TEXT,
                                                    Constants.DEFAULT_FONT_SIZE));
                                }
                            }
                        } catch (NumberFormatException e) {
                            showSnackbar(mActivity, R.string.invalid_entry);
                        }
                    }
                })
                .show();
    }

    private void showFontFamily() {
        mTextEnhancementOptionsEntityModelArrayList.get(1)
                .setName(getString(R.string.font_family_text) + mFontFamily.name());
        mTextEnhancementOptionsAdapter.notifyDataSetChanged();
    }

    private void showFontSize() {
        mTextEnhancementOptionsEntityModelArrayList.get(0)
                .setName(String.format(getString(R.string.font_size), String.valueOf(mFontSize)));
        mTextEnhancementOptionsAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.createtextpdf)
    public void openCreateTextPdf() {
        if (!mPermissionGranted) {
            getRuntimePermissions();
            return;
        }
        new MaterialDialog.Builder(mActivity)
                .title(R.string.creating_pdf)
                .content(R.string.enter_file_name)
                .input(getString(R.string.example), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (StringUtils.isEmpty(input)) {
                            showSnackbar(mActivity, R.string.snackbar_name_not_blank);
                        } else {
                            final String inputName = input.toString();
                            if (!mFileUtils.isFileExist(inputName + TextToPdfFragment.this.getString(R.string.pdf_ext))) {
                                TextToPdfFragment.this.createPdf(inputName);
                            } else {
                                MaterialDialog.Builder builder = createOverwriteDialog(mActivity);
                                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog12, @NonNull DialogAction which) {
                                        TextToPdfFragment.this.createPdf(inputName);
                                    }
                                })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog1, @NonNull DialogAction which) {
                                                TextToPdfFragment.this.openCreateTextPdf();
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                })
                .show();
    }

    private void createPdf(String mFilename) {
        String mPath = mSharedPreferences.getString(STORAGE_LOCATION,
                getDefaultStorageLocation());
        mPath = mPath + mFilename + mActivity.getString(R.string.pdf_ext);
        try {
            PDFUtils fileUtil = new PDFUtils(mActivity);
            fileUtil.createPdf(new TextToPDFOptionsModel(mFilename, PageSizeUtils.mPageSize, mPasswordProtected,
                    mPassword, mTextFileUri, mFontSize, mFontFamily), mFileExtension);
            final String finalMPath = mPath;
            getSnackbarwithAction(mActivity, R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFileUtils.openFile(finalMPath);
                        }
                    }).show();
            mTextView.setVisibility(View.GONE);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            showSnackbar(mActivity, R.string.error_occurred);
        } finally {
            mMorphButtonUtility.morphToGrey(mCreateTextPdf, mMorphButtonUtility.integer());
            mCreateTextPdf.setEnabled(false);
            mTextFileUri = null;
        }
    }

    @OnClick(R.id.selectFile)
    public void selectTextFile() {
        if (mButtonClicked == 0) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType(getString(R.string.text_type));
            intent.setType("*/*");
            String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword", getString(R.string.text_type)};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(
                        Intent.createChooser(intent, String.valueOf(R.string.select_file)),
                        mFileSelectCode);
            } catch (android.content.ActivityNotFoundException ex) {
                showSnackbar(mActivity, R.string.install_file_manager);
            }
            mButtonClicked = 1;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mButtonClicked = 0;
        switch (requestCode) {
            case mFileSelectCode:
                if (resultCode == RESULT_OK) {
                    mTextFileUri = data.getData();
                    showSnackbar(mActivity, R.string.text_file_selected);
                    String fileName = mFileUtils.getFileName(mTextFileUri);
                    if (fileName != null) {
                        if (fileName.endsWith(Constants.textExtension)) mFileExtension = Constants.textExtension;
                        else if (fileName.endsWith(Constants.docxExtension)) mFileExtension = Constants.docxExtension;
                        else if (fileName.endsWith(Constants.docExtension)) mFileExtension = Constants.docExtension;
                        else {
                            showSnackbar(mActivity, R.string.extension_not_supported);
                            return;
                        }
                    }
                    fileName = getString(R.string.text_file_name) + fileName;
                    mTextView.setText(fileName);
                    mTextView.setVisibility(View.VISIBLE);
                    mCreateTextPdf.setEnabled(true);
                    mMorphButtonUtility.morphToSquare(mCreateTextPdf, mMorphButtonUtility.integer());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mFileUtils = new FileUtils(mActivity);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length < 1)
            return;
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionGranted = true;
                    openCreateTextPdf();
                    showSnackbar(mActivity, R.string.snackbar_permissions_given);
                } else
                    showSnackbar(mActivity, R.string.snackbar_insufficient_permissions);
            }
        }
    }

    private void onPasswordAdded() {
        mTextEnhancementOptionsEntityModelArrayList.get(3)
                .setImage(mActivity.getResources().getDrawable(R.drawable.baseline_done_24));
        mTextEnhancementOptionsAdapter.notifyDataSetChanged();
    }

    private void onPasswordRemoved() {
        mTextEnhancementOptionsEntityModelArrayList.get(3)
                .setImage(mActivity.getResources().getDrawable(R.drawable.baseline_enhanced_encryption_24));
        mTextEnhancementOptionsAdapter.notifyDataSetChanged();
    }

    private void getRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT);
                return;
            }
            mPermissionGranted = true;
        }
    }
}
