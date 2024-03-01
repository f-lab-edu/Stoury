package com.stoury.dto.feed;

import com.stoury.domain.GraphicContent;

public record GraphicContentResponse(Long id, String path) {
    public static GraphicContentResponse from(GraphicContent graphicContent) {
        return new GraphicContentResponse(graphicContent.getId(), graphicContent.getPath());
    }
}
