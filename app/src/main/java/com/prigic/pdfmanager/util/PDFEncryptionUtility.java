package com.prigic.pdfmanager.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.interfaces.DataSetChanged;

import static com.prigic.pdfmanager.util.Constants.MASTER_PWD_STRING;
import static com.prigic.pdfmanager.util.Constants.appName;
import static com.prigic.pdfmanager.util.StringUtils.getSnackbarwithAction;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class PDFEncryptionUtility {

    private final Activity mContext;
    private final FileUtils mFileUtils;
    private final MaterialDialog mDialog;
    private String mPassword;
    private SharedPreferences mSharedPrefs;

    public PDFEncryptionUtility(Activity context) {
        this.mContext = context;
        this.mFileUtils = new FileUtils(context);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.custom_dialog, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .build();
    }

    public void setPassword(final String filePath, final DataSetChanged dataSetChanged) {

        mDialog.setTitle(R.string.set_password);
        final View mPositiveAction = mDialog.getActionButton(DialogAction.POSITIVE);
        assert mDialog.getCustomView() != null;
        EditText mPasswordInput = mDialog.getCustomView().findViewById(R.id.password);
        mPasswordInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mPositiveAction.setEnabled(s.toString().trim().length() > 0);
                    }

                    @Override
                    public void afterTextChanged(Editable input) {
                        if (StringUtils.isEmpty(input))
                            showSnackbar(mContext, R.string.snackbar_password_cannot_be_blank);
                        else
                            mPassword = input.toString();
                    }
                });
        mDialog.show();
        mPositiveAction.setEnabled(false);
        mPositiveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String path = PDFEncryptionUtility.this.doEncryption(filePath, mPassword);
                    getSnackbarwithAction(mContext, R.string.snackbar_pdfCreated)
                            .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                                @Override
                                public void onClick(View v2) {
                                    mFileUtils.openFile(path);
                                }
                            }).show();
                    if (dataSetChanged != null)
                        dataSetChanged.updateDataset();
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                    showSnackbar(mContext, R.string.cannot_add_password);
                }
                mDialog.dismiss();
            }
        });
    }

    private String doEncryption(String path, String password) throws IOException, DocumentException {

        String masterpwd = mSharedPrefs.getString(MASTER_PWD_STRING, appName);
        String finalOutputFile = mFileUtils.getUniqueFileName(path.replace(mContext.getString(R.string.pdf_ext),
                mContext.getString(R.string.encrypted_file)));

        PdfReader reader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
        stamper.setEncryption(password.getBytes(), masterpwd.getBytes(),
                PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128);
        stamper.close();
        reader.close();
        new DatabaseHelper(mContext).insertRecord(finalOutputFile, mContext.getString(R.string.encrypted));
        return finalOutputFile;
    }


    private boolean isPDFEncrypted(final String file) {
        PdfReader reader;
        String ownerPass = mContext.getString(R.string.app_name);
        try {
            reader = new PdfReader(file, ownerPass.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        if (!reader.isEncrypted()) {
            showSnackbar(mContext, R.string.not_encrypted);
            return false;
        }
        return true;
    }


    public void removePassword(final String file,
                               final DataSetChanged dataSetChanged) {

        if (!isPDFEncrypted(file))
            return;

        final String[] input_password = new String[1];
        mDialog.setTitle(R.string.enter_password);
        final View mPositiveAction = mDialog.getActionButton(DialogAction.POSITIVE);
        final EditText mPasswordInput = Objects.requireNonNull(mDialog.getCustomView()).findViewById(R.id.password);
        TextView text = mDialog.getCustomView().findViewById(R.id.enter_password);
        text.setText(R.string.decrypt_message);
        mPasswordInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mPositiveAction.setEnabled(s.toString().trim().length() > 0);
                    }

                    @Override
                    public void afterTextChanged(Editable input) {
                        input_password[0] = input.toString();
                    }
                });
        mDialog.show();
        mPositiveAction.setEnabled(false);
        mPositiveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!PDFEncryptionUtility.this.removePasswordUsingDefMasterPAssword(file, dataSetChanged, input_password)) {
                    if (!PDFEncryptionUtility.this.removePasswordUsingInputMasterPAssword(file, dataSetChanged, input_password)) {
                        showSnackbar(mContext, R.string.master_password_changed);
                    }
                }
                mDialog.dismiss();
            }
        });
    }

    private boolean removePasswordUsingDefMasterPAssword(final String file,
                                                         final DataSetChanged dataSetChanged,
                                                         final String[] inputPassword) {
        String finalOutputFile;
        try {
            String masterpwd = mSharedPrefs.getString(MASTER_PWD_STRING, appName);
            PdfReader reader = new PdfReader(file, masterpwd.getBytes());
            byte[] password;
            finalOutputFile = mFileUtils.getUniqueFileName
                    (file.replace(mContext.getResources().getString(R.string.pdf_ext),
                            mContext.getString(R.string.decrypted_file)));
            password = reader.computeUserPassword();
            byte[] input = inputPassword[0].getBytes();
            if (Arrays.equals(input, password)) {
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
                stamper.close();
                reader.close();
                if (dataSetChanged != null)
                    dataSetChanged.updateDataset();
                new DatabaseHelper(mContext).insertRecord(finalOutputFile, mContext.getString(R.string.decrypted));
                final String filepath = finalOutputFile;
                getSnackbarwithAction(mContext, R.string.snackbar_pdfCreated)
                        .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                            @Override
                            public void onClick(View v2) {
                                mFileUtils.openFile(filepath);
                            }
                        }).show();
                return true;
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    private boolean removePasswordUsingInputMasterPAssword(final String file,
                                                           final DataSetChanged dataSetChanged,
                                                           final String[] inputPassword) {
        String finalOutputFile;
        try {
            PdfReader reader = new PdfReader(file, inputPassword[0].getBytes());
            finalOutputFile = mFileUtils.getUniqueFileName(
                    file.replace(mContext.getResources().getString(R.string.pdf_ext),
                            mContext.getString(R.string.decrypted_file)));
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
            stamper.close();
            reader.close();
            if (dataSetChanged != null)
                dataSetChanged.updateDataset();
            new DatabaseHelper(mContext).insertRecord(finalOutputFile, mContext.getString(R.string.decrypted));
            final String filepath = finalOutputFile;
            getSnackbarwithAction(mContext, R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction, new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            mFileUtils.openFile(filepath);
                        }
                    }).show();
            return true;

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
