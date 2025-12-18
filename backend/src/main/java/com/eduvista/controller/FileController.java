package com.eduvista.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());
        
        String fileUrl = "/api/uploads/" + filename;
        
        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("filename", filename);
        
        return result;
    }
}

