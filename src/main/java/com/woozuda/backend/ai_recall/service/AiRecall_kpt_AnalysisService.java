package com.woozuda.backend.ai_recall.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woozuda.backend.ai.config.ChatGptService;
import com.woozuda.backend.ai_recall.dto.Airecall_Kpt_DTO;
import com.woozuda.backend.forai.dto.RetroNoteEntryResponseDto;
import com.woozuda.backend.forai.service.CustomeNoteRepoForAiService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecall_kpt_AnalysisService {
    private final ChatGptService chatGptService;
    private final ObjectMapper objectMapper;
    private final AiRecallService aiRecallService;

    public void analyzeAirecall(List<RetroNoteEntryResponseDto> recallList, String username) {
        // 회고 분석 요청 메시지 작성
        StringBuilder userMessage = new StringBuilder();


        // 회고 내용 추가
        for (RetroNoteEntryResponseDto recall_ktp : recallList) {
            userMessage.append("title: ").append(recall_ktp.getTitle()).append("\n");
            userMessage.append("date: ").append(recall_ktp.getDate()).append("\n");
            userMessage.append("framework: ").append(recall_ktp.getFramework()).append("\n");
            userMessage.append("content").append(recall_ktp.getContent()).append("\n");
        }

        // 프롬프트 정의
        String systemMessage = """
                당신은 분석 도우미입니다. 사용자의 회고 데이터를 분석하고 다음과 같은 정보를 제공하세요:
                 1. type 이 "KPT" 인 경우 KPT 을 정확히 출력해주세요.
                 2. strength_analysis : 일기 내용에서 행동이나 경험의 강점 또는 긍정적인 측면을 식별하고 설명해 주세요.
                 3. improvement : 일기 내용에서 개선할 수 있는 부분이나 행동이나 상황을 개선할 수 있는 방법을 제안해 주세요.
                 4. scalability : 일기 내용에서 사용자의 활동이나 경험에서 확장 가능성이나 성장의 기회를 분석해 주세요.
                 5. **중요**절대 모든 값의 Null 및 0을 반환하지 마세요. 비슷한 분석 결과값을 반환해주세요.
                 6. 시작날짜와 끝나는 날짜는 꼭 출력해주세요.
                 7. 위의 내용을 포함하여 각 항목을 객체 타입으로 한번만 반환해주세요. 예:
                     start_date: 2024-12-01
                     end_date: 2024-12-31
                     type : KTP
                     strength_analysis : 강점 분석
                     improvement : 개선 제안
                     scalability : 확장 가능성
                """;
        log.info("사용자 메시지 KTP 내용: {}", userMessage.toString());
        // ChatGPT API 호출
        String response = chatGptService.analyzeDiaryUsingGPT(systemMessage, userMessage.toString());

        log.info("AI 응답 내용: {}", response);

        // 응답 매핑
        Airecall_Kpt_DTO airecall_kpt_dto = mapResponseToAirecall(response, username);

        // DB에 저장
        aiRecallService.saveAirecall_ktp(airecall_kpt_dto);
    }

    private Airecall_Kpt_DTO mapResponseToAirecall(String response, String username) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choicesNode = root.path("choices");
            JsonNode firstChoiceNode = choicesNode.get(0);
            JsonNode messageNode = firstChoiceNode.path("message");
            JsonNode contentNode = messageNode.path("content");
            // content 값을 String으로 변환
            String content = contentNode.asText();
            /**
             * 날짜 변경
             */
            String startDate = extractValue(content, "start_date");
            String endDate = extractValue(content, "end_date");
            LocalDate start_date = convertStringToDate(startDate);
            LocalDate end_date = convertStringToDate(endDate);

            /**
             * type 변경
             */
            String airecallType = extractValue(content, "type");

            String strength_analysis = extractValue(content, "strength_analysis");
            String improvement = extractValue(content, "improvement");
            String scalability = extractValue(content, "scalability");


            return new Airecall_Kpt_DTO(
                    airecallType,
                    start_date,
                    end_date,
                    strength_analysis,
                    improvement,
                    scalability,
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