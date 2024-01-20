package com.stoury.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SupportedFileType {
    JPG("/images", ".jpeg"), MP4("/videos", ".mp4"), OTHER("/other", "");
    private final String path;
    private final String extension;
}
