package com.woozuda.backend.ai_diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woozuda.backend.ai.config.ChatGptService;
import com.woozuda.backend.ai_diary.dto.AiDiaryDTO;
import com.woozuda.backend.forai.dto.NonRetroNoteEntryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryAnalysisService {
    private final ChatGptService chatGptService;
    private final ObjectMapper objectMapper;
    private final AiDiaryService aiDiaryService;

    public void analyzeDiary(List<NonRetroNoteEntryResponseDto> diaryList , String username) {
        if (diaryList == null || diaryList.isEmpty()) {
            throw new IllegalArgumentException("분석할 일기 데이터가 없습니다.");
        }

        StringBuilder userMessage = new StringBuilder();

        // 일기 내용 반복문
        for (NonRetroNoteEntryResponseDto diary : diaryList) {
            userMessage.append("type: ").append(diary.getType()).append("\n");
            userMessage.append("id: ").append(diary.getId()).append("\n");
            userMessage.append("title: ").append(diary.getTitle()).append("\n");
            userMessage.append("date: ").append(diary.getDate()).append("\n");
            userMessage.append("weather: ").append(diary.getWeather() != null ? diary.getWeather() : "없음").append("\n");
            userMessage.append("season: ").append(diary.getSeason() != null ? diary.getSeason() : "없음").append("\n");
            userMessage.append("feeling: ").append(diary.getFeeling() != null ? diary.getFeeling() : "없음").append("\n");
            userMessage.append("content: ").append(diary.getContent()).append("\n\n");
        }

        // 프롬프트 정의
        String systemMessage = """
                당신은 분석 도우미입니다. 사용자의 일기를 분석하고 다음과 같은 정보를 제공하세요:
                1. 주요 장소(place), 주요 활동(activity), 주요 감정(emotion)을 반드시 분석해 값을 반환해서 목록 형식으로 제공
                2. 사용자가 제공한 날씨 데이터를 감정을 기반으로 분석 결과를 작성하세요.
                3. 평일/주말 비율(weekdayAt, weekendAt)을 분석이 가능하면 각각 반드시 백분율로 출력해주세요.
                    - `weekdayAt`과 `weekendAt`의 합은 꼭 100%가 되어야 합니다. 절대 Null 과 0.0을 출력하지 마세요.
                4. 긍정적 감정(positive)과 부정적 감정(denial)이 분석이 가능하면 긍정적 감정과 부정적 감정의 비율을 각각 백분율로 출력해주세요.
                    - `positive`와 `denial`의 합은 꼭 100%가 되어야 합니다. 절대 Null 과 0.0을 출력하지 마세요.
                5. 개선 사항이나 추천 행동(suggestion)을 반드시 작성하세요.
                6. **중요** 분석이 불가능한 경우 비슷한 데이터라도 출력해주세요. 절대 Null 반환 금지
                7. 시작날짜와 끝나는 날짜는 꼭 출력해주세요.
                8. 위의 내용을 포함하여 각 항목을 반환해주세요. 예:
                    start_date: 2024-12-01
                    end_date: 2024-12-31
                    place: 장소1, 장소2
                    activity: 활동1, 활동2
                    emotion: 주요감정1" , "주요감정2
                    weather: 비가 올때는 눈물이 난다. 날씨가 맑을때 기분이 좋다.
                    weekdayAt: 50.0
                    weekendAt: 50.0
                    positive: 80.0
                    denial: 20.0
                    suggestion: 일정 속에서 조금 더 휴식을 취하고, 자신만의 시간을 갖는 것이 중요해 보입니다.
                """;
        log.info("사용자 메시지 내용 Diary: {}", userMessage);

        // ChatGPT API 호출
        String response = chatGptService.analyzeDiaryUsingGPT(systemMessage, userMessage.toString());


        // 로그: AI가 응답한 내용 출력
        log.info("AI 응답 내용: {}", response);
        // GPT 응답을 AiDiaryDTO로 매핑
        AiDiaryDTO aiDiaryDTO = mapResponseToAiDiaryDTO(response , username);
        // DB에 저장
        aiDiaryService.saveAiDiary(aiDiaryDTO);

    }

    private AiDiaryDTO mapResponseToAiDiaryDTO(String response ,String username) {
        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode choicesNode = root.path("choices");
            JsonNode firstChoiceNode = choicesNode.get(0);
            JsonNode messageNode = firstChoiceNode.path("message");
            JsonNode contentNode = messageNode.path("content");
            String content = contentNode.asText();

            String startDate = extractValue(content, "start_date");
            String endDate = extractValue(content, "end_date");
            LocalDate start_date = convertStringToDate(startDate);
            LocalDate end_date = convertStringToDate(endDate);

            // 항목 추출
            String place = extractValue(content, "place");
            String activity = extractValue(content, "activity");
            String emotion = extractValue(content, "emotion");
            String weather = extractValue(content, "weather");

            // 감정 비율 처리
            String positiveStr = extractValue(content, "positive");
            String denialStr = extractValue(content, "denial");
            float positive = convertStringToFloat(positiveStr);
            float denial = convertStringToFloat(denialStr);

            // 평일, 주말 비율 처리
            String weekdayRatioStr = extractValue(content, "weekdayAt");
            String weekendRatioStr = extractValue(content, "weekendAt");
            float weekday = convertStringToFloat(weekdayRatioStr);
            float weekend = convertStringToFloat(weekendRatioStr);

            String suggestion = extractValue(content, "suggestion");

            return new AiDiaryDTO(
                    start_date,
                    end_date,
                    place,
                    activity,
                    emotion,
                    weather,
                    weekday,
                    weekend,
                    positive,
                    denial,
                    suggestion,
                    username
            );
        } catch (Exception e) {
            log.error("응답 매핑 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("응답 매핑 중 오류 발생: " + e.getMessage());
        }
    }
    private LocalDate convertStringToDate(String date) {
        date = date.replaceAll("\"", ""); // 따옴표 제거

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            log.error("잘못된 날짜 형식: '{}'. 기본값을 사용합니다.", date);
            return LocalDate.now(); // 기본값 설정
        }
    }

    private float convertStringToFloat(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0f;
        }
        // 숫자와 소수점(.)만 남기기
        String numberStr = value.replaceAll("[^0-9.]", "");
        try {
            return Float.parseFloat(numberStr);
        } catch (NumberFormatException e) {
            log.error("비율 파싱 오류: {}", value, e);
            return 0.0f;
        }
    }

    private String extractValue(String content, String key) {
        if (content == null || content.isEmpty()) {
            log.warn("내용이 비어 있음: {}", key);
            return "분석불가";
        }

        // 키에 따른 정규식 패턴
        String pattern = key + "\\s*:\\s*(.*?)(?=\\n|$)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(content);

        if (matcher.find()) {
            String extractedValue = matcher.group(1).trim();
            return extractedValue.isEmpty() ? "값 없음" : extractedValue;
        }

        log.warn("값 추출 실패: {}", key);
        return "분석불가"; // 기본값 설정
    }
}