package com.finalwork.soulcapsule;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finalwork.soulcapsule.util.ImageLoaderHelper;
import com.finalwork.soulcapsule.util.MoodColorConstants;

import java.util.ArrayList;
import java.util.List;

public class FootprintAdapter extends RecyclerView.Adapter<FootprintAdapter.FootprintViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(FootprintItem item);
    }

    private final List<FootprintItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public FootprintAdapter() {
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateItems(List<FootprintItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FootprintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_footprint, parent, false);
        return new FootprintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FootprintViewHolder holder, int position) {
        FootprintItem item = items.get(position);

        boolean showDateHeader = position == 0
                || !item.getDateMonthDay().equals(items.get(position - 1).getDateMonthDay());
        holder.layoutDateHeader.setVisibility(showDateHeader ? View.VISIBLE : View.INVISIBLE);
        if (showDateHeader) {
            holder.tvYear.setText(item.getYear());
            holder.tvDateMonthDay.setText(item.getDateMonthDay());
            holder.tvWeekDay.setText(item.getWeekDay());
        }

        holder.tvTime.setText(item.getTime());
        holder.tvMoodStatus.setText(item.getMoodStatus());
        int dotColor = MoodColorConstants.getColorForScore(
                holder.itemView.getContext(), item.getMoodScore());
        GradientDrawable moodDot = new GradientDrawable();
        moodDot.setShape(GradientDrawable.OVAL);
        moodDot.setColor(dotColor);
        holder.viewMoodDot.setBackground(moodDot);

        holder.tvReason.setText(item.getReason());
        holder.tvFeelings.setText(item.getFeelings());
        holder.tvContent.setText(item.getContent());

        if (item.hasImage()) {
            holder.ivImage.setVisibility(View.VISIBLE);
            ImageLoaderHelper.loadRemote(
                    holder.itemView.getContext(), holder.ivImage, item.getImageUrl(), 8);
        } else {
            holder.ivImage.setVisibility(View.GONE);
            holder.ivImage.setImageDrawable(null);
        }

        if (item.hasAiReply()) {
            holder.layoutAiReply.setVisibility(View.VISIBLE);
            holder.tvAiReply.setText("小旅：" + item.getAiReply());
        } else {
            holder.layoutAiReply.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FootprintViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout layoutDateHeader;
        final TextView tvYear;
        final TextView tvDateMonthDay;
        final TextView tvWeekDay;
        final View viewMoodDot;
        final TextView tvTime;
        final TextView tvMoodStatus;
        final TextView tvReason;
        final TextView tvFeelings;
        final TextView tvContent;
        final ImageView ivImage;
        final LinearLayout layoutAiReply;
        final TextView tvAiReply;

        FootprintViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutDateHeader = itemView.findViewById(R.id.layout_date_header);
            tvYear = itemView.findViewById(R.id.tv_year);
            tvDateMonthDay = itemView.findViewById(R.id.tv_date_month_day);
            tvWeekDay = itemView.findViewById(R.id.tv_weekday);
            viewMoodDot = itemView.findViewById(R.id.view_mood_dot);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMoodStatus = itemView.findViewById(R.id.tv_mood_status);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvFeelings = itemView.findViewById(R.id.tv_feelings);
            tvContent = itemView.findViewById(R.id.tv_content);
            ivImage = itemView.findViewById(R.id.iv_image);
            layoutAiReply = itemView.findViewById(R.id.layout_ai_reply);
            tvAiReply = itemView.findViewById(R.id.tv_ai_reply);
        }
    }
}
