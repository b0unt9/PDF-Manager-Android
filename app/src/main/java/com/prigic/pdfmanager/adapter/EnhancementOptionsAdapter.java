package com.prigic.pdfmanager.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.prigic.pdfmanager.R;
import com.prigic.pdfmanager.interfaces.OnItemClickListner;
import com.prigic.pdfmanager.model.EnhancementOptionsEntityModel;

public class EnhancementOptionsAdapter
        extends RecyclerView.Adapter<EnhancementOptionsAdapter.EnhancementOptionsViewHolder> {

    private final OnItemClickListner mOnItemClickListner;
    private final List<EnhancementOptionsEntityModel> mEnhancementOptionsEntityModelList;

    public EnhancementOptionsAdapter(OnItemClickListner mOnItemClickListner,
                                     List<EnhancementOptionsEntityModel> mEnhancementOptionsEntityModelList) {
        this.mOnItemClickListner = mOnItemClickListner;
        this.mEnhancementOptionsEntityModelList = mEnhancementOptionsEntityModelList;
    }

    @NonNull
    @Override
    public EnhancementOptionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_enhancement_option, parent, false);
        return new EnhancementOptionsViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull EnhancementOptionsViewHolder holder, int position) {
        holder.optionImage.setImageDrawable(mEnhancementOptionsEntityModelList.get(position).getImage());
        holder.optionName.setText(mEnhancementOptionsEntityModelList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mEnhancementOptionsEntityModelList.size();
    }

    public class EnhancementOptionsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.option_image)
        ImageView optionImage;
        @BindView(R.id.option_name)
        TextView optionName;

        EnhancementOptionsViewHolder(View itemView) {
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
