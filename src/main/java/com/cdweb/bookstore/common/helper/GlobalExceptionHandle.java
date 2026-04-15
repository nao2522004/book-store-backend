package com.cdweb.bookstore.common.helper;

import java.util.List;
import java.util.stream.Collectors;

import com.cdweb.bookstore.common.helper.exception.ResourceAlreadyExistsException;
import com.cdweb.bookstore.common.helper.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandle {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception ex) {
		System.out.println(ex);
		return ApiResponse.error(HttpStatus.BAD_REQUEST + "", ex.getMessage());
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
		return ApiResponse.error(HttpStatus.BAD_REQUEST + "", ex.getMessage());
	}

	@ExceptionHandler({ ResourceNotFoundException.class, ResourceAlreadyExistsException.class })
	public ResponseEntity<?> handleNotFound(Exception ex) {
		return ApiResponse.error(HttpStatus.BAD_REQUEST + "", ex.getMessage());
	}

	// xu ly ngoai le chu url neu sai dinh dang
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String errorMessage = String.format("Tham số '%s' có giá trị '%s' không đúng định dạng.", ex.getName(),
				ex.getValue());
		return ApiResponse.error(HttpStatus.BAD_REQUEST + "", errorMessage);
	}

	// xu ly validate cho du lieu duoc gui len tu client
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		List<String> errorList = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.toList());
		String errors = String.join("; ", errorList);
		ApiResponse<Object> response = new ApiResponse<>(HttpStatus.BAD_REQUEST, errors, null, "VALIDATION_ERROR");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

}
