package com.jokerliang.socket_netty_demo.device;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 文件切割工具类
 *
 * @author Logan
 * @createDate 2019-04-25
 * @version 1.0.0
 *
 */
public class FileSplitUtils {

    /**
     * 默认切割大小
     */
    public static final int DEFAULT_SIZE = 500;

    /**
     * 切割指定源文件，并输出到指定目录，按默认大小切割
     *
     * @param srcFile 指定要切割的源文件
     * @param outputDir 指定输出目录
     * @throws IOException 有异常时抛出，由调用者处理
     */
    public static void split(File srcFile, String outputDir) throws IOException {
        //split(srcFile, outputDir, DEFAULT_SIZE);
    }

    /**
     * 切割指定源文件，并输出到指定目录
     *
     * @param srcFile 指定要切割的源文件
     * @param size 切割大小
     * @throws IOException 有异常时抛出，由调用者处理
     */
    public static LinkedList<byte[]> split(File srcFile, int size) throws IOException {
        try (
                FileInputStream inputStream = FileUtils.openInputStream(srcFile);
        ) {

            LinkedList<byte[]> bytes = new LinkedList<>();


            long chunkFileNum = srcFile.length()%size==0?srcFile.length()/size:srcFile.length()/size+1;

            for (int i = 0; i < chunkFileNum; i++) {
                byte[] buffer;
                if (i == chunkFileNum - 1) {
                    // 算最后一个的大小
                    int lastSize = (int) (srcFile.length() - (i  * size));
                    buffer = new byte[lastSize];
                } else {
                    buffer = new byte[size];
                }
                IOUtils.read(inputStream, buffer);
                bytes.add(buffer);
            }

            return bytes;
        }

    }

    /**
     * 聚形碎片
     *
     * @param inputDir 碎片输入目录
     * @param destFile 聚形目标文件
     * @throws IOException 有异常时抛出，由调用者处理
     */
    public static void combinateFormChips(String inputDir, File destFile) throws IOException {
        File inputPath = FileUtils.getFile(inputDir);
        String[] files = inputPath.list();

        try (
                FileOutputStream outputStream = FileUtils.openOutputStream(destFile);
        ) {

            // 按文件名排序
            Arrays.sort(files);
            for (String fileName : files) {
                File file = FileUtils.getFile(inputPath, fileName);
                byte[] data = FileUtils.readFileToByteArray(file);

                IOUtils.write(data, outputStream);
            }
        }
    }

}
