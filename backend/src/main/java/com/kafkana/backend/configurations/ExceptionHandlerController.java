package com.kafkana.backend.configurations;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.concurrent.TimeoutException;


@ControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleAll(Exception exception, WebRequest request) throws Exception {
        HttpStatus status = getStatus(exception);
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw exception;
        }
        return handleExceptionInternal(exception, exception.getMessage(), new HttpHeaders(), status, request);
    }

    private HttpStatus getStatus(Exception exception) {
        if (exception instanceof NotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof ExecutionControl.NotImplementedException) {
            return HttpStatus.NOT_IMPLEMENTED;
        }
        if (
                exception instanceof MissingServletRequestParameterException ||
                exception instanceof InvalidDataAccessApiUsageException ||
                exception instanceof PropertyReferenceException ||
                exception instanceof MethodArgumentNotValidException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof ResourceAccessException) {
            return HttpStatus.BAD_GATEWAY;
        }
        if (exception instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        if (exception instanceof TimeoutException) {
            return HttpStatus.REQUEST_TIMEOUT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
