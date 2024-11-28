package com.smart.neural.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class PdfUtils {

    public static final String SEPARATOR = "\\.";
    public static final List<String> IMG_SUFFIX = Arrays.asList("jpeg", "png");


    public static void imagesToPdf(String imageDirPath, String pdfPath) throws Exception {
        Asserts.notBlank(imageDirPath, "imageDirPath is null");
        Asserts.notBlank(pdfPath, "pdfPath is null");
        File imageDir = new File(imageDirPath);
        if (!imageDir.exists()) {
            throw new RuntimeException("截图文件夹不存在：" + imageDirPath);
        }

        String[] files = imageDir.list();
        if (files == null || files.length == 0) {
            throw new RuntimeException("截图不存在：" + imageDirPath);
        }

        List<String> images = Arrays.stream(files)
                .filter(e -> IMG_SUFFIX.contains(e.substring(e.lastIndexOf(".") + 1)))
                .sorted(Comparator.comparing(e -> Integer.valueOf(e.split(SEPARATOR)[0])))
                .toList();
        if (images.isEmpty()) {
            throw new RuntimeException("截图不存在：" + imageDirPath);
        }

        File pdfFile = new File(pdfPath);
        File parentDir = pdfFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new RuntimeException("无法创建目录：" + parentDir.getPath());
        }

        if (pdfFile.exists()) {
            if (!pdfFile.delete()) {
                throw new RuntimeException("删除已存在PDF文件失败：" + pdfPath);
            }
        }

        Document document = new Document(new Rectangle(PageSize.A4));
        try {
            document = new Document(PageSize.A4);
            document.setMargins(0, 0, 0, 0);
            PdfWriter.getInstance(document, new FileOutputStream(pdfPath));

            document.open();
            for (String image : images) {
                String path = imageDir + File.separator + image;
                Image img = Image.getInstance(path);

                // 检查图片大小并调整
                if (img.getWidth() > 14400 || img.getHeight() > 14400) {
                    // 按比例缩放图片
                    img.scaleToFit(14400, 14400);
                } else {
                    img.setAlignment(Image.ALIGN_CENTER);
                    img.scalePercent(100);
                }

                // 根据图片大小设置页面，一定要先设置页面，再newPage()，否则无效
                // 请注意，在 img.scaleToFit(14400, 14400)之后
                // 应使用 img.getScaledWidth() 和 img.getScaledHeight() 来获取缩放后的尺寸
                document.setPageSize(new Rectangle(img.getScaledWidth(), img.getScaledHeight()));
                document.newPage();
                document.add(img);
            }
        } catch (Exception e) {
            log.error("图片生成PDF错误：", e);
            throw new Exception(e);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // 获取当前项目路径
        String projectPath = System.getProperty("user.dir");
        String imageDirPath = projectPath + "/report/test_001/img/区级/310112";
        String pdfPath = "output.pdf";

        long start = System.currentTimeMillis();
        imagesToPdf(imageDirPath, pdfPath);
        System.out.println("cost: " + (System.currentTimeMillis() - start) + "ms");
    }
}