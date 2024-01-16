package com.stoury.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    default List<String> saveFiles(List<MultipartFile> files) {
        return files.stream()
                .map(this::saveFile)
                .toList();
    }

    String saveFile(MultipartFile file);
}
