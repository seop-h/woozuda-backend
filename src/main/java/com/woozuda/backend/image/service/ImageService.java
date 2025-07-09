package com.woozuda.backend.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.woozuda.backend.image.config.S3Client;
import com.woozuda.backend.image.dto.ImageDto;
import com.woozuda.backend.image.entity.Image;
import com.woozuda.backend.image.repository.ImageRepository;
import com.woozuda.backend.image.type.ImageType;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ImageService {

    private final S3Client s3Client;
    private final ImageRepository imageRepository;


    public ImageDto uploadImage(MultipartFile multipartFile) throws IOException {

        // 올릴 이미지가 첨부되지 않았을 경우
        if (multipartFile == null || multipartFile.isEmpty()) {
            log.info("Not Exist multipartFile");
            return null;
        }

        AmazonS3 s3 = s3Client.getAmazonS3();

        // 파일 이름 추출
        log.info("create file name");
        String filename = multipartFile.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        filename = uuid + "_" +filename;


        String bucketname = s3Client.getBucketName();

        // 메타데이터 생성
        log.info("create meta data");
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        // object storage 에 그림 올리기
        log.info("upload to s3");
        System.out.println(bucketname);
        s3.putObject(bucketname, filename, multipartFile.getInputStream(), metadata);

        // 올린 파일에 read 권한을 모든 유저에게 준다
        /*
        log.info("authorize read-only to all user");
        AccessControlList accessControlList = s3Client.getAmazonS3().getObjectAcl(bucketname, filename);
        accessControlList.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        s3Client.getAmazonS3().setObjectAcl(bucketname, filename, accessControlList);
        */

        String imgUrl = s3Client.getAmazonS3().getUrl(bucketname, filename).toString();

        imageRepository.save(Image.of(imgUrl, false));

        return new ImageDto(imgUrl);
    }

    public void deleteImage(String filename){
        AmazonS3 s3 = s3Client.getAmazonS3();
        s3.deleteObject(s3Client.getBucketName(), filename);
    }

    // urlContent 를 가공해주는 메서드
    public List<String> makeImgsUrl(ImageType imageType, String urlContent){

        List<String> imgs = new ArrayList<>();

        if(imageType == ImageType.NOTE){
            // Note는 content가 <p> 안녕하세요 </p> <img src ="https://~~> 처럼 되어 있어서 파싱 해야함
            Document doc = Jsoup.parse(urlContent);
            Elements imgTags = doc.select("img");

            for(Element img: imgTags){
                imgs.add(img.attr("src"));
            }
        }else if(imageType == ImageType.DIARY) {
            //다이어리 같은 경우에는 imgUrl 이 바로 들어있어서 배열에 넣기만 하면 됨
            imgs.add(urlContent);
        }

        return imgs;
    }

    // 다이어리 - 랜덤 이미지 생성 (기본 이미지 10장) 은 연산에서 제외 되어야 함 - 기본 제공 이미지들.
    // 현재 기본 이미지가 이미지 테이블에 저장되지는 않았으나, 혹시 모를 변수(프론트의 약간의 착오 등...) 를 위해 예외 처리 합니다.
    public List<String> excludeBasicImage(List<String> imageStrings){
        imageStrings.removeIf(image -> image.startsWith("https://kr.object.ncloudstorage.com/woozuda-image/random-image"));
        return imageStrings;
    }

    public void afterCreate(ImageType imageType, Long connectedId, String content){
        //content에서 imageUrl 추출( 리스트로 변환)
        List<String> imagesUrl = makeImgsUrl(imageType, content);

        //기본 이미지 - 주어지는 default 이미지는 제외 (필수는 아니나 혹시 해서 넣어둔 로직)
        imagesUrl = excludeBasicImage(imagesUrl);

        List<Image> images = imageRepository.findByImageUrlIn(imagesUrl);

        // 영속 상태의 엔티티라 save() 하지 않아도 반영됨
        for(Image image : images){
            image.changeLinkedToPost(true);
            image.changeImageType(imageType);
            image.changeConectedId(connectedId);
        }
    }

    public void afterUpdate(ImageType imageType, Long connectedId, String content){

        List<String> imagesUrl = makeImgsUrl(imageType, content);

        //기본 이미지 - 주어지는 default 이미지는 제외 (필수는 아니나 혹시 해서 넣어둔 로직)
        imagesUrl = excludeBasicImage(imagesUrl);

        // 이번에 업데이트할 글에 저장되어있는 그림들 리스트
        List<Image> afterImages = imageRepository.findByImageUrlIn(imagesUrl);
        boolean[] afterImagesBool = new boolean[afterImages.size()];

        //기존에 글(다이어리)에 저장되어있던 그림들을 불러옴
        List<Image> beforeImages = imageRepository.findByImageTypeAndConnectedId(imageType, connectedId);
        boolean[] beforeImagesBool = new boolean[beforeImages.size()];

        //수정 전과 수정 후에 둘다 있는 이미지는 true 처리 - 그리고 이 이미지들은 수정할 필요 없음.
        for(int i=0; i < beforeImages.size(); i++){
            for(int j=0; j < afterImages.size(); j++) {
                if (beforeImages.get(i).getImageUrl().equals(afterImages.get(j).getImageUrl())) {
                    beforeImagesBool[i] = true;
                    afterImagesBool[j] = true;
                }
            }
        }

        // 수정 전 이미지 리스트 중 false 인 것들은 이번에 수정되면서 삭제된 이미지 임 - 고로 삭제 처리 해줘야 함
        List<Long> deleteImagesId = new ArrayList<>();
        for(int i=0; i < beforeImages.size(); i++){
            if(!beforeImagesBool[i]){
                deleteImagesId.add(beforeImages.get(i).getId());
            }
        }
        imageRepository.deleteAllById(deleteImagesId);

        // 수정 후 이미지 리스트 중 false 인 것들은 이번에 수정되면서 추가된 이미지 임 - 고로 추가 처리 해줘야 함
        for(int j=0; j < afterImages.size(); j++){
            if(!afterImagesBool[j]){
                Image nowImage = afterImages.get(j);

                nowImage.changeLinkedToPost(true);
                nowImage.changeImageType(imageType);
                nowImage.changeConectedId(connectedId);
            }
        }

    }

    public void afterDelete(ImageType imageType, Long connectedId){
        List<Image> images = imageRepository.findByImageTypeAndConnectedId(imageType, connectedId);
        imageRepository.deleteAll(images);
    }

    @Transactional(readOnly = true)
    public ImageDto getRandomImage() {
        Random random = new Random();
        int imageNumber = random.nextInt(10) + 1;

        String imgUrl = "https://kr.object.ncloudstorage.com/woozuda-image/random-image/random-image-" + imageNumber + ".jpg";
        return new ImageDto(imgUrl);
    }
}
