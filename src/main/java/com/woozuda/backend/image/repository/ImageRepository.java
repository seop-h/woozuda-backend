package com.woozuda.backend.image.repository;

import com.woozuda.backend.image.entity.Image;
import com.woozuda.backend.image.type.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image,Long> {

    // ImageType 과 ConnectedId 가 일치하는 이미지를 찾는 쿼리
    public List<Image> findByImageTypeAndConnectedId(ImageType imageType, Long connectedId);

    // imageUrl이 일치하는 이미지를 찾는 쿼리 (select * from image where image_url IN (image_url1 , image_url2 , ....)
    public List<Image> findByImageUrlIn(List<String> imageUrls);

    // isLinkedToPost 가 true/false 인 이미지 찾기.
    public List<Image> findByIsLinkedToPost(Boolean isLinkedToPost);

    public Image findByImageUrl(String imageUrl);
}
