package com.dabashou.system.service;

import com.dabashou.system.vo.FileVo;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileVo upload(MultipartFile file, Long userId);
}
