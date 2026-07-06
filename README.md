# Polycube Backend Assignment

## 과제 개요

신규 서비스에 사용할 결제 시스템을 Spring Boot로 구현한 과제입니다.

요구사항에서 가장 중요하게 본 문장은 “비즈니스 요구사항이 자주 바뀔 수 있다”는 부분이었습니다. 그래서 단순히 현재 할인 조건을 만족하는 것보다, 할인 정책이 추가되거나 변경되어도 결제 흐름 전체가 크게 흔들리지 않는 구조를 목표로 했습니다.

## 기술 스택

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 Database
- Lombok
- JUnit5
- Gradle

## 브랜치 전략

### main

기본 요구사항을 구현했습니다.

- 회원 등급: `NORMAL`, `VIP`, `VVIP`
- 주문: 상품명, 주문 원가, 회원 정보
- 결제: 주문 정보, 최종 결제 금액, 결제 수단, 결제 일시 저장
- 등급별 할인 정책
- 등급별 할인 단위 테스트

### feature

`main`의 구조를 확장하여 추가 요구사항을 구현했습니다.

- 결제 원가 `originalAmount` 저장
- 결제 당시 적용된 할인 스냅샷 저장
- 포인트 결제 시 5% 중복 할인
- 등급 할인과 결제 수단 할인 적용 순서 검증
- 결제 후 회원 등급이 변경되어도 기존 할인 이력이 보존되는지 검증

## 도메인 모델

### Member

회원은 등급 정보를 가집니다.

```text
Member
- id
- grade: NORMAL / VIP / VVIP
```

회원 등급 변경은 직접 setter를 열지 않고 `changeGrade` 메서드로 표현했습니다. 테스트에서 결제 이후 회원 등급이 변경되어도 결제 당시 할인 이력이 보존되는지 확인하기 위해 사용했습니다.

### Order

주문은 상품명, 주문 원가, 회원 정보를 가집니다.

```text
Order
- id
- productName
- originalAmount
- member
```

`Order`는 SQL 예약어와 충돌할 수 있으므로 테이블명은 `orders`로 지정했습니다.

### Payment

`main`에서는 최종 결제 금액 중심으로 저장했고, `feature`에서는 과거 결제 데이터 보존을 위해 주문 원가도 함께 저장하도록 확장했습니다.

```text
Payment
- id
- order
- originalAmount
- finalAmount
- paymentMethod
- paidAt
```

## 할인 정책 설계

회원 등급 할인은 `PaymentService` 내부 조건문으로 처리하지 않고 별도 정책 객체로 분리했습니다.

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

정책별 책임은 다음과 같습니다.

- `NoDiscountPolicy`: `NORMAL` 회원, 할인 없음
- `VipFixedDiscountPolicy`: `VIP` 회원, 1,000원 고정 할인
- `VvipRateDiscountPolicy`: `VVIP` 회원, 주문 금액의 10% 할인

`PaymentService`는 특정 등급의 할인 계산식을 직접 알지 않고, `GradeDiscountCalculator`가 `supports(memberGrade)`를 통해 적용 가능한 정책을 선택합니다.

이렇게 분리한 이유는 새로운 등급 할인 정책이 추가될 때 기존 결제 흐름을 수정하는 대신, 새로운 정책 구현체를 추가하는 방식으로 확장하기 위해서입니다.

## 할인 결과 모델

할인 계산 결과는 단순 금액만 반환하지 않고 `DiscountResult`로 표현했습니다.

```text
DiscountResult
- policyName
- target
- discountRate
- discountAmount
```

이 구조는 `feature` 브랜치에서 결제 당시 할인 이력을 저장할 때 그대로 활용됩니다.

## main에서 feature로 넘어가며 개선한 점

`main`에서는 기본 요구사항을 충족하기 위해 “현재 정책으로 계산된 최종 금액”을 저장하는 구조로 시작했습니다. 하지만 추가 요구사항에서는 정책이 짧은 주기로 변경되거나 삭제되어도 과거 결제 데이터와 이력이 보존되어야 합니다.

이 요구사항을 만족하기 위해 `feature`에서는 계산 정책과 결제 당시 적용된 할인 스냅샷을 분리했습니다.

```text
PaymentDiscountHistory
- payment
- discountType: GRADE / PAYMENT_METHOD
- policyName
- target
- discountRate
- discountAmount
- appliedOrder
```

핵심 의도는 다음과 같습니다.

- 할인 정책 객체는 현재 결제 계산을 담당한다.
- 결제 완료 후에는 계산 결과를 스냅샷으로 저장한다.
- 과거 결제 내역은 이후 회원 등급 변경이나 정책 변경의 영향을 받지 않는다.

예를 들어 결제 당시 `VVIP` 10% 할인이 적용되었다면, 이후 회원 등급이 `NORMAL`로 변경되더라도 해당 결제의 할인 이력에는 `VVIP`, `VvipRateDiscountPolicy`, `0.10`, `10,000원`이 그대로 남습니다.

