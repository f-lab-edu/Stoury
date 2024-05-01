package com.stoury.service;

import com.stoury.domain.GraphicContent;
import com.stoury.event.GraphicSaveEvent;
import com.stoury.service.storage.StorageService;
import com.stoury.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GraphicContentService {
    @Value("${path-prefix}")
    public String pathPrefix;
    private final StorageService storageService;

    @Transactional
    public List<GraphicContent> createGraphicContents(List<MultipartFile> graphicContents) {
        List<GraphicContent> graphicContentList = new ArrayList<>();

        for (int i = 0; i < graphicContents.size(); i++) {
            MultipartFile file = graphicContents.get(i);

            String path = FileUtils.createFilePath(file, pathPrefix);

            graphicContentList.add(new GraphicContent(path, i));

            storageService.saveFileAtPath(file, Paths.get(path));
        }

        return graphicContentList;
    }
}
