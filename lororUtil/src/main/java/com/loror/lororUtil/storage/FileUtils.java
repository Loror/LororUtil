package com.loror.lororUtil.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件管理
 */
public class FileUtils {

    /**
     * 保存文本到文件
     */
    public static boolean save(File file, String text) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(text.getBytes("utf-8"));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 拷贝文件
     */
    public static boolean copy(File from, File to) {
        try {
            FileInputStream inputStream = new FileInputStream(from);
            FileOutputStream outputStream = new FileOutputStream(to);
            byte[] temp = new byte[1024 * 100];
            int total;
            while ((total = inputStream.read(temp)) != -1) {
                outputStream.write(temp, 0, total);
            }
            inputStream.close();
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除文件（夹）
     */
    public static boolean delete(File file) {
        if (file.isFile()) {
            return file.delete();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                return file.delete();
            }
            for (File f : files) {
                if (!f.delete()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 读取文件内容
     */
    public static byte[] readFileToByteArray(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 获取单个文件的MD5值
     *
     * @param file 文件
     * @return md5
     */
    public static String getFileMD5(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(FileUtils.readFileToByteArray(file));
            byte[] b = md.digest();
            int d;
            for (byte value : b) {
                d = value;
                if (d < 0) {
                    d = value & 0xff;
                    // 与上一行效果等同
                    // i += 256;
                }
                if (d < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(d));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * 获取单个文件的MD5值
     *
     * @param file  文件
     * @param radix 位 16 32 64
     * @return md5
     */
    public static String getFileMD5(File file, int radix) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(radix);
    }

}
