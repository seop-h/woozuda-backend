package com.woozuda.backend.note.entity;

import com.woozuda.backend.diary.entity.Diary;
import com.woozuda.backend.note.entity.type.Feeling;
import com.woozuda.backend.note.entity.type.Season;
import com.woozuda.backend.note.entity.type.Visibility;
import com.woozuda.backend.note.entity.type.Weather;
import com.woozuda.backend.question.entity.Question;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@DiscriminatorValue("QUESTION")
@Table(name = "question_note")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionNote extends Note {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", updatable = false, nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(length = 100, nullable = false)
    private Feeling feeling;

    @Enumerated(EnumType.STRING)
    @Column(length = 100, nullable = false)
    private Weather weather;

    @Enumerated(EnumType.STRING)
    @Column(length = 100, nullable = false)
    private Season season;

    private QuestionNote(Diary diary, String title, LocalDate date, Visibility visibility, Question question, Feeling feeling, Weather weather, Season season) {
        super(diary, title, date, visibility);
        this.question = question;
        this.feeling = feeling;
        this.weather = weather;
        this.season = season;
    }

    public static QuestionNote of(Diary diary, String title, LocalDate date, Visibility visibility, Question question, Feeling feeling, Weather weather, Season season) {
        return new QuestionNote(diary, title, date, visibility, question, feeling, weather, season);
    }

    public void update(Diary foundDiary,
                       String title,
                       Weather weather,
                       Season season,
                       Feeling feeling,
                       LocalDate date,
                       String content) {
        super.update(foundDiary, title, date, List.of(content));
        this.weather = weather;
        this.season = season;
        this.feeling = feeling;
    }
}
