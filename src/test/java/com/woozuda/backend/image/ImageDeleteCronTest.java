package com.woozuda.backend.image;

import com.woozuda.backend.image.config.S3Client;
import com.woozuda.backend.image.cron.ImageDeleteTask;
import com.woozuda.backend.image.entity.Image;
import com.woozuda.backend.image.repository.ImageRepository;
import com.woozuda.backend.image.service.ImageService;
import com.woozuda.backend.note.entity.converter.AesEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ImageDeleteTask.class)
public class ImageDeleteCronTest {

    @MockBean
    private AesEncryptor aesEncryptor;

    @MockBean
    private ImageService imageService;

    @Autowired
    private ImageDeleteTask imageDeleteTask;

    @Autowired
    private ImageRepository imageRepository;

    @BeforeEach
    void setUp() {
        imageRepository.deleteAll();

        List<Image> images = new ArrayList<>();
        images.add(Image.of("https://kr.object.ncloudstorage.com/test/aaa.com", false));
        images.add(Image.of("https://kr.object.ncloudstorage.com/test/bbb.com", true));
        images.add(Image.of("https://kr.object.ncloudstorage.com/test/ccc.com", false));
        images.add(Image.of("https://kr.object.ncloudstorage.com/test/ddd.com", true));
        images.add(Image.of("https://kr.object.ncloudstorage.com/test/eee.com", false));

        imageRepository.saveAll(images);
    }

    @Test
    void 크론잡_테스트() {
        //when
        imageDeleteTask.cleanUpImages();

        //then
        assertThat(imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/test/aaa.com")).isEqualTo(null);
        assertThat(imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/test/ccc.com")).isEqualTo(null);
        assertThat(imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/test/eee.com")).isEqualTo(null);

        assertThat(imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/test/bbb.com").getIsLinkedToPost()).isEqualTo(true);
        assertThat(imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/test/ddd.com").getIsLinkedToPost()).isEqualTo(true);
    }
}
