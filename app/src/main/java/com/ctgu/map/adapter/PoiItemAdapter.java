package com.ctgu.map.adapter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.ctgu.map.R;
import com.ctgu.map.activity.SearchActivity;
import com.ctgu.map.utils.Constants;

import java.util.List;

/**
 * FileName: PoiItemAdapter
 * Author: Admin
 * Date: 2020/12/5 11:24
 * Description: POI适配器，容纳搜索结果
 */

public class PoiItemAdapter extends RecyclerView.Adapter<PoiItemAdapter.ViewHolder>{

    private List<PoiItem> poiItemList;
    private SearchActivity activity;

    static class ViewHolder extends RecyclerView.ViewHolder{

        View poiItemView;
        TextView poiItemName;
        TextView poiItemAddress;

        public ViewHolder(View view){
            super(view);
            poiItemView=view;
            poiItemName= view.findViewById(R.id.name);
            poiItemAddress= view.findViewById(R.id.address);
        }
    }

    public PoiItemAdapter(List<PoiItem> poiItemList, SearchActivity activity){
        this.poiItemList=poiItemList;
        this.activity=activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip_poi,
                parent, false);
        final ViewHolder holder=new ViewHolder(view);
        holder.poiItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                PoiItem poiItem=poiItemList.get(position);
                Intent intent=new Intent();
                intent.putExtra("resultType", Constants.RESULT_POIITEM);
                intent.putExtra("result", poiItem);
                activity.setResult(AppCompatActivity.RESULT_OK, intent);
                activity.finish();
                System.out.println("点击了搜索结果"+poiItem);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PoiItem poiItem=poiItemList.get(position);
        holder.poiItemName.setText(poiItem.getTitle());
        holder.poiItemAddress.setText(poiItem.getSnippet());
    }

    @Override
    public int getItemCount() {
        return poiItemList.size();
    }
}
