package com.woozuda.backend.image.entity;


import com.woozuda.backend.global.entity.BaseTimeEntity;
import com.woozuda.backend.image.type.ImageType;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name="image")
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_linked_to_post")
    private Boolean isLinkedToPost;

    @Column(name = "image_type")
    private ImageType imageType;

    @Column(name = "connected_id")
    private Long connectedId;

    private Image(String imageUrl, Boolean isLinkedToPost) {
        this.imageUrl = imageUrl;
        this.isLinkedToPost = isLinkedToPost;
    }

    public static Image of(String imageUrl, Boolean isLinkedToPost) {
        return new Image(imageUrl, isLinkedToPost);
    }

    public void changeLinkedToPost(Boolean isLinkedToPost) {
        this.isLinkedToPost = isLinkedToPost;
    }

    public void changeImageType(ImageType imageType){
        this.imageType = imageType;
    }

    public void changeConectedId(Long connectedId){
        this.connectedId = connectedId;
    }
}
