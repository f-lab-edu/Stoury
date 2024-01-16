package com.stoury.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile("test")
public class TestFileService implements FileService{
    public static final String PATH_PREFIX = "/test/path/";
    @Override
    public String saveFile(MultipartFile file) {
        return PATH_PREFIX + file.hashCode();
    }
}
