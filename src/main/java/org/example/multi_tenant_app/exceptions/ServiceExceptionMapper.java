package org.example.multi_tenant_app.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@Provider // This annotation registers the mapper with JAX-RS
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    private static final Logger LOG = Logger.getLogger(ServiceExceptionMapper.class);

    @Override
    public Response toResponse(ServiceException exception) {
        LOG.warnf(exception, "ServiceException caught: Code=%s, HTTPStatus=%d, Message=%s, Details=%s",
                exception.getErrorCode().getCode(),
                exception.getHttpStatus().getStatusCode(),
                exception.getMessage(),
                exception.getDetails());

        ErrorResponse errorResponse = new ErrorResponse(
                exception.getErrorCode().getCode(),
                exception.getMessage(), // Use the message from the exception, which might be more specific
                exception.getDetails()
        );

        return Response.status(exception.getHttpStatus())
                       .entity(errorResponse)
                       .type("application/json")
                       .build();
    }

    // Inner class to define the structured error response
    public static class ErrorResponse {
        private String error; // Could also be 'status' or 'type'
        private String code; // Internal error code
        private String message; // Human-readable message
        private String details; // Optional additional details

        public ErrorResponse(String code, String message, String details) {
            // Using a common field like "error" or "issue" for the general problem category
            this.error = code.startsWith("ERR_") ? "GeneralError" : code.substring(0, code.indexOf("_")) + "Error";
            this.code = code;
            this.message = message;
            if (details != null && !details.trim().isEmpty()) {
                this.details = details;
            }
        }

        // Getters are needed for Jackson serialization
        public String getError() {
            return error;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public String getDetails() {
            return details;
        }
    }
}
