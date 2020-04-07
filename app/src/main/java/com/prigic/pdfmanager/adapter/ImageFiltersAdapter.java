package com.prigic.pdfmanager.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.interfaces.OnFilterItemClickedListener;
import com.prigic.pdfmanager.model.FilterItemModel;
import com.prigic.pdfmanager.util.ImageUtils;

public class ImageFiltersAdapter extends RecyclerView.Adapter<ImageFiltersAdapter.ViewHolder> {

    private final ArrayList<FilterItemModel> mFilterItemModel;
    private final OnFilterItemClickedListener mOnFilterItemClickedListener;
    private final Context  mContext;

    public ImageFiltersAdapter(ArrayList<FilterItemModel> filterItemModels, Context context,
                               OnFilterItemClickedListener listener) {
        mFilterItemModel = filterItemModels;
        mContext = context;
        mOnFilterItemClickedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_filter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int imageid = mFilterItemModel.get(position).getImageId();
        Bitmap roundBitmap = BitmapFactory.decodeResource(mContext.getResources(), imageid);
        if (roundBitmap != null) {
            holder.img.setImageBitmap(ImageUtils.getRoundBitmap(roundBitmap));
        } else
            holder.img.setImageResource(imageid);
        holder.name.setText(mFilterItemModel.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mFilterItemModel.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.filter_preview)
        ImageView img;
        @BindView(R.id.filter_Name)
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnFilterItemClickedListener.onItemClick(view, getAdapterPosition());
        }
    }
}
