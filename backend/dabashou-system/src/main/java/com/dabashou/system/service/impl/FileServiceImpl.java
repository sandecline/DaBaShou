package com.dabashou.system.service.impl;

import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.system.domain.SysFile;
import com.dabashou.system.mapper.SysFileMapper;
import com.dabashou.system.service.FileService;
import com.dabashou.system.vo.FileVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final SysFileMapper fileMapper;

    @Value("${dabashou.file.upload-dir:./uploads/}")
    private String uploadDir;

    public FileServiceImpl(SysFileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    @Override
    public FileVo upload(MultipartFile file, Long userId) {
        if (file.isEmpty()) throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件保存失败");
        }

        SysFile sysFile = new SysFile();
        sysFile.setOriginalName(originalName);
        sysFile.setFileName(fileName);
        sysFile.setFilePath(uploadDir + fileName);
        sysFile.setFileUrl("/api/v1/files/download/" + fileName);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(ext);
        sysFile.setMimeType(file.getContentType());
        sysFile.setUploadUserId(userId);
        sysFile.setStatus(1);
        fileMapper.insert(sysFile);

        FileVo vo = new FileVo();
        vo.setId(sysFile.getId());
        vo.setOriginalName(originalName);
        vo.setFileName(fileName);
        vo.setFileUrl(sysFile.getFileUrl());
        vo.setFileSize(file.getSize());
        vo.setMimeType(file.getContentType());
        vo.setCreateTime(sysFile.getCreateTime());
        return vo;
    }
}
