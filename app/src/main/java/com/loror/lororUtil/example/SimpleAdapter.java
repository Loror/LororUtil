package com.loror.lororUtil.example;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.loror.lororUtil.image.ImageUtil;
import com.loror.lororUtil.image.ImageUtilCallBack;
import com.loror.lororUtil.image.ReadImageResult;
import com.loror.lororUtil.view.Find;
import com.loror.lororUtil.view.ViewUtil;

import java.util.List;

public class SimpleAdapter extends BaseAdapter {
    private Context context;
    private List<Image> images;

    public SimpleAdapter(Context context, List<Image> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_simple, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
            ViewUtil.find(holder, convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageUtil.with(context)
                .setOnLoadListener(new ImageUtilCallBack() {
                    @Override
                    public void onStart(ImageView imageView) {
                        Log.e("TAG_", position + " == start");
                    }

                    @Override
                    public void onLoadCach(ImageView imageView, ReadImageResult result) {
                        Log.e("TAG_", position + " == cach." + result.getErrorCode());
                    }

                    @Override
                    public void onFinish(ImageView imageView, ReadImageResult result) {
                        Log.e("TAG_", position + " == finish." + result.getErrorCode());
                    }

                    @Override
                    public void onFailed(ImageView imageView, ReadImageResult result) {
                        Log.e("TAG_", position + " == fail");
                    }
                })
                .from(images.get(position).path)
                .to(holder.image)
                .setWidthLimit(300)
                .setErrorImage(R.mipmap.ic_launcher)
                .loadImage();
        return convertView;
    }

    class ViewHolder {
        @Find
        ImageView image;
    }
}
