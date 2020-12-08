package com.ctgu.map.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.help.Tip;
import com.ctgu.map.R;

import java.util.List;

/**
 * FileName: LogDetailAdapter
 * Author: Admin
 * Date: 2020/12/8 9:55
 * Description: 日志
 */
public class LogDetailAdapter  extends BaseAdapter {
    private Context mContext;
    private String[] logList;
    private LayoutInflater layoutInflater;


    public LogDetailAdapter (Context context, String[] logs) {
        mContext = context;
        logList=logs;
        System.out.println("适配器中的日志："+logList[0]);
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(logList.length!=0){
            return logList.length;
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(logList.length!=0){
            return getItemId(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        try {
            LogDetailAdapter.Holder holder;
            if (view == null) {
                holder = new LogDetailAdapter.Holder();
                view = layoutInflater.inflate(R.layout.item_log, null);
                holder.content = (TextView) view.findViewById(R.id.item_log_detail);
                view.setTag(holder);
            } else {
                holder = (LogDetailAdapter.Holder) view.getTag();
            }
            if (logList == null) {
                return view;
            }
            holder.content.setText(logList[i]);
        } catch (Throwable e) {
        }
        return view;
    }

    class Holder {
        TextView content;
    }
}
