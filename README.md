# 무신사페이먼츠 Backend Engineer 과제 전형 (김민지)

## 서비스 개요
무신사페이먼츠 무료 포인트 시스템(API)입니다.
적립, 적립취소, 사용, 사용취소 기능을 제공하며, 실제 서비스 가능한 수준의 안정성을 고려하여 설계하였습니다.
애플리케이션 실행 시 별도의 DB 설정 없이 샘플 데이터가 자동으로 적재됩니다.

---

## 기술 스택
- Java 21
- Spring Boot 3.x
- Spring Data JPA
- H2 Database (In-Memory)
- Lombok
- Validation

---

## 프로젝트 구조
```
src/main/java/com/example/musinsapayments_test_project
├── controller      # HTTP 요청/응답 처리
├── service         # 비즈니스 로직
├── domain          # JPA 엔티티
├── repository      # DB 접근
├── dto             # 요청/응답 DTO
├── enums           # 도메인 코드 관리 (EarnStatusCode, EarnTypeCode, UsageStatusCode)
├── validator       # 요청 선검증
├── loader          # 애플리케이션 실행 시 샘플 데이터 자동 적재
└── exception       # 예외 처리
```

---

## 설계 내용 및 이유

### 1. 계층형 아키텍처
Controller → Service → Validator → Repository 구조로 관심사를 분리했습니다.
각 계층이 자신의 역할만 담당하도록 설계하여 유지보수성과 테스트 용이성을 높였습니다.

### 2. 선검증 구조 (Validator 분리)
비즈니스 로직 실행 전 요청 유효성 검증을 Validator로 분리하여, Service가 핵심 로직에만 집중할 수 있도록 설계했습니다.
- `PointEarnValidator` : 회원 존재 여부, 만료일 범위, 포인트 키 중복 검증
- `PointEarnCancelValidator` : 적립 내역 존재 여부, 취소/만료/사용 여부 검증
- `PointPolicyValidator` : 1회 최대 적립 금액, 최대 보유 금액 검증
- `PointUseValidator` : 회원/주문 존재 여부, 중복 사용 여부, 잔액 부족 검증
- `PointUsageCancelValidator` : 사용 내역 존재 여부, 취소 가능 상태 여부, 취소 가능 금액 초과 검증

### 3. Enum 코드 관리
포인트 상태 코드를 String 대신 Enum으로 관리하여 유효하지 않은 코드값 입력을 컴파일 시점에 방지합니다.
각 Enum에 비즈니스 로직을 캡슐화하여, 상태 변경 조건이 변경될 경우 Enum만 수정하면 됩니다.
- `EarnStatusCode.isCancellable()` : 적립 취소 가능 여부
- `EarnStatusCode.isUsable()` : 사용 가능 여부
- `UsageStatusCode.isCancellable()` : 사용 취소 가능 여부 (USED, PARTIALLY_CANCELLED)

### 4. 포인트 정책 관리
1회 최대 적립 가능 금액과 최대 보유 가능 금액을 `point_policy` 테이블로 외부화하여 하드코딩 없이 제어합니다.
`point_policy` 는 FK 연결 없이 독립 테이블로 두고, 적립/사용 시 Validator에서 참조하는 구조로 설계했습니다.
정책 변경 시 DB 값만 수정하면 됩니다.

### 5. 포인트 사용 우선순위
포인트 사용 시 아래 우선순위로 차감됩니다.
1. 수기 지급(MANUAL) 포인트 우선 사용
2. 만료일이 짧게 남은 순서로 사용

### 6. 1원 단위 추적
`point_usage_detail` 테이블을 통해 특정 시점에 적립된 포인트가 어떤 주문에서 얼마나 사용되었는지 1원 단위까지 추적합니다.

### 7. 포인트 사용 취소 시 만료 처리
사용 취소 시점에 이미 만료된 포인트는 잔액을 복구하지 않고, 해당 금액만큼 신규 적립 처리합니다.
신규 적립된 포인트 키는 응답의 `newPointKeys` 필드로 반환됩니다.

### 8. 부분 취소 상태 관리
포인트 사용 취소 시 전체 취소 여부에 따라 상태 코드를 구분합니다.
- 전체 취소: `CANCELLED`
- 부분 취소: `PARTIALLY_CANCELLED`

---

## 핵심 문제 해결 전략

### 멱등성 (Idempotency)
- **포인트 적립**: `point_key` 를 PK로 사용하여 중복 적립 요청 차단
    - Validator에서 선검증 후, DB PK 제약으로 이중 차단
    - `point_key` 는 전역 유니크를 보장하는 방식(UUID 등)으로 채번 필요
