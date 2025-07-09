package com.woozuda.backend.ai_creation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woozuda.backend.ai.config.ChatGptService;
import com.woozuda.backend.ai_creation.dto.AiCreationDTO;
import com.woozuda.backend.forai.dto.NonRetroNoteEntryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreationWritingAnalysisService {
    private final ChatGptService chatGptService;
    private final ObjectMapper objectMapper;
    private final AiCreationService aiCreationService;

    public void analyze(List<NonRetroNoteEntryResponseDto> diaryList , String username) {
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
                1. 일기를 읽고 창작으로 시를 간단하게 써주세요.
                2. **중요** 분석이 불가능한 경우 비슷한 데이터라도 출력해주세요. 절대 Null 반환 금지
                3. 시작날짜와 끝나는 날짜는 꼭 출력해주세요.
                4. text 는 줄바꿈 없이 230 이하로 창착한 글을 출력해주세요.
                5. 위의 내용을 포함하여 각 항목처럼 반환해주세요. 예:
                    start_date :2024-12-01
                    end_date :2024-12-31
                    text : 오늘의 내 기분은 그렇게 좋지 않았다.
                """;
        log.info("사용자 메시지 내용 Diary: {}", userMessage.toString());

        // ChatGPT API 호출
        String response = chatGptService.analyzeDiaryUsingGPT(systemMessage, userMessage.toString());

        // 로그: AI가 응답한 내용 출력
        log.info("AI 응답 내용: {}", response);

        // GPT 응답을 AiDiaryDTO로 매핑
        AiCreationDTO aiCreationDTO = mapResponseToAiDiaryDTO(response , username);

        // DB에 저장
        aiCreationService.saveCreation(aiCreationDTO);

    }

    private AiCreationDTO mapResponseToAiDiaryDTO(String response ,String username) {
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
            String creationType = "WRITING";
            String img = extractValue(content, "text");
            String text = img;

            String visibility = "PRIVATE";
            return new AiCreationDTO(
                    start_date,
                    end_date,
                    creationType,
                    text,
                    img,
                    visibility,
                    username
            );
        } catch (Exception e) {
            log.error("응답 매핑 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("응답 매핑 중 오류 발생: " + e.getMessage());
        }
    }
    private LocalDate convertStringToDate(String date) {
        if (date != null) {
            date = date.replaceAll("\"", ""); // 따옴표 제거
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            log.error("잘못된 날짜 형식: '{}'. 기본값을 사용합니다.", date);
            return LocalDate.now(); // 기본값 설정
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
