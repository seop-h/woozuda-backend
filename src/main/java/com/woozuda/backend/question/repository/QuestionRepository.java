package com.woozuda.backend.question.repository;

import com.woozuda.backend.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("select q from Question q where cast(q.createdAt as date) = :today")
    Question findByTodayDate(@Param("today") LocalDate today);

}