- **포인트 적립 취소**: 이미 취소된 적립건에 대해 재요청 시 `POINT_EARN_ALREADY_CANCELLED` 오류 반환
- **포인트 사용**: `order_id` 기준으로 이미 포인트가 사용된 주문인지 검증하여 중복 사용 차단
    - 주문시에만 포인트 사용이 가능하므로, 주문 1건당 포인트 사용 1건만 허용
- **포인트 사용 취소**: 이미 전체 취소된 사용건에 대해 재요청 시 `POINT_USAGE_ALREADY_CANCELLED` 오류 반환
    - `UsageStatusCode.isCancellable()` 로 검증하여 CANCELLED 상태인 경우 차단

### 동시성 처리 (비관적 락)
- **포인트 적립 취소**: `findByPointKeyWithLock` 으로 적립건 조회 시 비관적 락 적용
    - 동시에 동일한 적립건에 대한 취소 요청이 들어와도 순차 처리 보장
    - 멱등성이 보장되어 데이터 손상은 없으나, 안전한 운영을 위해 비관적 락 적용
- **포인트 사용**: `findUsablePointsWithLock` 으로 사용 가능 포인트 조회 시 비관적 락 적용
    - 동시에 동일 회원의 포인트 사용 요청이 들어와도 잔액 정합성 보장
- **포인트 사용 취소**: `findByIdWithLock` 으로 사용 내역 조회 시 비관적 락 적용

---

## API 명세

### 포인트 API
| Method | URL | 설명 |
|--------|-----|------|
| POST | /points/earn | 포인트 적립 |
| POST | /points/earn/cancel | 포인트 적립 취소 |
| POST | /points/use | 포인트 사용 |
| POST | /points/use/cancel | 포인트 사용 취소 |

---

### 포인트 적립
**요청 DTO** `EarnPointRequest`
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| memberId | String | Y | 회원 ID |
| pointKey | String | Y | 포인트 키 (중복 불가) |
| earnTypeCode | EarnTypeCode | Y | 적립 구분 코드 (NORMAL, MANUAL) |
| earnAmount | Long | Y | 적립 금액 (1 이상 100,000 이하) |
| expiredDays | Integer | N | 만료일 (1일 이상 5년 미만, 기본값 365일) |

**응답 DTO** `EarnPointResponse`
| 필드 | 타입 | 설명 |
|------|------|------|
| pointKey | String | 포인트 키 |
| memberId | String | 회원 ID |
| earnTypeCode | EarnTypeCode | 적립 구분 코드 |
| earnStatusCode | EarnStatusCode | 적립 상태 코드 |
| earnAmount | Long | 적립 금액 |
| remainingAmount | Long | 잔여 금액 |
| expiredAt | LocalDateTime | 만료일자 |
| earnedAt | LocalDateTime | 적립일자 |

---

### 포인트 적립 취소
**요청 DTO** `CancelEarnRequest`
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| pointKey | String | Y | 포인트 키 |
| cancelReasonCode | String | Y | 취소 사유 코드 |

**응답 DTO** `CancelEarnResponse`
| 필드 | 타입 | 설명 |
|------|------|------|
| pointKey | String | 포인트 키 |
| memberId | String | 회원 ID |
| earnTypeCode | EarnTypeCode | 적립 구분 코드 |
| earnStatusCode | EarnStatusCode | 적립 상태 코드 |
| earnAmount | Long | 적립 금액 |
| remainingAmount | Long | 잔여 금액 |
| earnedAt | LocalDateTime | 적립일자 |
| cancelledAt | LocalDateTime | 취소일자 |

---

### 포인트 사용
**요청 DTO** `UsePointRequest`
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| memberId | String | Y | 회원 ID |
| orderId | String | Y | 주문 ID |
| useAmount | Long | Y | 사용 금액 (1 이상) |

**응답 DTO** `UsePointResponse`
| 필드 | 타입 | 설명 |
|------|------|------|
| usageId | Long | 사용 ID |
| memberId | String | 회원 ID |
| orderId | String | 주문 ID |
| usageStatusCode | UsageStatusCode | 사용 상태 코드 |
| totalAmount | Long | 총 사용 금액 |
| remainingCancelAmount | Long | 취소 가능 잔여 금액 |
| usedAt | LocalDateTime | 사용일자 |

---

### 포인트 사용 취소
**요청 DTO** `CancelUseRequest`
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| usageId | Long | Y | 사용 ID |
| cancelAmount | Long | Y | 취소 금액 (1 이상) |
| cancelReasonCode | String | Y | 취소 사유 코드 |

**응답 DTO** `CancelUseResponse`
| 필드 | 타입 | 설명 |
|------|------|------|
| usageId | Long | 사용 ID |
| memberId | String | 회원 ID |
| orderId | String | 주문 ID |
| usageStatusCode | UsageStatusCode | 사용 상태 코드 |
| totalAmount | Long | 총 사용 금액 |
| remainingCancelAmount | Long | 취소 가능 잔여 금액 |
| cancelledAmount | Long | 취소 금액 |
| newPointKeys | List\<String\> | 만료로 인해 신규 적립된 포인트 키 목록 |
| usedAt | LocalDateTime | 사용일자 |

