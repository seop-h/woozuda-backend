package com.woozuda.backend.note.entity;

import com.woozuda.backend.global.entity.BaseTimeEntity;
import com.woozuda.backend.note.entity.converter.NoteContentConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "note_content")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteContent extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_content_id")
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", updatable = false, nullable = false)
    private Note note;

    @Column(name = "note_order", nullable = false)
    private Integer noteOrder;

    @Column(length = 2000, nullable = false)
//    @Convert(converter = NoteContentConverter.class)
    private String content; // 회고 부분 내용

    private NoteContent(Integer noteOrder, String content) {
        this.noteOrder = noteOrder;
        this.content = content;
    }

    public NoteContent(Note note, Integer noteOrder, String content) {
        this.note = note;
        this.noteOrder = noteOrder;
        this.content = content;
    }

    public static NoteContent of(Integer order, String content) {
        return new NoteContent(order, content);
    }

    public static NoteContent of(Note note, Integer order, String content) {
        return new NoteContent(note, order, content);
    }

    public void update(String content) {
        this.content = content;
    }
}
