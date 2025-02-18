package kr.hhplus.be.server.api.user.application.service;

import kr.hhplus.be.server.api.common.exception.CustomException;
import kr.hhplus.be.server.api.common.lock.util.RedisPublisher;
import kr.hhplus.be.server.api.user.application.dto.response.UserBalanceResult;
import kr.hhplus.be.server.api.user.domain.entity.User;
import kr.hhplus.be.server.api.user.domain.repository.UserRepository;
import kr.hhplus.be.server.api.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RedisPublisher redisPublisher;

    /**
     * 사용자 잔액을 조회
     * - 사용자가 존재하지 않을 경우 기본값 0을 반환
     */
    public UserBalanceResult getBalance(Long userId) {
        // 사용자 잔액 조회
        try {
            UserBalanceResult result = userRepository.findById(userId)
                    .map(UserBalanceResult::from)
                    .orElseGet(() -> {
                        // 잔액 정보가 없으면 생성 후 반환
                        User newBalance = User.builder().balance(0L).build();
                        userRepository.save(newBalance);
                        log.warn("[UserService] 새로운 사용자 잔액 생성 >> User ID: {}, Initial Balance: 0", userId);
                        return UserBalanceResult.from(newBalance);
                    });
            log.info("[UserService] 잔액 조회 성공 >> User ID: {}, Balance: {}", userId, result.balance());
            return result;
        } catch (Exception e) {
            log.error("[UserService] 잔액 조회 실패 >> User ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 결제 처리
     * - 잔액 확인 후 부족 시 충전
     * - 결제 가능 금액 반환
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long processPayment(Long userId, Long totalAmount) {
        log.info("[UserService] 결제 처리 시작 >> User ID: {}, Total Amount: {}", userId, totalAmount);
        try {
            User user = userRepository.findById(userId)
                    .orElseGet(() -> {
                        User newBalance = User.builder().balance(0L).build();
                        userRepository.save(newBalance);
                        log.info("[UserService] 새로운 사용자 생성 >> User ID: {}", userId);
                        return newBalance;
                    });

            // 부족한 금액 계산
            Long insufficientAmount = totalAmount - user.getBalance();
            if (insufficientAmount > 0) {
                user.chargeBalance(insufficientAmount);
                log.info("[UserService] 부족한 금액 충전 >> User ID: {}, Charged Amount: {}", userId, insufficientAmount);
            }

            // 잔액에서 결제 금액 차감
            user.deductBalance(totalAmount);
            userRepository.save(user);

            log.info("[UserService] 결제 처리 완료 >> User ID: {}, Total Amount: {}", userId, totalAmount);
            return totalAmount;
        } catch (Exception e) {
            log.error("[UserService] 결제 처리 실패 >> User ID: {}, Total Amount: {}", userId, totalAmount, e);
            throw e;
        }
    }

    /**
     * 사용자 잔액을 충전 후, 현재 잔액 반환
     */
    @Transactional
    public Long chargeBalance(Long userId, Long amount) {
        log.info("[UserService] 잔액 충전 시작 >> User ID: {}, Amount: {}", userId, amount);
        try {
            // 사용자 잔액 조회
            User user = userRepository.findById(userId)
                    .orElseGet(() -> {
                        User newUser = User.builder().balance(0L).build();
                        userRepository.save(newUser);
                        return newUser;
                    });

            // 잔액 충전
            user.chargeBalance(amount);
            // 변경된 잔액 정보 저장
            userRepository.save(user);

            log.info("[UserService] 잔액 충전 완료 >> User ID: {}, Current Balance: {}", userId, user.getBalance());

            // 충전 후의 현재 잔액 반환
            return user.getBalance();
        } catch (Exception e) {
            log.error("[UserService] 잔액 충전 실패 >> User ID: {}, Amount: {}", userId, amount, e);
            throw e;
        }
    }

    /**
     * 사용자 잔액 감소 후, 현재 잔액 반환
     */
    @Transactional
    public Long deductBalance(Long userId, Long amount) {
        log.info("[UserService] 잔액 차감 시작 >> User ID: {}, Amount: {}", userId, amount);
        try {
            // 사용자 잔액 조회
            User user = userRepository.findById(userId)
                    .orElseGet(() -> {
                        User newUser = User.builder().balance(0L).build();
                        userRepository.save(newUser);
                        return newUser;
                    });

            // 잔액 차감
            user.deductBalance(amount);
            // 변경된 잔액 정보 저장
            userRepository.save(user);

            log.info("[UserService] 잔액 차감 완료 >> User ID: {}, Current Balance: {}", userId, user.getBalance());

            // 차감 후의 현재 잔액 반환
            return user.getBalance();
        } catch (Exception e) {
            log.error("[UserService] 잔액 차감 실패 >> User ID: {}, Amount: {}", userId, amount, e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refundPayment(Long userId, Long paymentAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        // 환불 진행(지불한 금액만큼 잔액 충전)
        user.chargeBalance(paymentAmount);
        userRepository.save(user);
    }
}
