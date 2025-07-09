package com.woozuda.backend.image;

import com.woozuda.backend.image.config.S3Client;
import com.woozuda.backend.image.entity.Image;
import com.woozuda.backend.image.repository.ImageRepository;
import com.woozuda.backend.image.service.ImageService;
import com.woozuda.backend.image.type.ImageType;
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
@Import(ImageService.class)
public class ImageServiceTest {

    @MockBean
    private AesEncryptor aesEncryptor;

    @MockBean
    private S3Client s3Client;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @BeforeEach
    void setUp(){
       imageRepository.deleteAll();

       List<Image> images = new ArrayList<>();
       images.add(Image.of("https://aaa.com", false));
       images.add(Image.of("https://bbb.com", false));
       images.add(Image.of("https://ccc.com", false));
       images.add(Image.of("https://ddd.com", false));
       images.add(Image.of("https://eee.com", false));

       imageRepository.saveAll(images);
    }
    // 테스트 해야 할 것
    // 1. 다이어리 / 일기의 생성
    @Test
    void 기본이미지_다이어리_생성() {

        // when (랜덤 이미지로 다이어리 생성)
        imageService.afterCreate(ImageType.DIARY, 1L, "https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");

        // then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 커스텀이미지_다이어리_생성(){
        // when (aaa.com 이라는 커스텀 이미지로 다이어리를 생성했다면)
        imageService.afterCreate(ImageType.DIARY, 3L, "https://aaa.com");

        // then
        //aaa.com 은 true 가 되어야 함
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1.getConnectedId()).isEqualTo(3L);
        assertThat(tempImage1.getImageType()).isEqualTo(ImageType.DIARY);
        assertThat(tempImage1.getIsLinkedToPost()).isEqualTo(true);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 이미지_없는_일기_생성(){
        // when (aaa.com 이라는 커스텀 이미지로 다이어리를 생성했다면)
        imageService.afterCreate(ImageType.NOTE, 3L, "<a> 하하하 </a> <p> 나는 p 에요 </p> <h2> 하하하 </h2> <h1> 나는 제목이에요 </h1>");

        // then
        //aaa.com 은 true 가 되어야 함
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 이미지_있는_일기_생성(){
        // when (aaa.com 이라는 커스텀 이미지로 다이어리를 생성했다면)
        imageService.afterCreate(ImageType.NOTE, 3L, "<a> 하하하 </a> <p> 나는 p 에요 </p> <img src=\"https://aaa.com\"> <h1> 나는 제목이에요 </h1>");

        // then
        //aaa.com 은 true 가 되어야 함

        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1.getConnectedId()).isEqualTo(3L);
        assertThat(tempImage1.getImageType()).isEqualTo(ImageType.NOTE);
        assertThat(tempImage1.getIsLinkedToPost()).isEqualTo(true);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    //2. 다이어리 / 일기의 수정
    @Test
    void 다이어리_기본이미지에서_기본이미지로_수정(){
        //when
        imageService.afterUpdate(ImageType.DIARY, 1L, "https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");

        //then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 다이어리_기본이미지에서_커스텀이지미지로_수정(){
        //when
        imageService.afterUpdate(ImageType.DIARY, 3L, "https://aaa.com");

        //then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1.getConnectedId()).isEqualTo(3L);
        assertThat(tempImage1.getImageType()).isEqualTo(ImageType.DIARY);
        assertThat(tempImage1.getIsLinkedToPost()).isEqualTo(true);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 다이어리_커스텀이미지에서_기본이미지로_수정(){
        //given ( 기존 다이어리는 id 가 3이고, aaa.com 을 표지로 한다고 가정)

        Image prevImage = imageRepository.findByImageUrl("https://aaa.com");
        prevImage.changeImageType(ImageType.DIARY);
        prevImage.changeConectedId(3L);
        prevImage.changeLinkedToPost(true);

        //when
        imageService.afterUpdate(ImageType.DIARY, 3L, "https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");

        //then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1).isEqualTo(null);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 일기_수정(){
        //given ( 기존 일기장 id 가 3이고, aaa.com , ccc.com 을 가지고 있던 일기 라고 가정)
        Image prevImage = imageRepository.findByImageUrl("https://aaa.com");
        prevImage.changeImageType(ImageType.NOTE);
        prevImage.changeConectedId(3L);
        prevImage.changeLinkedToPost(true);

        Image prevImage2 = imageRepository.findByImageUrl("https://ccc.com");
        prevImage.changeImageType(ImageType.NOTE);
        prevImage.changeConectedId(3L);
        prevImage.changeLinkedToPost(true);

        //when ( aaa.com 지우고 bbb.com, ddd.com 추가, ccc.com 은 그대로 )
        imageService.afterUpdate(ImageType.NOTE, 3L, "<a> 하하하 </a> <p> 나는 p 에요 </p> <img src=\"https://bbb.com\"> <h1> 나는 제목이에요 </h1>"
        +"<img src=\"https://ccc.com\"> <img src=\"https://ddd.com\">");

        //then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1).isEqualTo(null);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getConnectedId()).isEqualTo(3L);
        assertThat(tempImage2.getImageType()).isEqualTo(ImageType.NOTE);
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(true);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getConnectedId()).isEqualTo(3L);
        assertThat(tempImage3.getImageType()).isEqualTo(ImageType.NOTE);
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(true);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getConnectedId()).isEqualTo(3L);
        assertThat(tempImage4.getImageType()).isEqualTo(ImageType.NOTE);
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(true);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }


