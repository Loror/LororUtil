package com.loror.lororUtil.http.okhttp;

import androidx.annotation.NonNull;

import com.loror.lororUtil.http.Actuator;
import com.loror.lororUtil.http.ProgressListener;

import java.io.IOException;

import okhttp3.MultipartBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ProgressRequestBody extends RequestBody {
    private final RequestBody requestBody;
    private final ProgressListener listener;
    private final Actuator callbackActuator;

    public ProgressRequestBody(RequestBody requestBody, ProgressListener listener, Actuator callbackActuator) {
        this.requestBody = requestBody;
        this.listener = listener;
        this.callbackActuator = callbackActuator;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType(); // 返回contentType
    }

    @Override
    public long contentLength() throws IOException {
        if (requestBody instanceof MultipartBody) {
            MultipartBody multipartBody = (MultipartBody) requestBody;
            return multipartBody.contentLength();
        }
        return super.contentLength();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        BufferedSink bufferedSink = Okio.buffer(new ForwardingSink(sink) {
            private long bytesWritten = 0L;
            private long contentLength = 0L;
            private long lastTime = 0L;
            private int speed = 0;

            @Override
            public void write(@NonNull Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                    lastTime = System.currentTimeMillis();
                }
                if (contentLength == 0 || listener == null) {
                    return;
                }
                long now = System.currentTimeMillis();
                final long timeGo = now - lastTime;
                bytesWritten += byteCount;
                speed += (int) byteCount;
                if (timeGo > 30) {
                    final float progress = (float) (bytesWritten * 1.0 / contentLength * 100);
                    final int finalSpeed = speed;
                    if (callbackActuator != null) {
                        callbackActuator.run(() -> listener.transing(progress, (int) (finalSpeed * 1000L / timeGo), contentLength));
                    } else {
                        listener.transing(progress, (int) (finalSpeed * 1000L / timeGo), contentLength);
                    }
                    speed = 0;
                    lastTime = now;
                }
            }
        });

        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }
}