---

### 에러 응답 샘플
```json
{
  "code": "MEMBER_NOT_FOUND",
  "message": "회원을 찾을 수 없습니다."
}
```

### 에러 코드
| 코드 | 설명 | HTTP 상태 |
|------|------|----------|
| MEMBER_NOT_FOUND | 회원 없음 | 404 |
| ORDER_NOT_FOUND | 주문 없음 | 404 |
| POINT_POLICY_NOT_FOUND | 포인트 정책 없음 | 404 |
| POINT_EARN_NOT_FOUND | 포인트 적립 내역 없음 | 404 |
| POINT_USAGE_NOT_FOUND | 포인트 사용 내역 없음 | 404 |
| EXCEED_MAX_EARN_AMOUNT | 1회 최대 적립 금액 초과 | 400 |
| EXCEED_MAX_HOLD_AMOUNT | 최대 보유 금액 초과 | 400 |
| POINT_EARN_ALREADY_CANCELLED | 적립 취소 내역 중복 | 400 |
| POINT_EARN_ALREADY_EXPIRED | 적립 내역 만료 | 400 |
| POINT_EARN_ALREADY_USED | 사용된 포인트 적립 취소 불가 | 400 |
| POINT_USAGE_ALREADY_CANCELLED | 사용 취소 내역 중복 | 400 |
| DUPLICATE_POINT_KEY | 포인트 키 중복 | 409 |
| DUPLICATE_ORDER_USAGE | 주문 포인트 사용 중복 | 409 |
| INSUFFICIENT_POINT | 포인트 잔액 부족 | 400 |
| EXCEED_REMAINING_CANCEL_AMOUNT | 취소 가능 금액 초과 | 400 |
| INVALID_EXPIRED_DAYS | 만료일 범위 오류 | 400 |
| INVALID_REQUEST | 잘못된 요청 | 400 |
| INTERNAL_SERVER_ERROR | 서버 내부 오류 | 500 |

---

## 한계점 및 고려사항

### 테이블 컬럼 구성
실제 운영 환경에서는 각 테이블에 최종변경일시(`updated_at`), 생성자, 변경자 등 감사(Audit) 컬럼을 포함한 더 많은 컬럼이 관리되나, 과제 범위에 맞게 핵심 컬럼으로만 구성했습니다.

### 만료 포인트 배치 처리
현재 포인트 사용 시 `EXPIRED_AT > NOW()` 조건으로 만료된 포인트를 필터링하고 있습니다.
실제 운영 환경에서는 별도의 배치 작업을 통해 만료된 포인트의 상태를 `EXPIRED` 로 주기적으로 업데이트하는 것이 필요합니다.
이를 통해 데이터 정합성을 유지하고, 만료 포인트 조회 성능을 향상시킬 수 있습니다.

---

## 실행 방법
별도의 DB 설정 없이 애플리케이션 실행만으로 동작합니다.
애플리케이션 시작 시 샘플 데이터(회원 3명, 주문 각 5건, 적립 내역 각 5~7건, 사용 내역 각 2건)가 자동으로 적재됩니다.

### 실행
```bash
./gradlew bootRun
```

### DB 확인
H2 콘솔 접속: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (없음)

### 동작 확인
애플리케이션 실행 후 아래 샘플 요청으로 순서대로 동작을 확인할 수 있습니다.

| 설명 | Method | URL |
|------|--------|-----|
| 포인트 적립 | POST | http://localhost:8080/points/earn |
| 포인트 적립 취소 | POST | http://localhost:8080/points/earn/cancel |
| 포인트 사용 | POST | http://localhost:8080/points/use |
| 포인트 사용 취소 | POST | http://localhost:8080/points/use/cancel |

**1. 포인트 적립**

POST /points/earn
- memberId: M001
- pointKey: PE999
- earnTypeCode: NORMAL
- earnAmount: 5000

**2. 포인트 적립 취소**

POST /points/earn/cancel
- pointKey: PE004
- cancelReasonCode: OWNER_CANCEL

**3. 포인트 사용**

POST /points/use
- memberId: M001
- orderId: O003
- useAmount: 3000

**4. 포인트 사용 취소** (3번 실행 후 생성된 usageId 로 요청)

POST /points/use/cancel
- usageId: 7
- cancelAmount: 1000
- cancelReasonCode: OWNER_CANCEL

> ℹ️ 샘플 데이터는 서버 실행 시 자동으로 적재되며, 서버 재시작 시 초기화됩니다.
> H2 콘솔에서 `SELECT * FROM POINT_USAGE;` 로 usageId 를 확인할 수 있습니다.