    //3. 다이어리 / 일기의 삭제
    @Test
    void 커스텀이미지_다이어리_삭제(){
        //given (다이어리 id 가 3이고 aaa.com 을 표지로 삼고 있음)
        Image prevImage = imageRepository.findByImageUrl("https://aaa.com");
        prevImage.changeImageType(ImageType.DIARY);
        prevImage.changeConectedId(3L);
        prevImage.changeLinkedToPost(true);

        //when ( aaa.com 지우고 bbb.com, ddd.com 추가, ccc.com 은 그대로 )
        imageService.afterDelete(ImageType.DIARY, 3L);

        //then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1).isEqualTo(null);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 기본이미지_다이어리_삭제(){
        //given

        //when ( aaa.com 지우고 bbb.com, ddd.com 추가, ccc.com 은 그대로 )
        imageService.afterDelete(ImageType.NOTE, 3L);

        //then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 이미지있는_일기_삭제(){
        //given (다이어리 id 가 3이고 aaa.com 을 표지로 삼고 있음)
        Image prevImage = imageRepository.findByImageUrl("https://aaa.com");
        prevImage.changeImageType(ImageType.NOTE);
        prevImage.changeConectedId(3L);
        prevImage.changeLinkedToPost(true);

        //when ( aaa.com 지우고 bbb.com, ddd.com 추가, ccc.com 은 그대로 )
        imageService.afterDelete(ImageType.NOTE, 3L);

        //then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1).isEqualTo(null);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

    @Test
    void 이미지없는_일기_삭제(){
        //given (다이어리 id 가 3이고 aaa.com 을 표지로 삼고 있음)

        //when ( aaa.com 지우고 bbb.com, ddd.com 추가, ccc.com 은 그대로 )
        imageService.afterDelete(ImageType.DIARY, 3L);

        //then
        Image tempImage1 = imageRepository.findByImageUrl("https://aaa.com");
        assertThat(tempImage1.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage2 = imageRepository.findByImageUrl("https://bbb.com");
        assertThat(tempImage2.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage3 = imageRepository.findByImageUrl("https://ccc.com");
        assertThat(tempImage3.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage4 = imageRepository.findByImageUrl("https://ddd.com");
        assertThat(tempImage4.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage5 = imageRepository.findByImageUrl("https://eee.com");
        assertThat(tempImage5.getIsLinkedToPost()).isEqualTo(false);

        Image tempImage6 = imageRepository.findByImageUrl("https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-3.jpg");
        assertThat(tempImage6).isEqualTo(null);
    }

}
