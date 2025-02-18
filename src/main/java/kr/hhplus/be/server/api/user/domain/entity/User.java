package kr.hhplus.be.server.api.user.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.api.common.exception.CustomException;
import kr.hhplus.be.server.api.user.exception.BalanceErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long balance; // 확장 될 경우 BigDecimal 사용

    /**
     * 잔액 충전
     */
    public void chargeBalance(Long amount) {
        if(amount <= 0) {
            throw new CustomException(BalanceErrorCode.INVALID_CHARGE_AMOUNT);
        }
        this.balance += amount;
    }

    /**
     * 잔액 차감
     */
    public void deductBalance(Long amount) {
        if(amount <= 0) {
            throw new CustomException(BalanceErrorCode.INVALID_DEDUCT_AMOUNT);
        }
        if (this.balance < amount) {
            throw new CustomException(BalanceErrorCode.BALANCE_INSUFFICIENT);
        }
        this.balance -= amount;
    }

}