package com.loror.lororUtil.image;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.loror.lororUtil.flyweight.ObjectPool;

import java.lang.ref.WeakReference;

public class GifAbleImageViewCallBack implements ImageUtilCallBack {

    private static final int tagKey = 3 << 24;

    private String tag;
    private int index;

    private final Context context;
    private final ImageView imageView;
    private final ImageUtilCallBack onLoadListener;
    private final BitmapConverter bitmapConverter;
    private final boolean isGif;
    private final int defaultImage, errorImage;
    private final boolean cachUseAnimation;
    private final Animation loadAnimation;

    GifAbleImageViewCallBack(Context context, ImageView imageView, ImageUtilCallBack onLoadListener, BitmapConverter bitmapConverter, boolean isGif,
                             int defaultImage, int errorImage, boolean cachUseAnimation, Animation loadAnimation) {
        this.context = context;
        this.imageView = imageView;
        this.onLoadListener = onLoadListener;
        this.bitmapConverter = bitmapConverter;
        this.isGif = isGif;
        this.defaultImage = defaultImage;
        this.errorImage = errorImage;
        this.cachUseAnimation = cachUseAnimation;
        this.loadAnimation = loadAnimation;
    }

    @Override
    public void onStart(ImageView imageView) {
        if (imageView != null) {
            tag = String.valueOf(imageView.getTag(tagKey));
            Animation animation = imageView.getAnimation();
            if (animation != null) {
                imageView.clearAnimation();
            }
            if (defaultImage != 0) {
                imageView.setImageResource(defaultImage);
            }
        }
        if (onLoadListener != null) {
            onLoadListener.onStart(imageView);
        }
    }

    private void loadGif(final Handler handler, final ReadImageResult readImageResult, final boolean local) {
        final int size = readImageResult.getCount();
        final boolean[] calledAnimation = {false};
        final WeakReference<Context> contextWeakReference = new WeakReference<Context>(context);
        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(imageView);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                ImageView imageView = weakReference.get();
                Context context = contextWeakReference.get();
                if (imageView == null || context == null) {
                    return;
                }
                boolean useful = tag.equals(imageView.getTag(tagKey));
                if (useful) {
                    if (readImageResult.isPause()) {
                        long delay = readImageResult.getFrame(index % size).delay;
                        handler.postDelayed(this, delay != 0 ? delay : 100);
                    } else {
                        if (context instanceof Activity) {
                            Activity activity = (Activity) context;
                            if (activity.isFinishing()) {
                                return;
                            }
                        }
                        if (index != 0 && index % size == 0 && !readImageResult.isRepeate()) {
                            return;
                        }
                        imageView.setImageBitmap(bitmapConverter == null ? readImageResult.getFrame(index % size).image : bitmapConverter.convert(context, readImageResult.getFrame(index % size).image));
                        if (index == 0 && (!local || cachUseAnimation) && !calledAnimation[0] && loadAnimation != null && imageView.getVisibility() == View.VISIBLE) {
                            calledAnimation[0] = true;
                            imageView.startAnimation(loadAnimation);
                        }
                        long delay = readImageResult.getFrame(index % size).delay;
                        handler.postDelayed(this, delay != 0 ? delay : 100);
                        index++;
                    }
                }
            }
        };
        runnable.run();
    }

    @Override
    public void onLoadCach(final ImageView imageView, final ReadImageResult readImageResult) {
        if (imageView != null) {
            if (readImageResult.getBitmap() == null) {
                if (errorImage != 0) {
                    imageView.setImageResource(errorImage);
                }
                return;
            }
            if (!isGif) {
                if (cachUseAnimation && loadAnimation != null && imageView.getVisibility() == View.VISIBLE) {
                    imageView.startAnimation(loadAnimation);
                }
                imageView.setImageBitmap(bitmapConverter == null ? readImageResult.getBitmap() : bitmapConverter.convert(context, readImageResult.getBitmap()));
            } else {
                final Handler handler = ObjectPool.getInstance().getHandler();
                if (readImageResult.getCount() > 0) {
                    loadGif(handler, readImageResult, true);
                } else {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (cachUseAnimation && loadAnimation != null && imageView.getVisibility() == View.VISIBLE) {
                                imageView.startAnimation(loadAnimation);
                            }
                            imageView.setImageBitmap(bitmapConverter == null ? readImageResult.getBitmap() : bitmapConverter.convert(context, readImageResult.getBitmap()));
                        }
                    });
                }
            }
        }
        if (onLoadListener != null) {
            onLoadListener.onLoadCach(imageView, readImageResult);
        }
    }

    @Override
    public void onFinish(final ImageView imageView, final ReadImageResult readImageResult) {
        if (imageView != null) {
            if (!isGif) {
                if (loadAnimation != null && imageView.getVisibility() == View.VISIBLE) {
                    imageView.startAnimation(loadAnimation);
                }
                imageView.setImageBitmap(bitmapConverter == null ? readImageResult.getBitmap() : bitmapConverter.convert(context, readImageResult.getBitmap()));
            } else {
                final Handler handler = ObjectPool.getInstance().getHandler();
                if (readImageResult.getCount() > 0) {
                    loadGif(handler, readImageResult, false);
                } else {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (loadAnimation != null && imageView.getVisibility() == View.VISIBLE) {
                                imageView.startAnimation(loadAnimation);
                            }
                            imageView.setImageBitmap(bitmapConverter == null ? readImageResult.getBitmap() : bitmapConverter.convert(context, readImageResult.getBitmap()));
                        }
                    });
                }
            }
        }
        if (onLoadListener != null) {
            onLoadListener.onFinish(imageView, readImageResult);
        }
    }

    @Override
    public void onFailed(ImageView imageView, ReadImageResult result) {
        if (imageView != null && errorImage != 0) {
            imageView.setImageResource(errorImage);
        }
        if (onLoadListener != null) {
            onLoadListener.onFailed(imageView, result);
        }
    }
}
