package com.loror.lororUtil.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loror.lororUtil.image.BitmapConverter;
import com.loror.lororUtil.image.BitmapUtil;
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
                .from(images.get(position).path)
                .to(holder.image)
//                .setWidthLimit(300) //可控制生成bitmap宽度，默认尝试获取控件宽度
                .setErrorImage(R.mipmap.ic_launcher)
//                .setBitmapConverter(new BitmapConverter() {
//                    @Override
//                    public Bitmap convert(Context context, Bitmap original) {
//                        return BitmapUtil.centerRoundCorner(original);
//                    }
//                }) //可设置图片加载到控件前的处理
//                .setOnLoadListener(new ImageUtilCallBack() {
//                    @Override
//                    public void onStart(ImageView imageView) {
//
//                    }
//
//                    @Override
//                    public void onLoadCach(ImageView imageView, ReadImageResult result) {
//
//                    }
//
//                    @Override
//                    public void onFinish(ImageView imageView, ReadImageResult result) {
//
//                    }
//
//                    @Override
//                    public void onFailed(ImageView imageView, ReadImageResult result) {
//
//                    }
//                }) //可监听图片加载生命周期
                .loadImage();
        holder.text.setText("id:" + images.get(position).id);
        return convertView;
    }

    class ViewHolder {
        @Find
        ImageView image;
        @Find
        TextView text;
    }
}
