package studio.greeks.vilomoyu.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 文件工具
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>Zhao.Wu</a>
 * @description studio.greeks.vilomoyu.util vilomoyu
 * @date 2020/9/2 0002 21:52
 */
@Slf4j
@UtilityClass
public class FileUtil {
    /**
     * 获取某文件夹下某些文件的行
     * @param dir 目录结构
     * @param filter 文件过滤器
     * @throws IOException 关闭异常时抛出
     */
    public void readDir(File dir, FileFilter filter, Consumer<Stream<String>> consumer) throws IOException {
        List<File> files = new LinkedList<>();

        if (dir.isFile()) {
            if (null==filter || filter.accept(dir)) {
                files.add(dir);
            }
        } else {
            files.addAll(listFiles(dir, filter, true));
        }

        if (files.isEmpty()) {
            return;
        }

        Vector<FileInputStream> vector = new Vector<>();
        for (File file : files) {
            vector.add(new FileInputStream(file));
        }

        SequenceInputStream sis = new SequenceInputStream(vector.elements());
        BufferedReader reader = new BufferedReader(new InputStreamReader(sis));
        consumer.accept(reader.lines());
        for (FileInputStream fileInputStream : vector) {
            try {
                fileInputStream.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public List<File> listFiles(File dir, FileFilter filter, boolean recursion) {
        if (!dir.isDirectory()) {
            return Collections.emptyList();
        }
        List<File> fileList = new LinkedList<>();

        File[] allFiles = dir.listFiles();
        if (null != allFiles) {
            for (File file : allFiles) {
                if (file.isFile()) {
                    if (null == filter || filter.accept(file)) {
                        fileList.add(file);
                    }
                }
                if (recursion && file.isDirectory()) {
                    fileList.addAll(listFiles(file, filter, true));
                }
            }
        }
        return fileList;
    }

    public static void main(String[] args) throws IOException {
        FileUtil.readDir(new File("D:\\Document\\智能保全系统\\现场日志\\20200513"),
                file -> file.getName().endsWith(".log"),
                stream -> stream.forEach(System.out::println));
    }
}
