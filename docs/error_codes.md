# Custom Error Codes

This document lists custom error codes used across the application's APIs (REST, gRPC).
Consistent error responses help clients understand and handle issues programmatically.

## Format

**REST API Error Response JSON:**
```json
{
  "timestamp": "2024-07-03T12:00:00.000Z", // ISO 8601 timestamp
  "status": 4xx_or_5xx_http_status_code,   // e.g., 400, 401, 403, 404, 500
  "error": "HTTP_STATUS_DESCRIPTION",    // e.g., "Bad Request", "Unauthorized"
  "code": "INTERNAL_ERROR_CODE",         // Custom internal error code (see below)
  "message": "Human-readable error message.",
  "path": "/api/v1/resource/path",       // The request path that caused the error
  "details": [                           // Optional: array of specific field errors or more details
    // { "field": "fieldName", "message": "Specific error for this field." }
  ]
}
```

**gRPC Error Response:**
- gRPC uses standard status codes (e.g., `INVALID_ARGUMENT`, `NOT_FOUND`, `UNAUTHENTICATED`, `PERMISSION_DENIED`, `INTERNAL`).
- Custom error codes and more details can be propagated using `io.grpc.Status.withDescription()` and error metadata (e.g., `io.grpc.protobuf.StatusProto`). The `code` and `message` from the list below can be part of this.

## Error Code Categories

Error codes might be structured, e.g., `COMPONENT_SPECIFIC_ERROR`.
- `GEN_xxxx`: General errors
- `AUTH_xxxx`: Authentication/Authorization errors
- `VALID_xxxx`: Validation errors
- `TENANT_xxxx`: Tenant management errors
- `USER_xxxx`: User management errors
- `ROLE_xxxx`: Role/Permission errors
- `DATA_xxxx`: Data access/persistence errors
- `MCP_xxxx`: Model Context Protocol errors

## Defined Error Codes

This section lists the error codes as defined in `org.example.multi_tenant_app.exceptions.ErrorCode.java`.
The HTTP Status and gRPC Status are typical mappings but can be adjusted by the `ServiceExceptionMapper` or gRPC interceptors based on the specific `ServiceException` instance.

| ErrorCode Enum Value         | Code                 | Default Message                                      | Typical HTTP Status | Typical gRPC Status | Notes                                      |
|------------------------------|----------------------|------------------------------------------------------|---------------------|---------------------|--------------------------------------------|
| **General Errors**           |                      |                                                      |                     |                     |                                            |
| `UNKNOWN_ERROR`              | `ERR_UNKNOWN`        | An unexpected error occurred.                        | 500 Internal Server | INTERNAL            | Catch-all for unexpected server issues.    |
| `VALIDATION_ERROR`           | `ERR_VALIDATION`     | Input validation failed.                             | 400 Bad Request     | INVALID_ARGUMENT    | `details` field should provide specifics.  |
| `RESOURCE_NOT_FOUND`         | `ERR_NOT_FOUND`      | The requested resource was not found.                | 404 Not Found       | NOT_FOUND           | Generic resource not found.                |
| `UNAUTHENTICATED`            | `ERR_UNAUTHENTICATED`| Authentication is required to access this resource.  | 401 Unauthorized    | UNAUTHENTICATED     | Missing or invalid authentication.       |
| `UNAUTHORIZED`               | `ERR_UNAUTHORIZED`   | You are not authorized to perform this action.       | 403 Forbidden       | PERMISSION_DENIED   | Authenticated but lacks permission.        |
| `BAD_REQUEST`                | `ERR_BAD_REQUEST`    | The request was malformed or invalid.                | 400 Bad Request     | INVALID_ARGUMENT    | General bad request.                       |
| **Tenant Specific Errors**   |                      |                                                      |                     |                     |                                            |
| `TENANT_CREATION_FAILED`     | `TEN_001`            | Failed to create tenant.                             | 500 Internal Server | INTERNAL            |                                            |
| `TENANT_UPDATE_FAILED`       | `TEN_002`            | Failed to update tenant.                             | 500 Internal Server | INTERNAL            |                                            |
| `TENANT_NOT_FOUND`           | `TEN_003`            | Tenant not found.                                    | 404 Not Found       | NOT_FOUND           |                                            |
| `TENANT_ID_MISMATCH`         | `TEN_004`            | Tenant ID in path does not match tenant ID in body.  | 400 Bad Request     | INVALID_ARGUMENT    |                                            |
| `TENANT_RESOLUTION_FAILED`   | `TEN_005`            | Could not determine tenant context for the request.  | 400 Bad Request     | FAILED_PRECONDITION | E.g. missing Tenant-ID header when required|
| **User Specific Errors**     |                      |                                                      |                     |                     |                                            |
| `USER_CREATION_FAILED`       | `USR_001`            | Failed to create user.                               | 500 Internal Server | INTERNAL            |                                            |
| `USER_NOT_FOUND`             | `USR_002`            | User not found.                                      | 404 Not Found       | NOT_FOUND           |                                            |
| `USER_UPDATE_FAILED`         | `USR_003`            | Failed to update user.                               | 500 Internal Server | INTERNAL            |                                            |
| `INVALID_CREDENTIALS`        | `USR_004`            | Invalid username or password.                        | 401 Unauthorized    | UNAUTHENTICATED     | For direct credential validation.          |
| **Role & Permission Errors** |                      |                                                      |                     |                     |                                            |
| `ROLE_CREATION_FAILED`       | `ACL_001`            | Failed to create role.                               | 500 Internal Server | INTERNAL            |                                            |
| `ROLE_NOT_FOUND`             | `ACL_002`            | Role not found.                                      | 404 Not Found       | NOT_FOUND           |                                            |
| `PERMISSION_DENIED`          | `ACL_003`            | Permission denied for this operation.                | 403 Forbidden       | PERMISSION_DENIED   |                                            |
| `ROLE_ASSIGNMENT_FAILED`     | `ACL_004`            | Failed to assign role to user.                       | 500 Internal Server | INTERNAL            |                                            |
| `PERMISSION_ASSIGNMENT_FAILED`| `ACL_005`           | Failed to assign permission to role.                 | 500 Internal Server | INTERNAL            |                                            |
| **MCP Specific Errors**      |                      |                                                      |                     |                     |                                            |
| `MCP_TENANT_ID_MISSING`      | `MCP_001`            | The Tenant-ID header is required for MCP requests.   | 400 Bad Request     | INVALID_ARGUMENT    |                                            |
| `MCP_INVALID_TENANT_ID`      | `MCP_002`            | The provided Tenant-ID is invalid or not recognized. | 400 Bad Request     | INVALID_ARGUMENT    |                                            |

*(This list is generated from `ErrorCode.java` and should be kept in sync.)*
