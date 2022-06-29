package com.esteban.products.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
public class ProductsResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ MissingStockException.class })
    public ResponseEntity<MissingMessage> handleAccessDeniedException(
            MissingStockException ex, WebRequest request) {
        MissingMessage missingMessage = MissingMessage.builder()
                .error("There are quantity products unavailable")
                .items(ex.getItems())
                .build();
        return new ResponseEntity<MissingMessage>(
                missingMessage, new HttpHeaders(), HttpStatus.CONFLICT);
    }

}