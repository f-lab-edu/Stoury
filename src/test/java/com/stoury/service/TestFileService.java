package com.stoury.service;

import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
@Profile("test")
public class TestFileService implements FileService {
    public static final String PATH_PREFIX = "/test/path/";

    @Override
    public String saveFile(MultipartFile file) {
        return PATH_PREFIX + file.hashCode();
    }

    @Override
    public void saveFilesAtPath(Pair<MultipartFile, String> reservedImagePaths) {
        // 지정된 경로에 이미지를 저장하는 로직
    }
}
