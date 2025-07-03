package org.example.multi_tenant_app.exceptions;

import jakarta.ws.rs.core.Response;

public class ServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Response.Status httpStatus;
    private final String details;

    // 2-argument constructor
    public ServiceException(ErrorCode errorCode, Response.Status httpStatus) {
        this(errorCode, httpStatus, errorCode.getDefaultMessage()); // Calls 3-arg
    }

    // 3-argument constructor
    public ServiceException(ErrorCode errorCode, Response.Status httpStatus, String message) {
        this(errorCode, httpStatus, message, (String) null); // Calls 4-arg (details)
    }

    // 4-argument constructor (with details)
    public ServiceException(ErrorCode errorCode, Response.Status httpStatus, String message, String details) {
        this(errorCode, httpStatus, message, details, null); // Calls 5-arg (cause = null)
    }

    // 4-argument constructor (with cause) - Restored
    public ServiceException(ErrorCode errorCode, Response.Status httpStatus, String message, Throwable cause) {
        this(errorCode, httpStatus, message, null, cause); // Calls 5-arg (details = null)
    }

    // 5-argument constructor (Primary)
    public ServiceException(ErrorCode errorCode, Response.Status httpStatus, String message, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Response.Status getHttpStatus() {
        return httpStatus;
    }

    public String getDetails() {
        return details;
    }

    // Static factory methods for common exceptions

    public static ServiceException notFound(String resourceName) {
        return new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND,
                "The resource '" + resourceName + "' was not found.");
    }

    public static ServiceException validationFailed(String details) {
        return new ServiceException(ErrorCode.VALIDATION_ERROR, Response.Status.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details);
    }

    public static ServiceException badRequest(String message) {
        return new ServiceException(ErrorCode.BAD_REQUEST, Response.Status.BAD_REQUEST, message);
    }

    public static ServiceException unauthorized() {
        return new ServiceException(ErrorCode.UNAUTHORIZED, Response.Status.FORBIDDEN, ErrorCode.UNAUTHORIZED.getDefaultMessage());
    }
     public static ServiceException tenantNotFound(String tenantId) {
        return new ServiceException(ErrorCode.TENANT_NOT_FOUND, Response.Status.NOT_FOUND,
                "Tenant with ID '" + tenantId + "' not found.", "Tenant ID: " + tenantId);
    }
}
