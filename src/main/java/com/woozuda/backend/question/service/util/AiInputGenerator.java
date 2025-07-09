package com.woozuda.backend.question.service.util;

import java.util.List;
import java.util.Random;

public class AiInputGenerator {

    private final static List<String> keywords = List.of(
            "소중한 순간", "성장", "변화", "결정", "친절", "자아성찰", "노력", "한계", "가치관", "관점", "목표",
            "전환점", "용기", "도전", "관계", "배려", "습관", "열정", "선택", "용서", "계획", "창의성", "성공",
            "시간 관리", "시작", "행복", "우정", "인내심", "신념", "독서", "집중", "실패 경험", "긍정",
            "감사함", "존중", "자기계발", "추억", "배움", "휴식", "꿈", "경험", "영향력", "소통", "결심", "성취",
            "자유", "신뢰", "가족", "책임감", "사랑"
    );

    private final static String INPUT_SUFFIX = "에 대한 질문을 작성해줘.";

    //keywords 기반 랜덤 질문 생성
    public static String execute() {
        int randomIndex = new Random().nextInt(keywords.size());
        return keywords.get(randomIndex) + INPUT_SUFFIX;
    }

}
