package com.prigic.pdfmanager.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;

import com.prigic.pdfmanager.R;

import static com.prigic.pdfmanager.util.Constants.DEFAULT_PAGE_SIZE;
import static com.prigic.pdfmanager.util.Constants.DEFAULT_PAGE_SIZE_TEXT;
import static com.prigic.pdfmanager.util.DialogUtils.createCustomDialogWithoutContent;

public class PageSizeUtils {

    private final Context mActivity;
    private final SharedPreferences mSharedPreferences;
    public static String mPageSize;
    private final String mDefaultPageSize;
    private final HashMap<Integer, Integer> mPageSizeToString;

    public PageSizeUtils(Context mActivity) {
        this.mActivity = mActivity;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mDefaultPageSize = mSharedPreferences.getString(Constants.DEFAULT_PAGE_SIZE_TEXT,
                DEFAULT_PAGE_SIZE);
        mPageSize = mSharedPreferences.getString(DEFAULT_PAGE_SIZE_TEXT, DEFAULT_PAGE_SIZE);
        mPageSizeToString = new HashMap<>();
        mPageSizeToString.put(R.id.page_size_default, R.string.a4);
        mPageSizeToString.put(R.id.page_size_legal, R.string.legal);
        mPageSizeToString.put(R.id.page_size_executive, R.string.executive);
        mPageSizeToString.put(R.id.page_size_ledger, R.string.ledger);
        mPageSizeToString.put(R.id.page_size_tabloid, R.string.tabloid);
        mPageSizeToString.put(R.id.page_size_letter, R.string.letter);
    }


    private String getPageSize(int selectionId, String spinnerAValue, String spinnerBValue) {
        String stringPageSize;
        switch (selectionId) {
            case R.id.page_size_a0_a10:
                stringPageSize = spinnerAValue;
                mPageSize = stringPageSize.substring(0, stringPageSize.indexOf(" "));
                break;
            case R.id.page_size_b0_b10:
                stringPageSize = spinnerBValue;
                mPageSize = stringPageSize.substring(0, stringPageSize.indexOf(" "));
                break;
            default:
                mPageSize = mActivity.getString(mPageSizeToString.get(selectionId));

        }
        return mPageSize;
    }


    public MaterialDialog showPageSizeDialog(boolean saveValue) {
        MaterialDialog materialDialog = getPageSizeDialog(saveValue);

        View view = materialDialog.getCustomView();
        RadioGroup radioGroup = view.findViewById(R.id.radio_group_page_size);
        Spinner spinnerA = view.findViewById(R.id.spinner_page_size_a0_a10);
        Spinner spinnerB = view.findViewById(R.id.spinner_page_size_b0_b10);
        RadioButton radioButtonDefault = view.findViewById(R.id.page_size_default);
        radioButtonDefault.setText(String.format(mActivity.getString(R.string.default_page_size), mDefaultPageSize));

        if (saveValue)
            view.findViewById(R.id.set_as_default).setVisibility(View.GONE);

        if (mPageSize.equals(mDefaultPageSize)) {
            radioGroup.check(R.id.page_size_default);
        } else if (mPageSize.startsWith("A")) {
            radioGroup.check(R.id.page_size_a0_a10);
            spinnerA.setSelection(java.lang.Integer.parseInt(mPageSize.substring(1)));
        } else if (mPageSize.startsWith("B")) {
            radioGroup.check(R.id.page_size_b0_b10);
            spinnerB.setSelection(java.lang.Integer.parseInt(mPageSize.substring(1)));
        } else {
            Integer key = getKey(mPageSizeToString, mPageSize);
            if (key != null)
                radioGroup.check(key);
        }
        materialDialog.show();
        return materialDialog;
    }

    private MaterialDialog getPageSizeDialog(final boolean saveValue) {
        MaterialDialog.Builder builder = createCustomDialogWithoutContent((Activity) mActivity,
                R.string.set_page_size_text);
        return builder.customView(R.layout.set_page_size_dialog, true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog1, @NonNull DialogAction which) {
                        View view = dialog1.getCustomView();
                        RadioGroup radioGroup = view.findViewById(R.id.radio_group_page_size);
                        int selectedId = radioGroup.getCheckedRadioButtonId();
                        Spinner spinnerA = view.findViewById(R.id.spinner_page_size_a0_a10);
                        Spinner spinnerB = view.findViewById(R.id.spinner_page_size_b0_b10);
                        mPageSize = PageSizeUtils.this.getPageSize(selectedId, spinnerA.getSelectedItem().toString(),
                                spinnerB.getSelectedItem().toString());
                        CheckBox mSetAsDefault = view.findViewById(R.id.set_as_default);
                        if (saveValue || mSetAsDefault.isChecked()) {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(Constants.DEFAULT_PAGE_SIZE_TEXT, mPageSize);
                            editor.apply();
                        }
                    }
                }).build();
    }

    private Integer getKey(HashMap<Integer, Integer> map, String value) {
        for (HashMap.Entry<Integer, Integer> entry : map.entrySet()) {
            if (value.equals(mActivity.getString(entry.getValue()))) {
                return entry.getKey();
            }
        }
        return null;
    }
}