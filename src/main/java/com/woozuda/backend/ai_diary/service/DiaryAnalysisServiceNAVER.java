package com.woozuda.backend.ai_diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woozuda.backend.ai.config.NaverCLOVAConfig;
import com.woozuda.backend.ai_diary.dto.AiDiaryDTO;
import com.woozuda.backend.forai.dto.NonRetroNoteEntryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryAnalysisServiceNAVER {
    private final ObjectMapper objectMapper;
    private final AiDiaryService aiDiaryService;
    private final NaverCLOVAConfig naverCLOVAConfig;

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
                8. 위의 내용을 포함하여 각 항목을 반환해주세요. 그리고 응답에서 `id`를 제거해주고 event에는 result` 부분만 포함하여 주세요." 예:
                    start_date: 2024-12-01
                    end_date: 2024-12-31
                    place: 장소1, 장소2
                    activity: 활동1, 활동2
                    emotion: 주요감정1 , 주요감정2
                    weather: 비가 올때는 눈물이 난다. 날씨가 맑을때 기분이 좋다.
                    weekdayAt: 50.0
                    weekendAt: 50.0
                    positive: 80.0
                    denial: 20.0
                    suggestion: 일정 속에서 조금 더 휴식을 취하고, 자신만의 시간을 갖는 것이 중요해 보입니다.
                """;
        log.info("사용자 메시지 내용 Diary: {}", userMessage.toString());

        // 클로바 호출
        String response = naverCLOVAConfig.analyzeDiaryUsingCLOVA(systemMessage,userMessage.toString());

        log.info("클로바 호출 성공 ");

        // 로그: AI가 응답한 내용 출력
        log.info("AI 응답 내용: {}", response);

       // AiDiaryDTO aiDiaryDTO = mapResponseToAiDiaryDTO(response , username);

        // DB에 저장
        //aiDiaryService.saveAiDiary(aiDiaryDTO);

    }
    public AiDiaryDTO mapResponseToAiDiaryDTO(String response , String username) {
        try {
            // "data:" 제거 후 JSON 변환
            String jsonResponse = response.replaceFirst("^data:", "").trim();
            JsonNode root = objectMapper.readTree(jsonResponse);

            // "message" -> "content" 추출
            String content = root.path("message").path("content").asText();

            // 값 추출
            String startDate = extractValue(content, "start_date");
            String endDate = extractValue(content, "end_date");
            String place = extractValue(content, "place");
            String activity = extractValue(content, "activity");
            String emotion = extractValue(content, "emotion");
            String weather = extractValue(content, "weather");
            String suggestion = extractValue(content, "suggestion");

            // 숫자 변환
            float positive = convertStringToFloat(extractValue(content, "positive"));
            float denial = convertStringToFloat(extractValue(content, "denial"));
            float weekdayAt = convertStringToFloat(extractValue(content, "weekdayAt"));
            float weekendAt = convertStringToFloat(extractValue(content, "weekendAt"));

            // 날짜 변환
            LocalDate start_date = convertStringToDate(startDate);
            LocalDate end_date = convertStringToDate(endDate);

            // DTO 반환
            return new AiDiaryDTO(
                    start_date,
                    end_date,
                    place,
                    activity,
                    emotion,
                    weather,
                    weekdayAt,
                    weekendAt,
                    positive,
                    denial,
                    suggestion,
                    username
            );
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 중 오류 발생", e);
        }
    }

    // 문자열에서 특정 키의 값을 추출하는 메서드
    private String extractValue(String content, String key) {
        String pattern = key + "\\s*:\\s*\"?(.*?)\"?\\n";
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    // 문자열을 LocalDate로 변환
    private LocalDate convertStringToDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateStr, formatter);
    }

    // 문자열을 float으로 변환
    private float convertStringToFloat(String floatStr) {
        return floatStr.isEmpty() ? 0.0f : Float.parseFloat(floatStr);
    }
}
