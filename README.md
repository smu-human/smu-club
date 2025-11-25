# Docker 명령어 정리
## 단순 멈춰있는 특정 컨테이너 재실행
```sh
docker start <컨테이너 이름 or ID>
```

## 명령어로 프로세스, 이미지 한번에 삭제하는 법
```sh
docker stop $(docker ps -q) # 모든 실행중인 컨테이너 중지
docker rm $(docker ps -a -q) # 모든 컨테이너 삭제
docker rmi -f $(docker images -a) # 모든 이미지 삭제

# 이후 볼륨 삭제 희망 시, 도커 데스크탑에서 직접 확인 후 삭제 요망
```

## 프로세스 확인
```sh
#항상 n번 이상 실행하여 서버 상태 확인하기
docker ps

#멈춘 프로세스 확인
docker ps -a
```

## 컨테이너 내부 접속
```sh
docker exec -it <컨테이너 이름> /bash
```

## 컨테이너 로그 확인법
```sh
#1. 컨테이너 이름 확인
docker ps
# @CONTAINER ID@   IMAGE     COMMAND   CREATED   STATUS    PORTS    @NAMES@

#2-1. 로그 확인
docker logs <컨테이너 이름 or ID> #id 사용시 앞 3~4자리만 입력해도 무방

#2-2. 전체 서비스 실시간 로그 (추천)
docker-compose -f docker-compose-dev.yml logs -f

```

## DB 컨테이너 덤프파일 적용
```sh
# 1. 기존 DB 스토어 삭제
rm -rf./db/store

# 2. 도커 컨테이너 + 볼륨 삭제
docker-compose -f docker-compose-dev.yml down -v

# 3. 도커 재시작
docker-compose -f docker-compose-dev. yml up -d #  여기까지 하면 JPA가 테이블 자동 생성함

# 4. dump 파일 적용하기
docker exec -i smu-club-db-1 mysql -u <username> -p'<password>' smu_db < dump.sql
```

# Docker Compose 명령어 정리

## 백그라운드 실행법 (daemon == -d)
```sh
# 디렉토리 위치 확인
docker-compose -f docker-compose-dev.yml up -d

# 만약 수정사항이 존재한다면
docker-compose -f docker-compose-dev.yml up -d --build 
```

## 프로세스 중지[메모리 스왑 아웃] (평상 시 삭제 x 중지 o)
```sh
docker-compose -f docker-compose-dev.yml stop
```


## 리소스 삭제 (볼륨 미포함)
```sh
#볼륨 포함 시 -v 옵션 추가
docker-compose -f docker-compose-dev.yml down 
```

## 특정 서비스만 실행
```sh
docker-compose -f docker-compose-dev.yml up -d [서비스명]

#services:
# db: <<< 서비스명

#의존성 무시할 경 서비스명 앞에 --no-deps 옵션 추가(but, 권장 x)
```



## UTF 8 설정확인
```sh
SHOW VARIABLES LIKE 'character_set%';
```

## 볼륨 옵션으로 실행하는 법
```sh
docker run -d -v /Users/sjy/docker_lab/ex05/mysql-volume:/var/lib/mysql -p 3307:3306 --name mysql-container mysql-image
```

## 이름 있는 볼륨 실행하는 법
```sh
docker run -d -v mysql-volume:/var/lib/mysql -p 3307:3306 --name mysql-container mysql-image
```


## 도커 컴포즈 백그라운드 실행법
```sh
docker-compose up -d
```


### 리엑트 실행하기
```sh
npm start
```

### 리엑트 빌드하기 (html 파일 만들기)
```sh
npm run build
```

### Dockerfile 만들어서 이미지 빌드하기
```sh
docker build . -t react-app
```

### nginx 실행하기
```sh
docker run -dit -p 80:80 react-app
```


# 프론트와 백엔드 배포를 따로 하는게 좋지 않을까?
## 1. 따로 배포하는 경우:
### 장점:
각 서비스를 독립적으로 확장 가능하며, 각각의 스케일링이 필요한 경우에 효율적입니다. 
각 서비스를 별도로 관리하고 업데이트하므로 서비스 간의 의존성을 줄일 수 있습니다. 
프론트엔드와 백엔드가 다른 언어 또는 기술 스택으로 개발되었거나 다른 인프라에서 호스팅되어야 하는 경우 유용합니다.

### 단점:

서비스 간의 통신과 CORS(Cross-Origin Resource Sharing) 관리가 필요합니다. 
관리 및 배포가 두 개의 독립적인 애플리케이션으로 분리되어 추가 작업이 필요합니다.

## 2. docker-compose로 묶어서 배포하는 경우: 
### docker-compose는 로컬 테스트 용도

### 장점:

편리하게 여러 서비스를 하나의 설정으로 관리할 수 있으며, 단일 배포 구성 파일로 전체 애플리케이션을 쉽게 배포할 수 있습니다. 개발 환경과 유사한 환경에서 애플리케이션을 실행하고 테스트하는 데 용이합니다. 각 서비스의 종속성 및 구성이 일관되게 관리됩니다.

### 단점:

모든 서비스를 함께 확장해야 하므로 일부 서비스에 불필요한 스케일링이 발생할 수 있습니다. 서비스 간의 의존성이 높은 경우 복잡성이 증가할 수 있습니다. 서비스 간의 통신 및 CORS 관리가 필요하며, 보안 설정이 중요합니다. 어떤 방식을 선택하느냐는 프로젝트 요구 사항과 팀의 개발 및 관리 스킬에 따라 다릅니다. 즉, 각 프로젝트의 고유한 상황에 맞춰 결정해야 합니다. 일반적으로 프로젝트가 복잡한 경우나 서비스 간의 의존성이 낮은 경우에는 서비스를 따로 배포하는 것이 유용할 수 있습니다. 반면에 단순한 애플리케이션이고 통합 관리가 편리한 경우에는 docker-compose로 묶어서 배포하는 것이 간편할 수 있습니다.