## 중복 할인 적용 순서

포인트 결제 5% 중복 할인은 등급 할인 후 남은 금액에 적용했습니다.

```text
1. 주문 원가
2. 회원 등급 할인 적용
3. 등급 할인 후 금액
4. 포인트 결제라면 남은 금액에서 추가 5% 할인
5. 최종 결제 금액
```

예시:

```text
회원 등급: VVIP
결제 수단: POINT
주문 원가: 100,000원

등급 할인: 10,000원
등급 할인 후 금액: 90,000원
포인트 결제 할인: 4,500원
최종 결제 금액: 85,500원
```

이때 할인 이력은 다음 순서로 저장됩니다.

```text
appliedOrder=1: GRADE / VVIP / VvipRateDiscountPolicy / 10% / 10,000원
appliedOrder=2: PAYMENT_METHOD / POINT / PointPaymentDiscountPolicy / 5% / 4,500원
```

결제 수단 할인도 등급 할인과 마찬가지로 정책 객체로 분리했습니다.

```text
PaymentMethodDiscountPolicy
- supports(paymentMethod)
- discount(amount)
```

현재 구현체는 `PointPaymentDiscountPolicy`입니다.

## 결제 흐름

`PaymentService`는 결제 흐름을 조립하는 역할을 맡습니다.

```text
1. 회원 조회
2. 주문 생성 및 저장
3. 등급 할인 계산
4. 등급 할인 후 금액 계산
5. 결제 수단 할인 계산
6. 최종 결제 금액 계산
7. 결제 정보 저장
8. 할인 이력 스냅샷 저장
9. 결제 결과 반환
```

계산 정책과 저장 책임을 분리하여 `PaymentService`가 할인 세부 계산식을 직접 갖지 않도록 했습니다.

## 예외 처리

다음과 같은 잘못된 입력을 방지했습니다.

- 존재하지 않는 회원
- 상품명이 비어 있는 주문
- 주문 금액이 0 이하인 경우
- 회원 등급이 없는 경우
- 결제 수단이 없는 경우
- 할인 금액이 주문 금액보다 큰 경우
- 할인율 또는 할인 금액이 음수인 경우
- 할인 적용 순서가 1보다 작은 경우

## 테스트

### GradeDiscountCalculatorTest

등급별 할인 정책을 검증하는 단위 테스트입니다.

- `NORMAL`: 할인 없음
- `VIP`: 1,000원 고정 할인
- `VVIP`: 주문 금액의 10% 할인

Spring Context를 띄우지 않는 순수 단위 테스트로 작성해 정책 계산 의도를 빠르게 검증했습니다.

### PaymentServiceDuplicateDiscountTest

등급 할인과 결제 수단 할인이 중복 적용될 때의 우선순위와 최종 금액을 검증합니다.

- `VVIP + POINT`: `100,000 -> 90,000 -> 85,500`
- `VVIP + CREDIT_CARD`: 결제 수단 추가 할인 없음

### PaymentDiscountHistoryPersistenceTest

결제 후 회원 등급이 변경되어도 결제 당시 할인 이력이 스냅샷으로 보존되는지 검증합니다.

검증 내용:

- 결제 당시 `VVIP` 등급 할인 이력 유지
- 결제 당시 `POINT` 결제 수단 할인 이력 유지
- `appliedOrder`를 통한 할인 적용 순서 유지

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

특정 테스트 실행:

```bash
./gradlew test --tests GradeDiscountCalculatorTest
./gradlew test --tests PaymentServiceDuplicateDiscountTest
./gradlew test --tests PaymentDiscountHistoryPersistenceTest
```

## 중요하게 생각한 부분

첫 번째로 중요하게 본 부분은 할인 정책의 확장성입니다. 결제 서비스가 등급별 할인 조건을 직접 가지면 정책이 추가될 때마다 결제 흐름이 함께 수정됩니다. 그래서 할인 정책을 인터페이스와 구현체로 분리하고, 계산기는 적용 가능한 정책을 선택하는 역할만 하도록 했습니다.

두 번째는 결제 완료 데이터의 불변성입니다. 결제 데이터는 정산과 이력 관리 관점에서 이후 정책 변경에 의해 달라지면 안 된다고 판단했습니다. 그래서 현재 정책 객체와 과거 결제 이력을 분리하고, 결제 당시 적용된 정책명, 대상, 할인율, 할인 금액을 `PaymentDiscountHistory`에 저장했습니다.

세 번째는 테스트의 의도입니다. 단순히 성공 케이스만 확인하는 것이 아니라, 정책 적용 순서와 과거 이력 보존이라는 요구사항의 핵심을 테스트 이름과 검증 값에 드러내고자 했습니다.
