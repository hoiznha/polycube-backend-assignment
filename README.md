# Polycube Backend Assignment

## 과제 개요

신규 서비스에 사용할 결제 시스템을 Spring Boot로 구현하는 과제입니다.

비즈니스 요구사항이 자주 바뀔 수 있다는 조건을 중요하게 보고, 결제 흐름과 할인 정책을 분리하는 방향으로 설계하고 있습니다.

## 진행 순서

현재는 `main` 브랜치의 기본 요구사항을 구현하는 단계입니다.

1. Spring Boot 프로젝트 생성
2. `Member`, `Order`, `Payment` 기본 엔티티 생성
3. 등급 할인 정책 인터페이스와 구현체 생성
4. `PaymentService` 구현
5. 등급별 할인 테스트 작성
6. `main` 브랜치 커밋
7. `feature` 브랜치 생성
8. `PaymentDiscountHistory` 추가
9. 포인트 결제 5% 중복 할인 추가
10. 중복 할인 테스트 작성
11. 이력 보존 테스트 작성
12. README 최종 정리
13. GitHub Public Repository push

## 기술 스택

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 Database
- Lombok
- JUnit5
- Gradle

## 현재 구현 상태: main 브랜치

### 1. 프로젝트 생성

Spring Initializr를 사용해 Spring Boot 3.x 기반 프로젝트를 생성했습니다.

사용한 주요 의존성은 다음과 같습니다.

- Spring Web
- Spring Data JPA
- H2 Database
- Lombok
- Validation
- Spring Boot Test

### 2. 기본 엔티티

현재 `main` 브랜치에서는 기본 요구사항에 필요한 핵심 엔티티를 먼저 구성했습니다.

#### Member

회원은 등급 정보를 가집니다.

- `NORMAL`
- `VIP`
- `VVIP`

#### Order

주문은 상품명, 주문 원가, 회원 정보를 가집니다.

- `productName`
- `originalAmount`
- `member`

`Order`는 SQL 예약어와 충돌할 수 있으므로 테이블명은 `orders`로 지정했습니다.

#### Payment

결제는 주문, 최종 결제 금액, 결제 수단, 결제 일시를 저장합니다.

- `order`
- `finalAmount`
- `paymentMethod`
- `paidAt`

현재 `main` 브랜치에서는 기본 요구사항에 맞춰 최종 결제 금액 중심으로 저장합니다.
이후 `feature` 브랜치에서는 할인 이력 스냅샷을 별도 엔티티로 분리할 예정입니다.

## 할인 정책 설계

할인 정책은 `PaymentService` 내부 조건문으로 직접 처리하지 않고, 별도 정책 객체로 분리했습니다.

```text
DiscountPolicy
- supports(memberGrade)
- discount(order)
```

구현체는 다음과 같습니다.

```text
NoDiscountPolicy
VipFixedDiscountPolicy
VvipRateDiscountPolicy
```

각 정책의 책임은 다음과 같습니다.

- `NoDiscountPolicy`: `NORMAL` 회원, 할인 없음
- `VipFixedDiscountPolicy`: `VIP` 회원, 1,000원 고정 할인
- `VvipRateDiscountPolicy`: `VVIP` 회원, 주문 금액의 10% 할인

할인 계산 결과는 `DiscountResult`로 반환합니다.

```text
DiscountResult
- policyName
- target
- discountRate
- discountAmount
```

이 구조는 이후 `feature` 브랜치에서 결제 당시 적용된 할인 스냅샷을 저장할 때 재사용하기 위한 의도도 포함하고 있습니다.

## 결제 흐름

`PaymentService`는 결제 흐름을 조립하는 역할을 맡습니다.

```text
1. 회원 조회
2. 주문 생성 및 저장
3. 회원 등급에 맞는 할인 정책 선택
4. 할인 금액 계산
5. 최종 결제 금액 계산
6. 결제 정보 저장
7. 결제 결과 반환
```

등급별 할인 정책 선택은 `GradeDiscountCalculator`가 담당합니다.
이를 통해 할인 정책이 추가되더라도 `PaymentService`의 변경을 최소화하는 방향으로 설계했습니다.

## 예외 처리 방향

현재 기본 구현에서는 다음과 같은 잘못된 입력을 방지합니다.

- 존재하지 않는 회원
- 상품명이 비어 있는 주문
- 주문 금액이 0 이하인 경우
- 회원 등급이 없는 경우
- 결제 수단이 없는 경우
- 할인 금액이 주문 금액보다 큰 경우

## 테스트

현재 작성된 테스트는 등급별 할인 정책을 검증하는 단위 테스트입니다.

```text
GradeDiscountCalculatorTest
```

검증 시나리오는 다음과 같습니다.

- `NORMAL` 회원은 할인이 적용되지 않는다.
- `VIP` 회원은 1,000원 고정 할인이 적용된다.
- `VVIP` 회원은 주문 금액의 10% 할인이 적용된다.

테스트는 Spring Context를 띄우지 않는 순수 단위 테스트로 작성했습니다.
할인 정책의 의도를 빠르고 명확하게 검증하기 위해서입니다.

## 실행 방법

테스트 실행:

```bash
./gradlew test
```

컴파일 확인:

```bash
./gradlew compileJava
```

전체 빌드:

```bash
./gradlew build
```

## feature 브랜치에서 개선할 내용

`main`에서는 단순 할인 정책 구조를 먼저 구성했습니다.

`feature`에서는 정책 변경에 따른 과거 데이터 훼손을 막기 위해, 계산 정책과 결제 당시 적용된 할인 스냅샷을 분리할 예정입니다.

추가 예정 구조는 다음과 같습니다.

```text
Payment
- originalAmount
- finalAmount
- paymentMethod
- paidAt

PaymentDiscountHistory
- payment
- discountType: GRADE / PAYMENT_METHOD
- policyName
- target: VIP, VVIP, POINT 등
- discountRate
- discountAmount
- appliedOrder
```

포인트 결제 시 추가 5% 중복 할인은 다음 순서로 적용할 예정입니다.

```text
1. 주문 원가
2. 회원 등급 할인 적용
3. 등급 할인 후 금액
4. 포인트 결제라면 남은 금액에서 추가 5% 할인
5. 최종 결제 금액
```

이를 통해 정책이 수정되거나 삭제되어도 이미 완료된 결제의 할인 이력은 보존되도록 설계할 계획입니다.
