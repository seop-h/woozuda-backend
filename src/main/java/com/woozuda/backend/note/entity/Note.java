package com.woozuda.backend.note.entity;

import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.global.entity.BaseTimeEntity;
import com.woozuda.backend.note.entity.type.Visibility;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@Table(name = "note")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(length = 100, nullable = false)
    private Visibility visibility;

    @Column(name = "dtype", insertable = false, updatable = false) // 읽기 전용 필드
    private String dtype;

    //TODO OneToOne 으로 해도 될 듯
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<NoteContent> noteContents = new ArrayList<>();

    protected Note(Diary diary, String title, LocalDate date, Visibility visibility) {
        this.diary = diary;
        this.title = title;
        this.date = date;
        this.visibility = visibility;
    }

    public void addContent(NoteContent content) {
        this.noteContents.add(content);
        content.setNote(this);
    }

    protected void update(Diary foundDiary, String title, LocalDate date, List<String> content) {
        this.diary = foundDiary;
        this.title = title;
        this.date = date;

        if (noteContents.size() != content.size()) {
            throw new IllegalArgumentException("Note Content mismatch");
        }
        for (int i = 0; i < noteContents.size(); i++) {
            noteContents.get(i).update(content.get(i));
        }

//        foundDiary.updateDuration();
    }
}
