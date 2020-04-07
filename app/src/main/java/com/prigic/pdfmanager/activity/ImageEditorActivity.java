package com.prigic.pdfmanager.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.adapter.BrushItemAdapter;
import com.prigic.pdfmanager.adapter.ImageFiltersAdapter;
import com.prigic.pdfmanager.interfaces.OnFilterItemClickedListener;
import com.prigic.pdfmanager.interfaces.OnItemClickListner;
import com.prigic.pdfmanager.model.BrushItemModel;
import com.prigic.pdfmanager.model.FilterItemModel;
import com.prigic.pdfmanager.util.ThemeUtils;

import static com.prigic.pdfmanager.util.BrushUtils.getBrushItems;
import static com.prigic.pdfmanager.util.Constants.IMAGE_EDITOR_KEY;
import static com.prigic.pdfmanager.util.Constants.RESULT;
import static com.prigic.pdfmanager.util.ImageFilterUtils.getFiltersList;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class ImageEditorActivity extends AppCompatActivity implements OnFilterItemClickedListener, OnItemClickListner {

    private ArrayList<String> mFilterUris = new ArrayList<>();
    private final ArrayList<String> mImagepaths = new ArrayList<>();
    private ArrayList<FilterItemModel> mFilterItemModels;
    private ArrayList<BrushItemModel> mBrushItemModels;

    private int mDisplaySize;
    private int mCurrentImage;
    private String mFilterName;

    @BindView(R.id.nextimageButton)
    ImageView mNextButton;
    @BindView(R.id.imagecount)
    TextView mImgcount;
    @BindView(R.id.previousImageButton)
    ImageView mPreviousButton;
    @BindView(R.id.doodleSeekBar)
    SeekBar doodleSeekBar;
    @BindView(R.id.photoEditorView)
    PhotoEditorView mPhotoEditorView;
    @BindView(R.id.doodle_colors)
    RecyclerView brushColorsView;

    private boolean mClicked = true;
    private boolean mClickedFilter = false;
    private boolean mDoodleSelected = false;

    private PhotoEditor mPhotoEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.setThemeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        ButterKnife.bind(this);

        initValues();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    void initValues() {

        mFilterUris = getIntent().getStringArrayListExtra(IMAGE_EDITOR_KEY);
        mDisplaySize = mFilterUris.size();
        mFilterItemModels = getFiltersList(this);
        mBrushItemModels = getBrushItems();
        mImagepaths.addAll(mFilterUris);

        mPhotoEditorView.getSource()
                .setImageBitmap(BitmapFactory.decodeFile(mFilterUris.get(0)));
        changeAndShowImageCount(0);

        initRecyclerView();

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .build();
        doodleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPhotoEditor.setBrushSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mPhotoEditor.setBrushSize(30);
        mPhotoEditor.setBrushDrawingMode(false);
    }

    @OnClick(R.id.nextimageButton)
    void nextImg() {
        if (mClicked) {
            changeAndShowImageCount((mCurrentImage + 1) % mDisplaySize);
        } else
            showSnackbar(this, R.string.save_first);
    }

    @OnClick(R.id.previousImageButton)
    void previousImg() {
        if (mClicked) {
            changeAndShowImageCount((mCurrentImage - 1 % mDisplaySize));
        } else
            showSnackbar(this, R.string.save_first);
    }

    private void changeAndShowImageCount(int count) {

        if (count < 0 || count >= mDisplaySize)
            return;

        mCurrentImage = count % mDisplaySize;
        mPhotoEditorView.getSource()
                .setImageBitmap(BitmapFactory.decodeFile(mImagepaths.get(mCurrentImage)));
        mImgcount.setText(String.format(getString(R.string.showing_image), mCurrentImage + 1, mDisplaySize));
    }

    @OnClick(R.id.savecurrent)
    void saveC() {
        mClicked = true;
        if (mClickedFilter || mDoodleSelected) {
            saveCurrentImage();
            hideBrushEffect();
            mClickedFilter = false;
            mDoodleSelected = false;
        }
    }

    @OnClick(R.id.resetCurrent)
    void resetCurrent() {
        String originalPath = mFilterUris.get(mCurrentImage);
        mImagepaths.set(mCurrentImage, originalPath);
        mPhotoEditorView.getSource()
                .setImageBitmap(BitmapFactory.decodeFile(originalPath));
        mPhotoEditor.clearAllViews();
        mPhotoEditor.undo();
    }

    private void saveCurrentImage() {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/PDFfilter");
            dir.mkdirs();
            String fileName = String.format(getString(R.string.filter_file_name),
                    String.valueOf(System.currentTimeMillis()), mFilterName);
            File outFile = new File(dir, fileName);
            String imagePath = outFile.getAbsolutePath();

            mPhotoEditor.saveAsFile(imagePath, new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@NonNull String imagePath) {
                    mImagepaths.remove(mCurrentImage);
                    mImagepaths.add(mCurrentImage, imagePath);
                    mPhotoEditorView.getSource()
                            .setImageBitmap(BitmapFactory.decodeFile(mImagepaths.get(mCurrentImage)));
                    Toast.makeText(getApplicationContext(), R.string.filter_saved, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), R.string.filter_not_saved, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void initRecyclerView() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        ImageFiltersAdapter adapter = new ImageFiltersAdapter(mFilterItemModels, this, this);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        brushColorsView.setLayoutManager(layoutManager2);
        BrushItemAdapter brushItemAdapter = new BrushItemAdapter(this,
                this, mBrushItemModels);
        brushColorsView.setAdapter(brushItemAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        mClicked = position == 0;
        if (position == 1) {
            mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                    .setPinchTextScalable(true)
                    .build();
            if (doodleSeekBar.getVisibility() == View.GONE && brushColorsView.getVisibility() == View.GONE) {
                showBrushEffect();
            } else if (doodleSeekBar.getVisibility() == View.VISIBLE &&
                    brushColorsView.getVisibility() == View.VISIBLE) {
                hideBrushEffect();
            }
        } else {
            applyFilter(mFilterItemModels.get(position).getFilter());
        }
    }

    private void showBrushEffect() {
        mPhotoEditor.setBrushDrawingMode(true);
        doodleSeekBar.setVisibility(View.VISIBLE);
        brushColorsView.setVisibility(View.VISIBLE);
        mDoodleSelected = true;
    }

    private void hideBrushEffect() {
        mPhotoEditor.setBrushDrawingMode(false);
        doodleSeekBar.setVisibility(View.GONE);
        brushColorsView.setVisibility(View.GONE);
    }

    private void applyFilter(PhotoFilter filterType) {
        try {
            mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                    .setPinchTextScalable(true)
                    .build();
            mPhotoEditor.setFilterEffect(filterType);
            mFilterName = filterType.name();
            mClickedFilter = filterType != PhotoFilter.NONE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra(RESULT, mImagepaths);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onItemClick(int position) {
        int color = mBrushItemModels.get(position).getColor();
        if (position == mBrushItemModels.size() - 1) {
            final MaterialDialog colorpallete = new MaterialDialog.Builder(this)
                    .title(R.string.choose_color_text)
                    .customView(R.layout.color_pallete_layout, true)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .build();
            final View mPositiveAction = colorpallete.getActionButton(DialogAction.POSITIVE);
            final ColorPickerView colorPickerInput = colorpallete.getCustomView().findViewById(R.id.color_pallete);

            mPositiveAction.setEnabled(true);
            mPositiveAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        doodleSeekBar.setBackgroundColor(colorPickerInput.getColor());
                        mPhotoEditor.setBrushColor(colorPickerInput.getColor());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    colorpallete.dismiss();
                }
            });
            colorpallete.show();

        } else {
            doodleSeekBar.setBackgroundColor(this.getResources().getColor(color));
            mPhotoEditor.setBrushColor(this.getResources().getColor(color));
        }
    }

    public static Intent getStartIntent(Context context, ArrayList<String> uris) {
        Intent intent = new Intent(context, ImageEditorActivity.class);
        intent.putExtra(IMAGE_EDITOR_KEY, uris);
        return intent;
    }
}