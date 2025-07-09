package com.woozuda.backend.image.controller;

import com.woozuda.backend.image.dto.ImageDto;
import com.woozuda.backend.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/api/image")
@RequiredArgsConstructor
@RestController
@Slf4j
public class ImageController {

    private final ImageService imageService;

    // 이미지 단건 업로드
    @PostMapping("/upload")
    public ResponseEntity<ImageDto> uploadImage(MultipartFile multipartFile) throws IOException {
        log.info("이미지 업로드 시작");
        ImageDto responseDto = imageService.uploadImage(multipartFile);
        log.info("이미지 업로드 끝");
        return ResponseEntity.ok(responseDto);
    }

    /*
    @PostMapping("/delete")
    public void deleteImage(){
        String url = "https://kr.object.ncloudstorage.com/woozuda-image/test-dummy.png";
        imageService.deleteImage(url.split("/")[4]);
    }
    */

    // 랜덤 이미지 추출
    @GetMapping("/random")
    public ResponseEntity<ImageDto> getRandomImage(){
        ImageDto responseDto = imageService.getRandomImage();
        return ResponseEntity.ok(responseDto);
    }
}
