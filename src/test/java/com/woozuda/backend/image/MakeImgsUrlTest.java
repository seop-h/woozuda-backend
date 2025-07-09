package com.woozuda.backend.image;

import com.woozuda.backend.image.config.S3Client;
import com.woozuda.backend.image.repository.ImageRepository;
import com.woozuda.backend.image.service.ImageService;
import com.woozuda.backend.image.type.ImageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class MakeImgsUrlTest {

    @InjectMocks
    ImageService imageService;

    @Mock
    S3Client s3Client;

    @Mock
    ImageRepository imageRepository;

    @Test
    public void imageUrl_파서_테스트_노트()  {
        List<String> urls= imageService.makeImgsUrl(ImageType.NOTE, "<p> 나는 </p> <h1> 운동을 했다 </h1> <img src = \"https://aaa.com\"> " +
                "<p> 나는 </p> <h1> 잠을 잤다 </h1> <img src = \"https://bbb.com\">");

        assertThat(urls).contains("https://aaa.com", "https://bbb.com");
    }

    @Test
    public void imageUrl_파서_테스트_노트_없을때()  {
        List<String> urls= imageService.makeImgsUrl(ImageType.NOTE, "<p> 나는 </p> <h1> 운동을 했다 </h1>" +
                "<p> 나는 </p> <h1> 잠을 잤다 </h1>");

        assertThat(urls).isEmpty();
    }

    @Test
    public void imageUrl_파서_테스트_다이어리()  {
        List<String> urls= imageService.makeImgsUrl(ImageType.DIARY, "https://aaa.com");

        assertThat(urls).contains("https://aaa.com");
    }
}
