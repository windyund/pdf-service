package com.smart.neural.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.http.util.Asserts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
public class ZipUtils {

    /**
     * 压缩文件夹
     *
     * @param directory   文件夹路径
     * @param zipFilePath 压缩文件路径
     * @throws IOException IO异常
     */
    public static void zip(String directory, String zipFilePath) throws IOException {
        Asserts.notBlank(directory, "directory is null");
        Asserts.notBlank(zipFilePath, "zipFile is null");
        Path baseDirPath = Paths.get(directory);
        if (!baseDirPath.toFile().exists()) {
            throw new RuntimeException("压缩文件夹不存在: " + directory);
        }
        File zipFile = new File(zipFilePath);
        if (zipFile.exists()) {
            if (!zipFile.delete()) {
                throw new RuntimeException("删除已存在压缩文件失败: " + zipFilePath);
            }
        }

        try (ZipArchiveOutputStream archive = new ZipArchiveOutputStream(zipFile)) {
            try (Stream<Path> walk = Files.walk(baseDirPath)) {
                walk.forEach(path -> {
                    String entryName = baseDirPath.relativize(path).toString();
                    ZipArchiveEntry zipEntry = new ZipArchiveEntry(path.toFile(), entryName);
                    try {
                        archive.putArchiveEntry(zipEntry);
                        if (!Files.isDirectory(path)) {
                            Files.copy(path, archive);
                        }
                        archive.closeArchiveEntry();
                    } catch (IOException e) {
                        throw new RuntimeException("压缩文件失败: " + path, e);
                    }
                });
            }
            archive.finish();
        }
    }

    public static void main(String[] args) throws Exception {
        // 获取当前项目路径
        String projectPath = System.getProperty("user.dir");
        String directory = projectPath + File.separator + "src";
        String zipFile = "test.zip";

        long start = System.currentTimeMillis();
        ZipUtils.zip(directory, zipFile);
        System.out.println("cost: " + (System.currentTimeMillis() - start) + "ms");
    }
}
