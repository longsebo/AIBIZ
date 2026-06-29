package com.ruoyi.flowable.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flowable.domain.SysProcessAttachment;
import com.ruoyi.flowable.service.ISysProcessAttachmentService;
import com.ruoyi.flowable.service.impl.DocumentConverterService;

@RestController
@RequestMapping("/flowable/attachment")
public class SysProcessAttachmentController extends BaseController {

    @Autowired
    private ISysProcessAttachmentService attachmentService;

    @Autowired
    private DocumentConverterService documentConverterService;

    @Value("${ruoyi.profile}")
    private String profile;

    @GetMapping("/list/{processInstanceId}")
    public AjaxResult list(@PathVariable String processInstanceId) {
        List<SysProcessAttachment> list = attachmentService.selectAttachmentsByProcessInstanceId(processInstanceId);
        for (SysProcessAttachment attachment : list) {
            attachment.setFileType(documentConverterService.getPreviewType(attachment.getFileName()));
            String filePath = attachment.getFilePath();
            if (filePath != null && !filePath.startsWith("/profile/")) {
                attachment.setFilePath("/profile/" + filePath);
            }
        }
        return success(list);
    }

    @GetMapping("/preview/{id}")
    public void preview(@PathVariable Long id, HttpServletResponse response) throws Exception {
        SysProcessAttachment attachment = attachmentService.selectAttachmentById(id);
        
        if (attachment == null) {
            response.sendError(404, "附件不存在");
            return;
        }

        String fileName = attachment.getFileName();
        String filePath = profile + "/" + attachment.getFilePath();
        
        File file = new File(filePath);
        if (!file.exists()) {
            response.sendError(404, "文件不存在");
            return;
        }

        String previewType = documentConverterService.getPreviewType(fileName);
        
        if ("html".equals(previewType)) {
            String html = documentConverterService.convertToHtml(filePath);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(html);
        } else if ("image".equals(previewType)) {
            response.setContentType("image/" + fileName.substring(fileName.lastIndexOf('.') + 1));
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
        } else if ("pdf".equals(previewType)) {
            response.setContentType("application/pdf");
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
        } else {
            downloadFile(response, file, fileName);
        }
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) throws Exception {
        SysProcessAttachment attachment = attachmentService.selectAttachmentById(id);

        if (attachment == null) {
            response.sendError(404, "附件不存在");
            return;
        }

        String fileName = attachment.getFileName();
        String filePath = profile + "/" + attachment.getFilePath();

        File file = new File(filePath);
        if (!file.exists()) {
            response.sendError(404, "文件不存在");
            return;
        }

        downloadFile(response, file, fileName);
    }

    /**
     * 基于文件路径的预览接口（用于未保存到数据库的临时文件）
     */
    @GetMapping("/previewByPath")
    public void previewByPath(@RequestParam String filePath, @RequestParam String fileName, HttpServletResponse response) throws Exception {
        // 安全检查：防止路径遍历攻击
        String normalizedPath = normalizeFilePath(filePath);
        if (normalizedPath == null) {
            response.sendError(400, "非法文件路径");
            return;
        }

        String fullPath = profile + "/" + normalizedPath;
        File file = new File(fullPath);
        if (!file.exists()) {
            response.sendError(404, "文件不存在");
            return;
        }

        String previewType = documentConverterService.getPreviewType(fileName);

        if ("html".equals(previewType)) {
            String html = documentConverterService.convertToHtml(fullPath);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(html);
        } else if ("image".equals(previewType)) {
            response.setContentType("image/" + fileName.substring(fileName.lastIndexOf('.') + 1));
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
        } else if ("pdf".equals(previewType)) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
        } else {
            downloadFile(response, file, fileName);
        }
    }

    /**
     * 基于文件路径的下载接口（用于未保存到数据库的临时文件）
     */
    @GetMapping("/downloadByPath")
    public void downloadByPath(@RequestParam String filePath, @RequestParam String fileName, HttpServletResponse response) throws Exception {
        // 安全检查：防止路径遍历攻击
        String normalizedPath = normalizeFilePath(filePath);
        if (normalizedPath == null) {
            response.sendError(400, "非法文件路径");
            return;
        }

        String fullPath = profile + "/" + normalizedPath;
        File file = new File(fullPath);
        if (!file.exists()) {
            response.sendError(404, "文件不存在");
            return;
        }

        downloadFile(response, file, fileName);
    }

    /**
     * 规范化文件路径，防止路径遍历攻击
     */
    private String normalizeFilePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        // 移除开头的/profile/或profile/前缀
        if (filePath.startsWith("/profile/")) {
            filePath = filePath.substring("/profile/".length());
        } else if (filePath.startsWith("profile/")) {
            filePath = filePath.substring("profile/".length());
        }
        // 防止路径遍历攻击
        if (filePath.contains("..") || filePath.contains(":") || filePath.startsWith("/")) {
            return null;
        }
        return filePath;
    }

    private void downloadFile(HttpServletResponse response, File file, String fileName) throws Exception {
        response.setContentType("application/octet-stream");
        response.setContentLength((int) file.length());
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
            .replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
    }
}