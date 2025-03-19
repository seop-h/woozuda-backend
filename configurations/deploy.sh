#!/bin/bash

# 1. 8080번 포트에서 실행 중인 프로그램 확인
PORT=8080
echo "Checking for running application on port $PORT..."
PID=$(lsof -ti:$PORT)

if [ -n "$PID" ]; then
  # 2. 실행 중인 프로그램이 있으면 종료
  echo "Application found on port $PORT (PID: $PID). Terminating..."
  kill -15 $PID

  # 3. 완전히 종료될 때까지 기다림
  echo "Waiting for application to stop..."
  while lsof -ti:$PORT >/dev/null; do
    sleep 2
  done
  echo "Application stopped."
else
  echo "No application running on port $PORT."
fi

# 4. 새로운 애플리케이션 실행
echo "Starting new application..."
nohup java -jar -Dserver.port=${PORT} -Dspring.profiles.active=release app.jar > ../log/app.log 2>&1 &

# 5. 실행 상태 확인
echo "Checking if the application is running on port $PORT..."
for i in {1..15}; do
  PID=$(lsof -ti:$PORT | grep "$(pgrep java)")
  if [ -n "$PID" ]; then
    echo "Application is running successfully on port $PORT (PID: $PID)."
    exit 0
  fi
  sleep 2
done

# 6. 30초 동안 동작이 확인되지 않으면 실패 메시지 출력
echo "Failed to detect the application running on port $PORT within 30 seconds."
exit 1