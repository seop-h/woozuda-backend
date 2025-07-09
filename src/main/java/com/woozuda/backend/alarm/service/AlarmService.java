package com.woozuda.backend.alarm.service;

import com.woozuda.backend.shortlink.repository.SharedNoteRepoImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmService {

    private final SharedNoteRepoImpl sharedNoteRepo;

    // 메모리 저장소 (서버 켤 때 마다 일관된 값을 들고 있는게 아니라, 연결 자체를 기록하는 것이므로 메모리 저장소도 괜찮음)
    // 서버가 여러 대로 늘어나면 redis 도입을 해 봐야함
    //
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(String username){

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.put(username, emitter);

        //emitter 가 완료 되거나 타임아웃 되면 리스트에서 제거
        emitter.onCompletion(() -> {
            log.info("emitter.onCompletion 발생");
            emitters.remove(username);
        });

        emitter.onTimeout(() -> {
            log.info("emitter.onTimeout 발생");
            emitter.complete();
            emitters.remove(username);
        });

        emitter.onError((e) -> {
            log.info("emitter.onError 발생 : {}", e.getMessage());
            emitters.remove(username);
        });

        // 초기 1회는 답을 해주어야 함
        try{
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("SSE connect"));
        }catch(IOException e){
            log.info("초기 연결 메세지 못보냈음");
            emitter.completeWithError(e);
            emitters.remove(username);
        }

        return emitter;
    }

    public void threePostAlarm(String username, String date){
        Long postCount = sharedNoteRepo.noteCountToMakeReport(username, date);
        log.info("postCount 는 {}", postCount);

        if(postCount == 3){
            SseEmitter emitter = emitters.get(username);

            if(emitter == null){
                return;
            }

            WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
            LocalDate nowDate = LocalDate.parse(date);
            int week = nowDate.get(weekFields.weekOfMonth());
            int month = nowDate.getMonthValue();
            int year = nowDate.getYear();

            String msg = String.format("%d년 %d월 %d주 AI 레포트를 생성해보세요!", year, month, week);

            try{
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(msg));
            }catch(IOException e){
                log.info("알람 메세지 못보냈음");
                emitters.remove(username);
            }
        }
    }


    public void alarmTest(String username){
        SseEmitter emitter = emitters.get(username);

        if(emitter == null){
            return;
        }

        try{
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data("테스트 알람 메세지 입니다."));
        }catch(IOException e){
            log.info("테스트 알람 메세지 못보냈음");
            emitters.remove(username);
        }
    }

    @Scheduled(fixedDelay = 21600000) // 360분(6시간)마다 하트비트
    public void sendHeartbeat() {

        //log.info("하트비트 확인용 ");
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {

            SseEmitter emitter = entry.getValue();

            try {
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("heartbeat"));
            } catch (IOException e) {
                log.info("emitter 핑 실패");
            }
        };
    }
}
