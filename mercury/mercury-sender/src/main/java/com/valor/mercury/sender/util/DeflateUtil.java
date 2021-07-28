package com.valor.mercury.sender.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static java.util.zip.Deflater.BEST_COMPRESSION;

public class DeflateUtil {

    /**
     * 将字符串压缩
     *
     * @param str
     * @return
     */
    public static byte[] compressStr(String str) throws Exception {
        byte[] input = str.getBytes("UTF-8");
        Deflater deflater = new Deflater(BEST_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();
        byte[] bytes = new byte[1024];
        int len = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (!deflater.finished()) {
            len = deflater.deflate(bytes);
            outputStream.write(bytes, 0, len);
        }
        deflater.end();
        outputStream.close();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return outputStream.toByteArray();
    }

    public static String unCompress(byte[] inputByte) throws IOException {
        int len = 0;
        Inflater inflater = new Inflater();
        inflater.setInput(inputByte);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] outByte = new byte[1024];
        try {
            while (!inflater.finished()) {
                // 解压缩并将解压缩后的内容输出到字节输出流bos中
                len = inflater.inflate(outByte);
                if (len == 0) {
                    break;
                }
                outputStream.write(outByte, 0, len);
            }
            inflater.end();
        } catch (Exception e) {
        } finally {
            outputStream.close();
        }
        return new String(outputStream.toByteArray(), "UTF-8");
//        return Base64.encodeBase64String(outputStream.toByteArray());
    }
}
