package com.dabashou.system.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.system.service.FileService;
import com.dabashou.system.vo.FileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;

@Tag(name = "文件", description = "文件上传下载")
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public AjaxResult<FileVo> upload(@RequestParam("file") MultipartFile file) {
        return AjaxResult.ok(fileService.upload(file, SecurityUtil.requireCurrentUserId()));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        java.nio.file.Path filePath = Paths.get("./uploads/").resolve(fileName).normalize();
        Resource resource = new FileSystemResource(filePath.toFile());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
}
