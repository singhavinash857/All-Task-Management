package com.transformedge.apps.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.NOT_FOUND, false, request.getDescription(false), ex.getMessage(), null);
		List<FieldErrorDTO> errorsList = addErrorDescriptionToErroList(ex.getMessage());
		errorInfo.setErrors(errorsList);
		return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private List<FieldErrorDTO> addErrorDescriptionToErroList(String message) {
		 List<FieldErrorDTO> errorsList = new ArrayList<>();
		 errorsList.add(new FieldErrorDTO(message));
		 return errorsList;
	}

	@ExceptionHandler(PasswordLinkExpiredException.class)
	public final ResponseEntity<?> handlePasswordLinkExpiredException(PasswordLinkExpiredException ex,
			WebRequest request) {
		String errorMessage = ex.getMessage();
		String errorURL = request.getDescription(false);
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.SEE_OTHER, false, errorURL, errorMessage, null);
		List<FieldErrorDTO> errorsList = addErrorDescriptionToErroList(ex.getMessage());
		errorInfo.setErrors(errorsList);
		return new ResponseEntity<>(errorInfo, HttpStatus.SEE_OTHER);
	}
	
	@ExceptionHandler(UserNotFoundException.class)
	public final ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex,
			WebRequest request) {
		String errorMessage = ex.getMessage();
		String errorURL = request.getDescription(false);
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.NOT_FOUND, false, errorURL, errorMessage, null);

		List<FieldErrorDTO> errorsList = addErrorDescriptionToErroList(ex.getMessage());
		errorInfo.setErrors(errorsList);
		return new ResponseEntity<>(errorInfo, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(FileStorageException.class)
	public final ResponseEntity<?> handleFileStorageException(FileStorageException ex,
			WebRequest request) {
		String errorMessage = ex.getMessage();
		String errorURL = request.getDescription(false);
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.NOT_FOUND, false, errorURL, errorMessage, null);

		List<FieldErrorDTO> errorsList = addErrorDescriptionToErroList(ex.getMessage());
		errorInfo.setErrors(errorsList);
		return new ResponseEntity<>(errorInfo, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(MyFileNotFoundException.class)
	public final ResponseEntity<?> handleFileStorageException(MyFileNotFoundException ex,
			WebRequest request) {
		String errorMessage = ex.getMessage();
		String errorURL = request.getDescription(false);
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.NOT_FOUND, false, errorURL, errorMessage, null);

		List<FieldErrorDTO> errorsList = addErrorDescriptionToErroList(ex.getMessage());
		errorInfo.setErrors(errorsList);
		return new ResponseEntity<>(errorInfo, HttpStatus.NOT_FOUND);
	}

	
	@ExceptionHandler(UserAlreadyExistException.class)
	public final ResponseEntity<?> handleUserAlreadyExistException(UserAlreadyExistException ex,
			WebRequest request) {
		String errorMessage = ex.getMessage();
		String errorURL = request.getDescription(false);
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.SEE_OTHER, false, errorURL, errorMessage, null);
		List<FieldErrorDTO> errorsList = addErrorDescriptionToErroList(ex.getMessage());
		errorInfo.setErrors(errorsList);
		return new ResponseEntity<>(errorInfo, HttpStatus.SEE_OTHER);
	}
	
	@ExceptionHandler(TaskAlreadyAssignedToEmployee.class)
	public final ResponseEntity<?> handleTaskAlreadyExistForEmployeeException(TaskAlreadyAssignedToEmployee ex,
			WebRequest request) {
		String errorMessage = ex.getMessage();
		String errorURL = request.getDescription(false);
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.SEE_OTHER, false, errorURL, errorMessage, null);
		List<FieldErrorDTO> errorsList = addErrorDescriptionToErroList(ex.getMessage());
		errorInfo.setErrors(errorsList);
		return new ResponseEntity<>(errorInfo, HttpStatus.SEE_OTHER);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		//String errorMessage = "error.bad.arguments";
		String errorMessage = ex.getMessage();
		String errorURL = request.getDescription(false);

		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.BAD_REQUEST, errorURL, errorMessage);

		BindingResult result = ex.getBindingResult();
		List<FieldError> fieldErrors = result.getFieldErrors();
		errorInfo.getErrors().addAll(populateFieldErrors(fieldErrors));
		return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
	}

	public List<FieldErrorDTO> populateFieldErrors(List<FieldError> fieldErrorList) {
		List<FieldErrorDTO> fieldErrors = new ArrayList<FieldErrorDTO>();
//		StringBuilder errorMessage = new StringBuilder("");
		for (FieldError fieldError : fieldErrorList) {
//			errorMessage.append(fieldError.getCode()).append(".");
//			errorMessage.append(fieldError.getObjectName()).append(".");
//			errorMessage.append(fieldError.getField());

			// String localizedErrorMsg =
			// localizeErrorMessage(errorMessage.toString());

			fieldErrors.add(new FieldErrorDTO(fieldError.getField(), fieldError.getDefaultMessage()));
//			errorMessage.delete(0, errorMessage.capacity());
		}
		return fieldErrors;
	}

	// public String localizeErrorMessage(String errorCode) {
	// Locale locale = LocaleContextHolder.getLocale();
	// String errorMessage = messageSource.getMessage(errorCode, null, locale);
	// return errorMessage;
	// }

}
