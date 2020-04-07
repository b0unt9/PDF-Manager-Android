package com.prigic.pdfmanager.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.activity.MainActivity;
import com.prigic.pdfmanager.util.CardView;

import static com.prigic.pdfmanager.util.Constants.ADD_IMAGES;
import static com.prigic.pdfmanager.util.Constants.ADD_PWD;
import static com.prigic.pdfmanager.util.Constants.BUNDLE_DATA;
import static com.prigic.pdfmanager.util.Constants.COMPRESS_PDF;
import static com.prigic.pdfmanager.util.Constants.EXTRACT_IMAGES;
import static com.prigic.pdfmanager.util.Constants.PDF_TO_IMAGES;
import static com.prigic.pdfmanager.util.Constants.REMOVE_PAGES;
import static com.prigic.pdfmanager.util.Constants.REMOVE_PWd;
import static com.prigic.pdfmanager.util.Constants.REORDER_PAGES;
import static com.prigic.pdfmanager.util.DialogUtils.ADD_WATERMARK;
import static com.prigic.pdfmanager.util.DialogUtils.ROTATE_PAGES;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private Activity mActivity;
    @BindView(R.id.images_to_pdf)
    CardView imagesToPdf;
    @BindView(R.id.text_to_pdf)
    CardView textToPdf;
    @BindView(R.id.view_files)
    CardView viewFiles;
    @BindView(R.id.view_history)
    CardView viewHistory;
    @BindView(R.id.split_pdf)
    CardView splitPdf;
    @BindView(R.id.merge_pdf)
    CardView mergePdf;
    @BindView(R.id.compress_pdf)
    CardView compressPdf;
    @BindView(R.id.remove_pages)
    CardView removePages;
    @BindView(R.id.rearrange_pages)
    CardView rearrangePages;
    @BindView(R.id.extract_images)
    CardView extractImages;
    @BindView(R.id.pdf_to_images)
    CardView mPdfToImages;
    @BindView(R.id.add_password)
    CardView addPassword;
    @BindView(R.id.remove_password)
    CardView removePassword;
    @BindView(R.id.rotate_pages)
    CardView rotatePdf;
    @BindView(R.id.add_watermark)
    CardView addWatermark;
    @BindView(R.id.add_images)
    CardView addImages;
    @BindView(R.id.remove_duplicates_pages_pdf)
    CardView removeDuplicatePages;
    @BindView(R.id.invert_pdf)
    CardView invertPdf;

    private HashMap<Integer, Integer> mFragmentPositionMap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootview);




        fillMap();
        imagesToPdf.setOnClickListener(this);
        textToPdf.setOnClickListener(this);
        viewFiles.setOnClickListener(this);
        viewHistory.setOnClickListener(this);
        splitPdf.setOnClickListener(this);
        mergePdf.setOnClickListener(this);
        compressPdf.setOnClickListener(this);
        removePages.setOnClickListener(this);
        rearrangePages.setOnClickListener(this);
        extractImages.setOnClickListener(this);
        mPdfToImages.setOnClickListener(this);
        addPassword.setOnClickListener(this);
        removePassword.setOnClickListener(this);
        rotatePdf.setOnClickListener(this);
        addWatermark.setOnClickListener(this);
        addImages.setOnClickListener(this);
        removeDuplicatePages.setOnClickListener(this);
        invertPdf.setOnClickListener(this);
        return rootview;
    }

    private void fillMap() {
        mFragmentPositionMap = new HashMap<>();
        mFragmentPositionMap.put(R.id.images_to_pdf, 1);
        mFragmentPositionMap.put(R.id.view_files, 2);
        mFragmentPositionMap.put(R.id.rotate_pages, 3);
        mFragmentPositionMap.put(R.id.add_watermark, 3);
        mFragmentPositionMap.put(R.id.merge_pdf, 4);
        mFragmentPositionMap.put(R.id.split_pdf, 4);
        mFragmentPositionMap.put(R.id.text_to_pdf, 1);
        mFragmentPositionMap.put(R.id.compress_pdf, 4);
        mFragmentPositionMap.put(R.id.remove_pages, 5);
        mFragmentPositionMap.put(R.id.rearrange_pages, 5);
        mFragmentPositionMap.put(R.id.extract_images, 5);
        mFragmentPositionMap.put(R.id.view_history, 2);
        mFragmentPositionMap.put(R.id.pdf_to_images, 5);
        mFragmentPositionMap.put(R.id.add_password, 3);
        mFragmentPositionMap.put(R.id.remove_password, 3);
        mFragmentPositionMap.put(R.id.add_images, 3);
        mFragmentPositionMap.put(R.id.remove_duplicates_pages_pdf, 4);
        mFragmentPositionMap.put(R.id.invert_pdf, 4);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    private void setNavigationViewSelection(int index) {
        if (mActivity instanceof MainActivity)
            ((MainActivity) mActivity).setNavigationViewSelection(index);
    }

    @Override
    public void onClick(View v) {

        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        Bundle bundle = new Bundle();
        highLightNavigationDrawerItem(v);

        switch (v.getId()) {
            case R.id.images_to_pdf:
                fragment = new ImageToPdfFragment();
                break;
            case R.id.text_to_pdf:
                fragment = new TextToPdfFragment();
                break;
            case R.id.view_files:
                fragment = new ViewFilesFragment();
                break;
            case R.id.view_history:
                fragment = new HistoryFragment();
                break;
            case R.id.merge_pdf:
                fragment = new MergeFilesFragment();
                break;
            case R.id.split_pdf:
                fragment = new SplitFilesFragment();
                break;
            case R.id.compress_pdf:
                fragment = new RemovePagesFragment();
                bundle.putString(BUNDLE_DATA, COMPRESS_PDF);
                fragment.setArguments(bundle);
                break;
            case R.id.extract_images:
                fragment = new PdfToImageFragment();
                bundle.putString(BUNDLE_DATA, EXTRACT_IMAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.pdf_to_images:
                fragment = new PdfToImageFragment();
                bundle.putString(BUNDLE_DATA, PDF_TO_IMAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.remove_pages:
                fragment = new RemovePagesFragment();
                bundle.putString(BUNDLE_DATA, REMOVE_PAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.rearrange_pages:
                fragment = new RemovePagesFragment();
                bundle.putString(BUNDLE_DATA, REORDER_PAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.add_password:
                fragment = new RemovePagesFragment();
                bundle.putString(BUNDLE_DATA, ADD_PWD);
                fragment.setArguments(bundle);
                break;
            case R.id.remove_password:
                fragment = new RemovePagesFragment();
                bundle.putString(BUNDLE_DATA, REMOVE_PWd);
                fragment.setArguments(bundle);
                break;
            case R.id.rotate_pages:
                fragment = new ViewFilesFragment();
                bundle.putInt(BUNDLE_DATA, ROTATE_PAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.add_watermark:
                fragment = new ViewFilesFragment();
                bundle.putInt(BUNDLE_DATA, ADD_WATERMARK);
                fragment.setArguments(bundle);
                break;
            case R.id.add_images:
                fragment = new AddImagesFragment();
                bundle.putString(BUNDLE_DATA, ADD_IMAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.remove_duplicates_pages_pdf:
                fragment = new RemoveDuplicatePagesFragment();
                break;
            case R.id.invert_pdf:
                fragment = new InvertPdfFragment();
                break;
        }

        try {
            if (fragment != null && fragmentManager != null)
                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void highLightNavigationDrawerItem(View v) {
        if (mFragmentPositionMap.containsKey(v.getId())) {
            setNavigationViewSelection(mFragmentPositionMap.get(v.getId()));
        }
    }
}
