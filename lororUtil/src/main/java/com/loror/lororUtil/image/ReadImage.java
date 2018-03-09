package com.loror.lororUtil.image;

public interface ReadImage {
    ReadImageResult readImage(String path, int widthLimit, boolean mutiCache);
}
