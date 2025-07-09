package com.woozuda.backend.diary.entity;

import com.woozuda.backend.account.entity.UserEntity;
import com.woozuda.backend.global.entity.BaseTimeEntity;
import com.woozuda.backend.note.entity.Note;
import com.woozuda.backend.tag.entity.Tag;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//TODO 같은 사용자 내 다이어리 이름 중복 X
@Entity
@Table(name = "diary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Diary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    private UserEntity user;

    @Column(name = "image_url", nullable = false)
    private String image;

    @Column(nullable = false)
    private String title;

//    private LocalDate startDate;

//    private LocalDate endDate;

//    @Column(nullable = false)
//    private Integer noteCount;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC")
    private final List<Note> noteList = new ArrayList<>();

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<DiaryTag> tagList = new ArrayList<>();

    private Diary(UserEntity user, String image, String title) {
        this.user = user;
        this.image = image;
        this.title = title;
//        this.noteCount = 0;
    }

    public static Diary of(UserEntity user, String image, String title) {
        return new Diary(user, image, title);
    }

    public void addTag(Tag tag) {
        DiaryTag diaryTag = DiaryTag.of(this, tag);
        tagList.add(diaryTag);
    }

    public void change(String title, List<Tag> tags, String imgUrl) {
        this.title = title;
        this.tagList.clear();
        this.tagList.clear();
        for (Tag tag : tags) {
            addTag(tag);
        }
        this.image = imgUrl;
    }

    /*public void addNote(LocalDate noteDate) {
        if (startDate == null || startDate.isAfter(noteDate)) {
            startDate = noteDate;
        }
        if (endDate == null || endDate.isBefore(noteDate)) {
            endDate = noteDate;
        }
        noteCount++;
    }*/

    /*public void updateDuration() {
        List<LocalDate> noteDates = new ArrayList<>(
                this.noteList.stream()
                        .map(Note::getDate)
                        .toList()
        );

        Collections.sort(noteDates);
        this.startDate = noteDates.getFirst();
        this.endDate = noteDates.getLast();
    }*/

    /*public void updateNoteInfo(List<Long> noteIdList) {
        log.info("noteIdList = {}", noteIdList);

        log.info("noteList size = {}", noteList.size());
        noteList.removeIf(note -> noteIdList.contains(note.getId()));
        log.info("noteList size = {}", noteList.size());

        noteList.sort(Comparator.comparing(Note::getDate));

        this.startDate = !noteList.isEmpty() ? noteList.getFirst().getDate() : null;
        this.endDate = !noteList.isEmpty() ? noteList.getLast().getDate() : null;
        this.noteCount = noteList.size();
    }*/
}
