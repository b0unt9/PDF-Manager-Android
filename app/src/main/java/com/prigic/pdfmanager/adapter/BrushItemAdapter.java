package com.prigic.pdfmanager.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.interfaces.OnItemClickListner;
import com.prigic.pdfmanager.model.BrushItemModel;

public class BrushItemAdapter extends RecyclerView.Adapter<BrushItemAdapter.BrushItemViewHolder> {

    private final Context mContext;
    private final OnItemClickListner mOnItemClickListner;
    private final List<BrushItemModel> mBrushItemModels;

    public BrushItemAdapter(Context context,
                            OnItemClickListner onItemClickListner,
                            List<BrushItemModel> brushItemModels) {
        mBrushItemModels = brushItemModels;
        mOnItemClickListner = onItemClickListner;
        mContext = context;
    }

    @NonNull
    @Override
    public BrushItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.brush_color_item, parent, false);
        return new BrushItemAdapter.BrushItemViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull BrushItemViewHolder holder, int position) {
        int color = mBrushItemModels.get(position).getColor();
        if (position == mBrushItemModels.size() - 1)
            holder.Doodlebutton.setBackground(mContext.getResources().getDrawable(color));
        else
            holder.Doodlebutton.setBackgroundColor(mContext.getResources().getColor(color));
    }

    @Override
    public int getItemCount() {
        return mBrushItemModels.size();
    }

    public class BrushItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.doodle_color)
        Button Doodlebutton;

        BrushItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnItemClickListner.onItemClick(getAdapterPosition());
        }
    }
}
