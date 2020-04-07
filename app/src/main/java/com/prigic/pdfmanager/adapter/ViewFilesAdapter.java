package com.prigic.pdfmanager.adapter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.util.DatabaseHelper;
import com.prigic.pdfmanager.interfaces.DataSetChanged;
import com.prigic.pdfmanager.interfaces.EmptyStateChangeListener;
import com.prigic.pdfmanager.interfaces.ItemSelectedListener;
import com.prigic.pdfmanager.model.PDFFileModel;
import com.prigic.pdfmanager.util.DirectoryUtils;
import com.prigic.pdfmanager.util.FileUtils;
import com.prigic.pdfmanager.util.PDFEncryptionUtility;
import com.prigic.pdfmanager.util.PDFUtils;
import com.prigic.pdfmanager.util.PopulateList;
import com.prigic.pdfmanager.util.WatermarkUtils;

import static com.prigic.pdfmanager.util.Constants.SORTING_INDEX;
import static com.prigic.pdfmanager.util.DialogUtils.createOverwriteDialog;
import static com.prigic.pdfmanager.util.FileSortUtils.NAME_INDEX;
import static com.prigic.pdfmanager.util.FileUtils.getFormattedDate;
import static com.prigic.pdfmanager.util.StringUtils.getSnackbarwithAction;
import static com.prigic.pdfmanager.util.StringUtils.showSnackbar;

