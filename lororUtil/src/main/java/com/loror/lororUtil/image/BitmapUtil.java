package com.loror.lororUtil.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.ImageColumns;

public class BitmapUtil {
    /**
     * 压缩图片分辨率
     */
    public static Bitmap compessBitmap(Bitmap res, int widthLimit, boolean recycleOld) {
        Bitmap bmp = null;
        try {
            int width = res.getWidth();
            int height = res.getHeight();
            double bl = width * 1.0 / height;
            if (widthLimit > 0 && width > widthLimit) {
                bmp = Bitmap.createScaledBitmap(res, widthLimit, (int) (widthLimit / bl), true);
                if (recycleOld) {
                    res.recycle();
                }
            } else {
                bmp = res;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    /**
     * 压缩图片分辨率
     */
    public static Bitmap compessBitmap(Context context, Uri uri, int widthLimit) {
        String path = uri.getPath();
        try {
            Bitmap bitmap;
            return (bitmap = compessBitmap(path, widthLimit)) != null ? bitmap
                    : compessBitmap(getRealImagePathByUri(context, uri), widthLimit);
        } catch (Exception e) {
            return compessBitmap(getRealImagePathByUri(context, uri), widthLimit);
        }
    }

    /**
     * 压缩图片分辨率
     */
    public static Bitmap compessBitmap(String path, int widthLimit) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);
        if (widthLimit > 0) {
            opt.inSampleSize = opt.outWidth < widthLimit ? 1 : opt.outWidth / widthLimit;
        }
        opt.inJustDecodeBounds = false;
        return compessBitmap(BitmapFactory.decodeFile(path, opt), widthLimit, false);
    }

    /**
     * 压缩图片分辨率
     */
    public static Bitmap compessBitmap(byte[] source, int widthLimit) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(source, 0, source.length, opt);
        if (widthLimit > 0) {
            opt.inSampleSize = opt.outWidth < widthLimit ? 1 : opt.outWidth / widthLimit;
        }
        opt.inJustDecodeBounds = false;
        return compessBitmap(BitmapFactory.decodeByteArray(source, 0, source.length, opt), widthLimit, false);
    }

    /**
     * 压缩图片体积
     */
    public static Bitmap compessBitmapSize(Bitmap bitmap, int size) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 压缩位图 ;
        int options = 100;
        while (b.toByteArray().length / size > 1024) { // 循环判断如果压缩后图片是否大于sizekb,大于继续压缩
            b.reset();// 重置b即清空b
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, b);// 这里压缩options%，把压缩后的数据存放到b中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(b.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, null);
    }

    /**
     * 获取图片旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        if (path == null) {
            return degree;
        }
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        return rotateBitmapByDegree(bm, degree, true);
    }

    /**
     * 旋转图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree, boolean recycleOld) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm && recycleOld) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * Drawable转换成Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 获取网络图片
     */
    public static Bitmap getBitmapByUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            InputStream is = conn.getInputStream();
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将bitmap存储到sd卡
     */
    public static boolean storeBitmap2SD(Bitmap bitmap, String path) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return false;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bs);// 压缩位图
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            byte[] out = bs.toByteArray();
            fos.write(out);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通过uri获取图片真实路径
     */
    public static String getRealImagePathByUri(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{ImageColumns.DATA}, null, null,
                    null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 获取图片类型
     */
    public static String getBitmapType(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options.outMimeType;
    }

    /**
     * 获取图片类型
     */
    public static String getBitmapType(byte[] source) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(source, 0, source.length, options);
        return options.outMimeType;
    }

    /**
     * 图片去色,返回灰度图片
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * 截取图片中心圆形图片
     */
    public static Bitmap centerRoundCorner(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int left = 0, top = 0, right = width, bottom = height;
        float roundPx = height / 2;
        if (width > height) {
            left = (width - height) / 2;
            top = 0;
            right = left + height;
            bottom = height;
        } else if (height > width) {
            left = 0;
            top = (height - width) / 2;
            right = width;
            bottom = top + width;
            roundPx = width / 2;
        }
        Bitmap output = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(left, top, right, bottom);
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    
    /**
     * 高斯模糊
     * Stack Blur v1.0 from
     * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
     */
     public static Bitmap fastBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
          bitmap = sentBitmap;
        } else {
          bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
          return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
          dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
          rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
          for (i = -radius; i <= radius; i++) {
            p = pix[yi + Math.min(wm, Math.max(i, 0))];
            sir = stack[i + radius];
            sir[0] = (p & 0xff0000) >> 16;
            sir[1] = (p & 0x00ff00) >> 8;
            sir[2] = (p & 0x0000ff);
            rbs = r1 - Math.abs(i);
            rsum += sir[0] * rbs;
            gsum += sir[1] * rbs;
            bsum += sir[2] * rbs;
            if (i > 0) {
              rinsum += sir[0];
              ginsum += sir[1];
              binsum += sir[2];
            } else {
              routsum += sir[0];
              goutsum += sir[1];
              boutsum += sir[2];
            }
          }
          stackpointer = radius;

          for (x = 0; x < w; x++) {

            r[yi] = dv[rsum];
            g[yi] = dv[gsum];
            b[yi] = dv[bsum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;

            stackstart = stackpointer - radius + div;
            sir = stack[stackstart % div];

            routsum -= sir[0];
            goutsum -= sir[1];
            boutsum -= sir[2];

            if (y == 0) {
              vmin[x] = Math.min(x + radius + 1, wm);
            }
            p = pix[yw + vmin[x]];

            sir[0] = (p & 0xff0000) >> 16;
            sir[1] = (p & 0x00ff00) >> 8;
            sir[2] = (p & 0x0000ff);

            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;

            stackpointer = (stackpointer + 1) % div;
            sir = stack[(stackpointer) % div];

            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];

            rinsum -= sir[0];
            ginsum -= sir[1];
            binsum -= sir[2];

            yi++;
          }
          yw += w;
        }
        for (x = 0; x < w; x++) {
          rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
          yp = -radius * w;
          for (i = -radius; i <= radius; i++) {
            yi = Math.max(0, yp) + x;

            sir = stack[i + radius];

            sir[0] = r[yi];
            sir[1] = g[yi];
            sir[2] = b[yi];

            rbs = r1 - Math.abs(i);

            rsum += r[yi] * rbs;
            gsum += g[yi] * rbs;
            bsum += b[yi] * rbs;

            if (i > 0) {
              rinsum += sir[0];
              ginsum += sir[1];
              binsum += sir[2];
            } else {
              routsum += sir[0];
              goutsum += sir[1];
              boutsum += sir[2];
            }

            if (i < hm) {
              yp += w;
            }
          }
          yi = x;
          stackpointer = radius;
          for (y = 0; y < h; y++) {
            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;

            stackstart = stackpointer - radius + div;
            sir = stack[stackstart % div];

            routsum -= sir[0];
            goutsum -= sir[1];
            boutsum -= sir[2];

            if (x == 0) {
              vmin[y] = Math.min(y + r1, hm) * w;
            }
            p = x + vmin[y];

            sir[0] = r[p];
            sir[1] = g[p];
            sir[2] = b[p];

            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;

            stackpointer = (stackpointer + 1) % div;
            sir = stack[stackpointer];

            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];

            rinsum -= sir[0];
            ginsum -= sir[1];
            binsum -= sir[2];

            yi += w;
          }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
  }
}
