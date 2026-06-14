package com.finalwork.soulcapsule;



import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

import android.widget.ImageView;

import android.widget.LinearLayout;

import android.widget.TextView;



import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;



import java.util.ArrayList;

import java.util.Calendar;

import java.util.Collections;
import java.util.List;



public class FootprintAdapter extends RecyclerView.Adapter<FootprintAdapter.FootprintViewHolder> {



    public interface OnItemClickListener {

        void onItemClick(FootprintItem item);

    }



    private final List<FootprintItem> items;

    private OnItemClickListener listener;



    public FootprintAdapter(List<FootprintItem> items) {

        this.items = items != null ? items : new ArrayList<>();

    }



    public void setOnItemClickListener(OnItemClickListener listener) {

        this.listener = listener;

    }



    public static List<FootprintItem> createMockData() {

        List<FootprintItem> data = new ArrayList<>();



        data.add(new FootprintItem(

                "2026年", "10.30", "周三", "21:30",

                buildTimestamp(2026, 10, 30, 21, 30),

                "心情好", true, "学习", "开心 骄傲",

                "今天完成了一个非常复杂的 3D 交互项目，还在寝室拼了一个可爱的模型，满满的成就感！",

                true,

                "你今天真的很棒！完成复杂项目需要专注和耐心，再加上手工拼装的乐趣，这种成就感值得好好珍藏。"

        ));



        data.add(new FootprintItem(

                "2026年", "10.30", "周三", "13:47",

                buildTimestamp(2026, 10, 30, 13, 47),

                "心情好", true, "天气", "舒服",

                "今天阳光很好，微风拂过脸颊，在校园里散步感觉特别放松。",

                false,

                null

        ));



        data.add(new FootprintItem(

                "2026年", "6.10", "周三", "08:29",

                buildTimestamp(2026, 6, 10, 8, 29),

                "心情一般", false, "工作", "麻木",

                "和家里人通了电话，聊了一些最近的压力，心情有些复杂。",

                false,

                "看见你在「工作」中感到「麻木」，我想先轻轻抱抱你。愿意把感受写下来，本身就是一种勇气。"

        ));



        data.add(new FootprintItem(

                "2026年", "6.10", "周三", "08:28",

                buildTimestamp(2026, 6, 10, 8, 28),

                "心情好", true, "学习", "压抑",

                "今天完成了一个非常复杂的 3D 交互项目，还在寝室拼了一个可爱的模型，满满的成就感！",

                false,

                "看见你在「学习」中感到「压抑」，我想先轻轻抱抱你。这种感受并不奇怪——当我们对一件事投入很多，却暂时看不到回报时，心里很容易积一层薄雾。"

        ));



        Collections.sort(data, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        return data;

    }



    private static long buildTimestamp(int year, int month, int day, int hour, int minute) {

        Calendar calendar = Calendar.getInstance();

        calendar.set(year, month - 1, day, hour, minute, 0);

        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();

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

        holder.viewMoodDot.setBackgroundResource(

                item.isGoodMood()

                        ? R.drawable.shape_mood_dot_green

                        : R.drawable.shape_mood_dot_orange

        );



        holder.tvReason.setText(item.getReason());

        holder.tvFeelings.setText(item.getFeelings());

        holder.tvContent.setText(item.getContent());



        if (item.hasImage()) {

            holder.ivImage.setVisibility(View.VISIBLE);

            holder.ivImage.setImageDrawable(null);

            holder.ivImage.setBackgroundResource(R.drawable.shape_footprint_image);

        } else {

            holder.ivImage.setVisibility(View.GONE);

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

