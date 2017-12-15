package com.netease.nim.demo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.demo.R;

import butterknife.InjectView;

/**
 * 联系人列表数据适配器
 * Created by huangjun on 2016/12/9.
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {


    @InjectView(R.id.cover_image)
    ImageView coverImage;
    @InjectView(R.id.tv_online_count)
    TextView tvOnlineCount;
    @InjectView(R.id.tv_name)
    TextView tvName;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room_item, null);
        final ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
  holder.coverImage.setImageResource(R.drawable.room_cover_50);
  holder.tvName.setText("哈哈");
  holder.tvOnlineCount.setText("20");
    }

    @Override
    public int getItemCount() {
        return 5;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView tvName;
         TextView tvOnlineCount;
        public ViewHolder(View itemView) {
            super(itemView);
            coverImage = (ImageView) itemView.findViewById(R.id.cover_image);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvOnlineCount = (TextView) itemView.findViewById(R.id.tv_online_count);
        }
    }
}
