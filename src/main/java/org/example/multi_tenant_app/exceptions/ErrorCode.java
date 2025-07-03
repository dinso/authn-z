package org.example.multi_tenant_app.exceptions;

public enum ErrorCode {
    // General Errors
    UNKNOWN_ERROR("ERR_UNKNOWN", "An unexpected error occurred."),
    VALIDATION_ERROR("ERR_VALIDATION", "Input validation failed."),
    RESOURCE_NOT_FOUND("ERR_NOT_FOUND", "The requested resource was not found."),
    UNAUTHENTICATED("ERR_UNAUTHENTICATED", "Authentication is required to access this resource."),
    UNAUTHORIZED("ERR_UNAUTHORIZED", "You are not authorized to perform this action."),
    BAD_REQUEST("ERR_BAD_REQUEST", "The request was malformed or invalid."),

    // Tenant Specific Errors
    TENANT_CREATION_FAILED("TEN_001", "Failed to create tenant."),
    TENANT_UPDATE_FAILED("TEN_002", "Failed to update tenant."),
    TENANT_NOT_FOUND("TEN_003", "Tenant not found."),
    TENANT_ID_MISMATCH("TEN_004", "Tenant ID in path does not match tenant ID in body."),
    TENANT_RESOLUTION_FAILED("TEN_005", "Could not determine tenant context for the request."),


    // User Specific Errors
    USER_CREATION_FAILED("USR_001", "Failed to create user."),
    USER_NOT_FOUND("USR_002", "User not found."),
    USER_UPDATE_FAILED("USR_003", "Failed to update user."),
    INVALID_CREDENTIALS("USR_004", "Invalid username or password."), // Typically for direct login, less so with OIDC token validation

    // Role & Permission Errors
    ROLE_CREATION_FAILED("ACL_001", "Failed to create role."),
    ROLE_NOT_FOUND("ACL_002", "Role not found."),
    PERMISSION_DENIED("ACL_003", "Permission denied for this operation."),
    ROLE_ASSIGNMENT_FAILED("ACL_004", "Failed to assign role to user."),
    PERMISSION_ASSIGNMENT_FAILED("ACL_005", "Failed to assign permission to role."),

    // MCP Specific Errors
    MCP_TENANT_ID_MISSING("MCP_001", "The Tenant-ID header is required for MCP requests."),
    MCP_INVALID_TENANT_ID("MCP_002", "The provided Tenant-ID is invalid or not recognized.");


    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
