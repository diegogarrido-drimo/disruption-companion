package com.latam.disruption.api;

import com.latam.disruption.application.BookingNotFoundException;
import com.latam.disruption.application.DependencyUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DependencyUnavailableException.class)
    ProblemDetail dependencyUnavailable(DependencyUnavailableException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "No se pudo confirmar la información necesaria para evaluar.");
        p.setProperty("code", "DEPENDENCY_UNAVAILABLE");
        p.setProperty("dependency", e.dependency());
        return p;
    }

    @ExceptionHandler(BookingNotFoundException.class)
    ProblemDetail bookingNotFound(BookingNotFoundException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "No existe información para el bookingId indicado.");
        p.setProperty("code", "BOOKING_NOT_FOUND");
        p.setProperty("bookingId", e.bookingId());
        return p;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail invalidParameter(MethodArgumentTypeMismatchException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Parámetro inválido: " + e.getName());
        p.setProperty("code", "INVALID_PARAMETER");
        return p;
    }
}
