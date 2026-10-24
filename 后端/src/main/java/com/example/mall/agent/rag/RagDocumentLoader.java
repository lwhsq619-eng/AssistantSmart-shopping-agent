package com.example.mall.agent.rag;

import com.example.mall.agent.exception.AiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * RAG 文档加载器。
 * 当前直接支持 txt/md；PDF 解析预留 TODO，避免引入复杂依赖影响课程项目运行。
 */
@Component
public class RagDocumentLoader {
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    public String loadText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AiException(400, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AiException(400, "文件不能超过 10MB");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!(filename.endsWith(".txt") || filename.endsWith(".md"))) {
            if (filename.endsWith(".pdf")) {
                throw new AiException(400, "PDF 解析暂未启用，请先上传 txt 或 md 文件");
            }
            throw new AiException(400, "仅支持 txt、md 文件");
        }
        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            if (!StringUtils.hasText(text)) {
                throw new AiException(400, "文档文本为空");
            }
            return text;
        } catch (IOException e) {
            throw new AiException(500, "读取上传文件失败", e);
        }
    }

    public String loadClasspathText(String path) {
        try {
            String text = new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8).trim();
            if (!StringUtils.hasText(text)) {
                throw new AiException(400, "默认知识文档为空：" + path);
            }
            return text;
        } catch (IOException e) {
            throw new AiException(500, "读取默认知识文档失败：" + path, e);
        }
    }
}
