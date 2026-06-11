package com.example.demo.finalprojectjavawebservice.aspect;

import com.example.demo.finalprojectjavawebservice.dto.request.BookingRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.BookingResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("PERFORMANCE");
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");

    @Around("""
            execution(public * com.example.demo.finalprojectjavawebservice..*(..))
            && (within(@org.springframework.stereotype.Service *)
                || within(@org.springframework.web.bind.annotation.RestController *))
            """)
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMillis = (System.nanoTime() - startTime) / 1_000_000;
            PERFORMANCE_LOGGER.info(
                    "[PERFORMANCE] {}.{} executed in {} ms",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    durationMillis
            );
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.example.demo.finalprojectjavawebservice.service.BookingService.createBooking(..))",
            returning = "result"
    )
    public void logBookingSuccess(JoinPoint joinPoint, Object result) {
        if (result instanceof BookingResponse booking) {
            AUDIT_LOGGER.info(
                    "[AUDIT - SUCCESS] Customer {} booked court {} from {} to {}, durationMinutes={}, bookingId={}",
                    booking.getCustomerUsername(),
                    booking.getCourtName(),
                    booking.getBookingDate(),
                    booking.getEndTime(),
                    booking.getDurationMinutes(),
                    booking.getId()
            );
        }
    }

    @AfterThrowing(
            pointcut = "execution(* com.example.demo.finalprojectjavawebservice.service.BookingService.createBooking(..))",
            throwing = "exception"
    )
    public void logBookingFailure(JoinPoint joinPoint, Throwable exception) {
        Object[] args = joinPoint.getArgs();
        BookingRequest request = args.length > 0 && args[0] instanceof BookingRequest bookingRequest
                ? bookingRequest
                : null;
        String username = args.length > 1 && args[1] instanceof String value ? value : "unknown";

        AUDIT_LOGGER.warn(
                "[AUDIT - FAILED] Customer {} tried to book courtId={} at {} for {} minutes but failed: {}",
                username,
                request == null ? null : request.getCourtId(),
                request == null ? null : request.getBookingDate(),
                request == null ? null : request.getDurationMinutes(),
                exception.getMessage()
        );
    }
}
