### querydsl
***
  + practice querydsl in springBoot
  + Ch01. 프로젝트 환경설정 
    1. 프로젝트 생성 
    2. Querydsl 설정과 검증
    3. 스프링 부트 설정 - JPA, DB
    4. 예제 도메인 모델 설정 및 확인
  + Ch02. Querydsl 기본 문법
    1. JPQL VS Querydsl
    2. 기본 Q-Type 활용
    3. 검색 조건 쿼리
    4. 결과 조회
    5. 정렬
    6. 페이징
    7. 집합(group by)
    8. 조인 - basic
    9. 조인 - on 절
    10. 조인 - 페치(fetch) 조인
    11. 서브쿼리
    12. Case 문
    13. 상수, 문자 더하기
  + Ch03. Querydsl 중급 문법
    1. 프로젝션과 결과 반환 - BASIC
    2. 프로젝션과 결과 반환 - DTO Search
    3. 프로젝션과 결과 반환 - @QueryProjection
    4. 동적쿼리 - BooleanBuilder
    5. 동적쿼리 - Where 다중 파라미터
    6. UPDATE, DELETE 벌크(BULK) 처리
    7. SQL function 호출
  + Ch04. 순수 JPA 리포지토리와 Querydsl
    1. 순수 JPA 리포지토리와 Querydsl
    2. 동적 쿼리와 성능 최적화 - Builder
    3. 동적 쿼리와 성능 최적화 - Where 절 파라미터
    4. 조회 API 컨트롤러 개발
  + Ch05. 스프링 데이터 JPA와 Querydsl
    1. 스프링 데이터 JPA 리포지토리로 변경
    2. 사용자정의 리포지토리(MemberRepositoryImpl)
    3. 스프링 데이터 페이징 활용 1 - Querydsl 페이징
    4. 스프링 데이터 페이징 활용 2 - CountQuery 최적화
    5. 스프링 데이터 페이징 활용 3 - MemberController API 구현
  + Ch06. 스프링 데이터 JPA가 제공하는 Querydsl 기능
    1. 인터페이스 지원 - QuerydslPredicateExecutor
    2. Querydsl 지원 클래스 직접 만들기 - Querydsl4RepositorySupport