public class ViewFilesAdapter extends RecyclerView.Adapter<ViewFilesAdapter.ViewFilesHolder>
        implements DataSetChanged, EmptyStateChangeListener {

    private final Activity mActivity;
    private final EmptyStateChangeListener mEmptyStateChangeListener;
    private final ItemSelectedListener mItemSelectedListener;
    private final ArrayList<Integer> mSelectedFiles;
    private final FileUtils mFileUtils;
    private final PDFUtils mPDFUtils;
    private final WatermarkUtils mWatermakrUtils;
    private final PDFEncryptionUtility mPDFEncryptionUtils;
    private final DatabaseHelper mDatabaseHelper;
    private final SharedPreferences mSharedPreferences;

    private List<PDFFileModel> mFileList;

    public ViewFilesAdapter(Activity activity,
                            List<PDFFileModel> feedItems,
                            EmptyStateChangeListener emptyStateChangeListener,
                            ItemSelectedListener itemSelectedListener) {
        this.mActivity = activity;
        this.mEmptyStateChangeListener = emptyStateChangeListener;
        this.mItemSelectedListener = itemSelectedListener;
        this.mFileList = feedItems;
        mSelectedFiles = new ArrayList<>();
        mFileUtils = new FileUtils(activity);
        mPDFUtils = new PDFUtils(activity);
        mPDFEncryptionUtils = new PDFEncryptionUtility(activity);
        mWatermakrUtils = new WatermarkUtils(activity);
        mDatabaseHelper = new DatabaseHelper(mActivity);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    @NonNull
    @Override
    public ViewFilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new ViewFilesHolder(itemView, mItemSelectedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewFilesHolder holder, final int pos) {
        final int position = holder.getAdapterPosition();
        final PDFFileModel pdfFileModel = mFileList.get(position);

        holder.mFilename.setText(pdfFileModel.getPdfFile().getName());
        holder.mFilesize.setText(FileUtils.getFormattedSize(pdfFileModel.getPdfFile()));
        holder.mFiledate.setText(getFormattedDate(pdfFileModel.getPdfFile()));
        holder.checkBox.setChecked(mSelectedFiles.contains(position));
        holder.mEncryptionImage.setVisibility(pdfFileModel.isEncrypted() ? View.VISIBLE : View.GONE);
        holder.mRipple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(mActivity)
                        .title(R.string.title)
                        .items(R.array.items)
                        .itemsIds(R.array.itemIds)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view1, int which, CharSequence text) {
                                ViewFilesAdapter.this.performOperation(which, position, pdfFileModel.getPdfFile());
                            }
                        })
                        .show();
                ViewFilesAdapter.this.notifyDataSetChanged();
            }
        });
    }


    private void performOperation(int index, int position, File file) {
        switch (index) {
            case 0:
                mFileUtils.openFile(file.getPath());
                break;

            case 1:
                deleteFile(file.getPath(), position);
                break;

            case 2:
                onRenameFileClick(position);
                break;

            case 3:
                mFileUtils.printFile(file);
                break;

            case 4:
                mPDFUtils.showDetails(file);
                break;

            case 5:
                mPDFEncryptionUtils.setPassword(file.getPath(), ViewFilesAdapter.this);
                break;

            case 6:
                mPDFEncryptionUtils.removePassword(file.getPath(), ViewFilesAdapter.this);
                break;

            case 7:
                mPDFUtils.rotatePages(file.getPath(), ViewFilesAdapter.this);
                break;

            case 8:
                mWatermakrUtils.setWatermark(file.getPath(), ViewFilesAdapter.this);
                break;
            case 9:
                mPDFUtils.setImages();
                break;
        }
    }

    public void checkAll() {
        mSelectedFiles.clear();
        for (int i = 0; i < mFileList.size(); i++)
            mSelectedFiles.add(i);
        notifyDataSetChanged();
    }

    public void unCheckAll() {
        mSelectedFiles.clear();
        notifyDataSetChanged();
        updateActionBarTitle();
    }

    private void updateActionBarTitle() {
        ActionBar actionBar = ((AppCompatActivity) mActivity).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewFilesHolder holder) {
        super.onViewRecycled(holder);
        holder.checkBox.setChecked(false);
    }

    public ArrayList<String> getSelectedFilePath() {
        ArrayList<String> filePathList = new ArrayList<>();
        for (int position : mSelectedFiles) {
            if (mFileList.size() > position)
                filePathList.add(mFileList.get(position).getPdfFile().getPath());
        }
        return filePathList;
    }

    @Override
    public int getItemCount() {
        return mFileList == null ? 0 : mFileList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<PDFFileModel> pdfFileModels) {
        mFileList = pdfFileModels;
        notifyDataSetChanged();
    }

    public PDFUtils getPDFUtils() {
        return mPDFUtils;
    }

    public boolean areItemsSelected() {
        return mSelectedFiles.size() > 0;
    }

    private void deleteFile(String name, int position) {

        if (position < 0 || position >= mFileList.size())
            return;

        final AtomicInteger undoClicked = new AtomicInteger();
        final File fdelete = new File(name);
        mFileList.remove(position);
        notifyDataSetChanged();
        getSnackbarwithAction(mActivity, R.string.snackbar_file_deleted).setAction(R.string.snackbar_undoAction, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFileList.size() == 0) {
                    mEmptyStateChangeListener.setEmptyStateInvisible();
                }
                ViewFilesAdapter.this.updateDataset();
                undoClicked.set(1);
            }
        }).addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (undoClicked.get() == 0) {
                    fdelete.delete();
                    mDatabaseHelper.insertRecord(fdelete.getAbsolutePath(),
                            mActivity.getString(R.string.deleted));
                }
            }
        }).show();
        if (mFileList.size() == 0)
            mEmptyStateChangeListener.setEmptyStateVisible();
    }

    public void deleteFiles() {

        for (int position : mSelectedFiles) {

            if (position >= mFileList.size())
                continue;

            String fileName = mFileList.get(position).getPdfFile().getPath();
            File fdelete = new File(fileName);
            mDatabaseHelper.insertRecord(fdelete.getAbsolutePath(), mActivity.getString(R.string.deleted));
            if (fdelete.exists() && !fdelete.delete())
                showSnackbar(mActivity, R.string.snackbar_file_not_deleted);
        }

        ArrayList<PDFFileModel> newList = new ArrayList<>();
        for (int position = 0; position < mFileList.size(); position++)
            if (!mSelectedFiles.contains(position))
                newList.add(mFileList.get(position));

        mSelectedFiles.clear();
        if (newList.size() == 0)
            mEmptyStateChangeListener.setEmptyStateVisible();

        setData(newList);
    }

    private void onRenameFileClick(final int position) {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.creating_pdf)
                .content(R.string.enter_file_name)
                .input(mActivity.getString(R.string.example), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, final CharSequence input) {
                        if (input == null || input.toString().trim().isEmpty())
                            showSnackbar(mActivity, R.string.snackbar_name_not_blank);
                        else {
                            if (!mFileUtils.isFileExist(input + mActivity.getString(R.string.pdf_ext))) {
                                ViewFilesAdapter.this.renameFile(position, input.toString());
                            } else {
                                MaterialDialog.Builder builder = createOverwriteDialog(mActivity);
                                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog2, @NonNull DialogAction which) {
                                        ViewFilesAdapter.this.renameFile(position, input.toString());
                                    }
                                })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog1, @NonNull DialogAction which) {
                                                ViewFilesAdapter.this.onRenameFileClick(position);
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                }).show();
    }

    private void renameFile(int position, String newName) {
        PDFFileModel pdfFileModel = mFileList.get(position);
        File oldfile = pdfFileModel.getPdfFile();
        String oldPath = oldfile.getPath();
        String newfilename = oldPath.substring(0, oldPath.lastIndexOf('/'))
                + "/" + newName + mActivity.getString(R.string.pdf_ext);
        File newfile = new File(newfilename);
        if (oldfile.renameTo(newfile)) {
            showSnackbar(mActivity, R.string.snackbar_file_renamed);
            pdfFileModel.setPdfFile(newfile);
            notifyDataSetChanged();
            mDatabaseHelper.insertRecord(newfilename, mActivity.getString(R.string.renamed));
        } else
            showSnackbar(mActivity, R.string.snackbar_file_not_renamed);
    }

    @Override
    public void updateDataset() {
        int index = mSharedPreferences.getInt(SORTING_INDEX, NAME_INDEX);
        new PopulateList(this, this,
                new DirectoryUtils(mActivity), index, null).execute();
    }

    @Override
    public void setEmptyStateVisible() {

    }

    @Override
    public void setEmptyStateInvisible() {

    }

    @Override
    public void showNoPermissionsView() {

    }

    @Override
    public void hideNoPermissionsView() {

    }

    public class ViewFilesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fileRipple)
        MaterialRippleLayout mRipple;
        @BindView(R.id.fileName)
        TextView mFilename;
        @BindView(R.id.checkbox)
        CheckBox checkBox;
        @BindView(R.id.fileDate)
        TextView mFiledate;
        @BindView(R.id.fileSize)
        TextView mFilesize;
        @BindView(R.id.encryptionImage)
        ImageView mEncryptionImage;

        ViewFilesHolder(View itemView, final ItemSelectedListener itemSelectedListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (!mSelectedFiles.contains(ViewFilesHolder.this.getAdapterPosition())) {
                            mSelectedFiles.add(ViewFilesHolder.this.getAdapterPosition());
                            itemSelectedListener.isSelected(true, mSelectedFiles.size());
                        }
                    } else
                        mSelectedFiles.remove(Integer.valueOf(ViewFilesHolder.this.getAdapterPosition()));
                    itemSelectedListener.isSelected(false, mSelectedFiles.size());
                }
            });
        }
    }
}