package com.share.ylh.mediaplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.share.ylh.mediaplayer.R;
import com.share.ylh.mediaplayer.domain.FileInfo;
import com.share.ylh.mediaplayer.utils.ViewUtil;

import java.util.HashMap;
import java.util.List;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-14
 * Time: 12:51
 * FIXME
 */
public class MyAdapter extends BaseAdapter {

    private Context context;
    private List<FileInfo> data;
    private int resource;
    private LayoutInflater mLayoutInflater;

    public IshowShareView getIshowview() {
        return ishowview;
    }

    public void setIshowview(IshowShareView ishowview) {
        this.ishowview = ishowview;
    }

    private IshowShareView ishowview;


    public MyAdapter(Context context, List<FileInfo> data, int resource) {
        this.context = context;
        this.data = data;
        this.resource = resource;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(resource, null);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.fileName);
            viewHolder.fileType = (TextView) convertView.findViewById(R.id.fileType);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.menubtn);
            viewHolder.mimageview = (ImageView) convertView.findViewById(R.id.mimageview);
            final ImageView img_care = viewHolder.mimageview;
            img_care.setVisibility(View.GONE);
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IshowShareView ishowview = getIshowview();
                    ishowview.showView(position);
                }
            });

//            viewHolder.fileName.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    for(int i=0;i<data.size();i++){
//                        img_care.setVisibility(View.VISIBLE);
//                    }
//                }
//            });

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }
        final FileInfo music = data.get(position);

        viewHolder.fileName.setText(ViewUtil.getFileName(music.getFilePath()));//music.get("fileName")

        viewHolder.fileType.setText(ViewUtil.getFileType(music.getFilePath()));//music.get("fileType")

        return convertView;
    }


    private final class ViewHolder {
        public TextView fileName;
        public TextView fileType;
        public ImageView imageView;
        public ImageView mimageview;
    }
}
