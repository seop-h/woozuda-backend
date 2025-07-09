package com.woozuda.backend.image.cron;

import com.woozuda.backend.image.entity.Image;
import com.woozuda.backend.image.repository.ImageRepository;
import com.woozuda.backend.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ImageDeleteTask {

    private final ImageService imageService;
    private final ImageRepository imageRepository;

    @Scheduled(cron = "0 0 3 * * *") // 매일 오전 3시
    public void cleanUpImages() {
        // 게시물과 연결되지 않은 이미지 조회
        List<Image> deleteImages = imageRepository.findByIsLinkedToPost(false);
        List<Long> deleteImageIds = new ArrayList<>();

        // 해당 이미지 object storage 에서 삭제
        for(Image deleteImage : deleteImages){
            // 지울 이미지들의 Id 추가 (db 에도 반영 위해서)
            deleteImageIds.add(deleteImage.getId());
            imageService.deleteImage(deleteImage.getImageUrl().split("/")[4]);
        }

        // db(이미지 테이블) 도 그에 맞추어 삭제
        imageRepository.deleteAllById(deleteImageIds);
    }
}
