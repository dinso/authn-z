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

## Common Error Codes

| HTTP Status | gRPC Status        | Code                 | Message                                           | Description                                                                 |
|-------------|--------------------|----------------------|---------------------------------------------------|-----------------------------------------------------------------------------|
| 400         | INVALID_ARGUMENT   | GEN_INVALID_INPUT    | Invalid input provided.                           | General validation error for request payload. `details` may contain specifics. |
| 400         | INVALID_ARGUMENT   | VALID_FIELD_REQUIRED | Field '{fieldName}' is required.                  | A specific required field is missing.                                       |
| 400         | INVALID_ARGUMENT   | VALID_FIELD_FORMAT   | Field '{fieldName}' has an invalid format.        | A specific field has an incorrect format (e.g., email, date).             |
| 401         | UNAUTHENTICATED    | AUTH_UNAUTHENTICATED | Authentication required.                          | Missing, invalid, or expired authentication token.                          |
| 403         | PERMISSION_DENIED  | AUTH_FORBIDDEN       | Access denied.                                    | Authenticated user does not have permission to perform the action.          |
| 404         | NOT_FOUND          | GEN_NOT_FOUND        | Resource not found.                               | The requested resource does not exist.                                      |
| 404         | NOT_FOUND          | TENANT_NOT_FOUND     | Tenant not found.                                 | Specified tenant ID does not correspond to an existing tenant.              |
| 404         | NOT_FOUND          | USER_NOT_FOUND       | User not found.                                   | Specified user ID does not correspond to an existing user in the tenant.    |
| 409         | ALREADY_EXISTS     | GEN_CONFLICT         | Resource already exists.                          | Attempt to create a resource that already exists (e.g., unique constraint). |
| 500         | INTERNAL           | GEN_INTERNAL_ERROR   | An unexpected internal error occurred.            | Generic server-side error. Logged for investigation.                        |
| 503         | UNAVAILABLE        | GEN_SERVICE_UNAVAIL  | Service temporarily unavailable. Please try again. | The service or a downstream dependency is temporarily unavailable.          |

## Tenant & RBAC Specific Error Codes (Examples)

| HTTP Status | gRPC Status        | Code                  | Message                                      |
|-------------|--------------------|-----------------------|----------------------------------------------|
| 400         | INVALID_ARGUMENT   | TENANT_ID_MISSING     | Tenant-ID header/parameter is missing.       |
| 403         | PERMISSION_DENIED  | TENANT_ACCESS_DENIED  | Access to this tenant is not permitted.      |
| 400         | INVALID_ARGUMENT   | ROLE_NAME_INVALID     | Role name is invalid or contains errors.     |
| 409         | ALREADY_EXISTS     | ROLE_ALREADY_EXISTS   | A role with this name already exists.        |
| 404         | NOT_FOUND          | ROLE_NOT_FOUND        | Role not found.                              |
| 400         | INVALID_ARGUMENT   | PERMISSION_INVALID    | One or more permissions are invalid.         |

*(This list will be expanded as the application is developed.)*
