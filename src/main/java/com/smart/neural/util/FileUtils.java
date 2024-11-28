package com.smart.neural.util;

import org.apache.http.util.Asserts;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

    /**
     * 删除文件或者文件夹及内全部文件
     *
     * @param fileName 文件路径
     */
    public static void deleteFile(String fileName) throws IOException {
        Asserts.notBlank(fileName, "fileName is blank");
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                // 如果是目录，遍历删除
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                // 如果是文件，直接删除
                Files.delete(path);
            }
        } else {
            throw new NoSuchFileException("Path does not exist: " + path);
        }
    }

}
