package com.prigic.pdfmanager.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.eftimoff.viewpagertransformers.DepthPageTransformer;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.adapter.PreviewAdapter;
import com.prigic.pdfmanager.adapter.PreviewImageOptionsAdapter;
import com.prigic.pdfmanager.model.PreviewImageOptionItemModel;
import com.prigic.pdfmanager.util.Constants;
import com.prigic.pdfmanager.util.ImageSortUtils;
import com.prigic.pdfmanager.util.ThemeUtils;

import static com.prigic.pdfmanager.util.Constants.PREVIEW_IMAGES;

public class PreviewActivity extends AppCompatActivity implements PreviewImageOptionsAdapter.OnItemClickListener {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private ArrayList<String> mImagesArrayList;
    private static final int INTENT_REQUEST_REARRANGE_IMAGE = 1;
    private PreviewAdapter mPreviewAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.setThemeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        ButterKnife.bind(this);
        Intent intent = getIntent();
        mImagesArrayList = intent.getStringArrayListExtra(PREVIEW_IMAGES);

        mViewPager = findViewById(R.id.viewpager);
        mPreviewAdapter = new PreviewAdapter(this, mImagesArrayList);
        mViewPager.setAdapter(mPreviewAdapter);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        showOptions();
    }

    private void showOptions() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        PreviewImageOptionsAdapter adapter = new PreviewImageOptionsAdapter(this, getOptions(),
                getApplicationContext());
        mRecyclerView.setAdapter(adapter);
    }

    private ArrayList<PreviewImageOptionItemModel> getOptions() {
        ArrayList<PreviewImageOptionItemModel> mOptions = new ArrayList<>();
        mOptions.add(new PreviewImageOptionItemModel(R.drawable.ic_rearrange, getString(R.string.rearrange_text)));
        mOptions.add(new PreviewImageOptionItemModel(R.drawable.ic_sort, getString(R.string.sort)));
        return mOptions;
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                startActivityForResult(RearrangeImagesActivity.getStartIntent(this, mImagesArrayList),
                        INTENT_REQUEST_REARRANGE_IMAGE);
                break;
            case 1:
                sortImages();
                break;
        }
    }

    private void sortImages() {
        new MaterialDialog.Builder(this)
                .title(R.string.sort_by_title)
                .items(R.array.sort_options_images)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        ImageSortUtils.performSortOperation(position, mImagesArrayList);
                        mPreviewAdapter.setData(new ArrayList<>(mImagesArrayList));
                        mViewPager.setAdapter(mPreviewAdapter);
                    }
                })
                .negativeText(R.string.cancel)
                .show();
    }

    private void passUris() {
        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra(Constants.RESULT, mImagesArrayList);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case INTENT_REQUEST_REARRANGE_IMAGE:
                try {
                    mImagesArrayList = data.getStringArrayListExtra(Constants.RESULT);
                    mPreviewAdapter.setData(mImagesArrayList);
                    mViewPager.setAdapter(mPreviewAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        passUris();
    }

    public static Intent getStartIntent(Context context, ArrayList<String> uris) {
        Intent intent = new Intent(context, PreviewActivity.class);
        intent.putExtra(PREVIEW_IMAGES, uris);
        return intent;
    }
}