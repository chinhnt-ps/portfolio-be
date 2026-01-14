# API cURL Examples

File này chứa các ví dụ cURL commands cho tất cả các API endpoints hiện có.

## 📋 Base URL

- **Local Development**: `http://localhost:8080`
- **Production**: (Cập nhật khi deploy)

## 🔗 Swagger UI

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs

---

## 🔐 Authentication APIs

### 1. Register (Đăng ký)

Đăng ký tài khoản mới. Sau khi đăng ký, hệ thống sẽ gửi verification code qua email.

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123",
    "fullName": "Nguyen Van A"
  }'
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác nhận tài khoản.",
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "status": "INACTIVE"
  }
}
```

---

### 2. Verify Email (Xác nhận email)

Xác nhận email với verification code nhận được từ email.

```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "verificationCode": "123456"
  }'
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Xác nhận thành công. Bạn có thể đăng nhập ngay.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_string",
    "expiresIn": 3600,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "fullName": "Nguyen Van A",
      "status": "ACTIVE",
      "role": "USER"
    }
  }
}
```

---

### 3. Login (Đăng nhập)

Đăng nhập với email và password.

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123"
  }'
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_string",
    "expiresIn": 3600,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "fullName": "Nguyen Van A",
      "status": "ACTIVE",
      "role": "USER"
    }
  }
}
```

**Lưu token để dùng cho các API cần authentication:**
```bash
# Lưu access token vào biến
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
REFRESH_TOKEN="refresh_token_string"
```

---

### 4. Refresh Token (Làm mới token)

Làm mới access token bằng refresh token.

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "refresh_token_string"
  }'
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "new_refresh_token_string",
    "expiresIn": 3600,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "fullName": "Nguyen Van A",
      "status": "ACTIVE",
      "role": "USER"
    }
  }
}
```

---

### 5. Forgot Password (Quên mật khẩu)

Gửi verification code qua email để reset password.

```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Nếu email tồn tại, chúng tôi đã gửi mã xác nhận. Vui lòng kiểm tra email.",
  "data": null
}
```

---

### 6. Reset Password (Đặt lại mật khẩu)

Đặt lại mật khẩu với verification code nhận được từ email.

```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "verificationCode": "123456",
    "newPassword": "NewPassword123"
  }'
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.",
  "data": null
}
```

---

### 7. Logout (Đăng xuất)

Đăng xuất và revoke refresh token. **Cần authentication.**

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d '{
    "refreshToken": "refresh_token_string"
  }'
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đăng xuất thành công",
  "data": null
}
```

---

## 📁 File Management APIs

### 8. Upload File

Upload file/ảnh lên Firebase Storage. **Cần authentication và ADMIN role.**

```bash
curl -X POST http://localhost:8080/api/v1/files/upload \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -F "file=@/path/to/your/image.jpg" \
  -F "folder=uploads"
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Upload thành công",
  "data": {
    "fileId": "uuid",
    "originalName": "image.jpg",
    "fileName": "a1b2c3d4-1234567890.jpg",
    "fileSize": 1024000,
    "mimeType": "image/jpeg",
    "folder": "uploads",
    "publicUrl": "https://firebasestorage.googleapis.com/...",
    "uploadedAt": "2024-01-01T12:00:00"
  }
}
```

**Lưu ý:**
- ⚠️ **Chỉ ADMIN mới có quyền upload file**
- File size tối đa: 10MB
- Supported types: images (jpg, png, gif, webp), documents (pdf, doc, docx)
- `folder` parameter là optional (default: "uploads")

---

### 9. Get File Info

Lấy thông tin file theo fileId. **Cần authentication.**

```bash
curl -X GET http://localhost:8080/api/v1/files/{fileId} \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "fileId": "uuid",
    "originalName": "image.jpg",
    "fileName": "a1b2c3d4-1234567890.jpg",
    "fileSize": 1024000,
    "mimeType": "image/jpeg",
    "folder": "uploads",
    "publicUrl": "https://firebasestorage.googleapis.com/...",
    "uploadedAt": "2024-01-01T12:00:00"
  }
}
```

---

### 10. Delete File

Xóa file từ Firebase Storage và MongoDB. **Cần authentication và chỉ owner mới xóa được.**

```bash
curl -X DELETE http://localhost:8080/api/v1/files/{fileId} \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Xóa file thành công",
  "data": null
}
```

---

## 👑 Admin Management APIs

### 11. Update User Role

Cập nhật role của user (chỉ ADMIN mới có quyền). **Cần authentication và ADMIN role.**

```bash
curl -X PUT http://localhost:8080/api/v1/auth/users/{userId}/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" \
  -d '{
    "role": "ADMIN"
  }'
```

**Request Body:**
- `role`: Role mới cho user (`USER` hoặc `ADMIN`)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cập nhật role thành công",
  "data": {
    "id": "mongodb_id",
    "userId": "uuid",
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "status": "ACTIVE",
    "role": "ADMIN",
    "emailVerified": true,
    "emailVerifiedAt": "2024-01-01T12:00:00",
    "createdAt": "2024-01-01T10:00:00",
    "lastLoginAt": "2024-01-01T12:00:00"
  }
}
```

**Lưu ý:**
- ⚠️ **Chỉ ADMIN mới có quyền cập nhật role**
- Sau khi cập nhật role, user cần login lại để nhận token mới với role mới
- Role có thể là `USER` (mặc định) hoặc `ADMIN`

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Không có quyền truy cập. Vui lòng kiểm tra quyền của tài khoản.",
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Không có quyền truy cập. Vui lòng kiểm tra quyền của tài khoản.",
    "details": null
  },
  "timestamp": "2026-01-13T22:10:10"
}
```

---

## 🏥 Health Check API

### 12. Health Check

Kiểm tra trạng thái của service.

```bash
curl -X GET http://localhost:8080/api/v1/health
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Service is healthy",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-01T12:00:00"
  }
}
```

---

## 📝 Complete Flow Examples

### Flow 1: Đăng ký → Verify → Login → Upload File

```bash
# 1. Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123",
    "fullName": "Nguyen Van A"
  }'

# 2. Check email để lấy verification code (ví dụ: 123456)

# 3. Verify email
curl -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "verificationCode": "123456"
  }'

# 4. Login
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123"
  }')

ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')

# 5. Upload File
curl -X POST http://localhost:8080/api/v1/files/upload \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -F "file=@/path/to/image.jpg" \
  -F "folder=uploads"
```

### Flow 2: Forgot Password → Reset Password

```bash
# 1. Forgot password
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'

# 2. Check email để lấy verification code (ví dụ: 123456)

# 3. Reset password
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "verificationCode": "123456",
    "newPassword": "NewPassword123"
  }'

# 4. Login với password mới
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "NewPassword123"
  }'
```

---

## 🔧 Using Variables (Bash/PowerShell)

### Bash (Linux/Mac/Git Bash)

```bash
# Set base URL
BASE_URL="http://localhost:8080"

# Register
curl -X POST ${BASE_URL}/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123",
    "fullName": "Nguyen Van A"
  }'

# Login và lưu token
RESPONSE=$(curl -s -X POST ${BASE_URL}/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123"
  }')

ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.data.refreshToken')

# Sử dụng token
curl -X POST ${BASE_URL}/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d "{
    \"refreshToken\": \"${REFRESH_TOKEN}\"
  }"
```

### PowerShell (Windows)

```powershell
# Set base URL
$baseUrl = "http://localhost:8080"

# Register
Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/register" `
  -Method Post `
  -ContentType "application/json" `
  -Body (@{
    email = "user@example.com"
    password = "Password123"
    fullName = "Nguyen Van A"
  } | ConvertTo-Json)

# Login và lưu token
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body (@{
    email = "user@example.com"
    password = "Password123"
  } | ConvertTo-Json)

$accessToken = $loginResponse.data.accessToken
$refreshToken = $loginResponse.data.refreshToken

# Sử dụng token
Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/logout" `
  -Method Post `
  -ContentType "application/json" `
  -Headers @{
    Authorization = "Bearer $accessToken"
  } `
  -Body (@{
    refreshToken = $refreshToken
  } | ConvertTo-Json)
```

---

## ⚠️ Error Responses

Tất cả các API đều trả về error response theo format chuẩn:

```json
{
  "success": false,
  "message": "Error message",
  "error": {
    "code": "ERROR_CODE",
    "message": "Detailed error message",
    "details": [
      {
        "field": "email",
        "reason": "INVALID_FORMAT"
      }
    ]
  }
}
```

### Common Error Codes

- `VALIDATION_ERROR`: Dữ liệu đầu vào không hợp lệ
- `USER_NOT_FOUND`: Không tìm thấy user
- `INVALID_CREDENTIALS`: Email hoặc password sai
- `INVALID_VERIFICATION_CODE`: Verification code không đúng hoặc đã hết hạn
- `USER_ALREADY_EXISTS`: Email đã được đăng ký
- `UNAUTHORIZED`: Không có quyền truy cập (thiếu hoặc token không hợp lệ)
- `TOKEN_EXPIRED`: Token đã hết hạn
- `ACCESS_DENIED`: Không đủ quyền (thiếu role cần thiết, ví dụ: ADMIN)
- `RATE_LIMIT_EXCEEDED`: Vượt quá giới hạn số lần request (rate limiting)

---

## 📚 Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/api/v1/health

---

## 💡 Tips

1. **Lưu token**: Sau khi login, lưu `accessToken` và `refreshToken` để dùng cho các API cần authentication.

2. **Token expiration**: Access token có thời hạn (mặc định 1 giờ). Dùng refresh token để lấy access token mới.

3. **Verification code**: Verification code có thời hạn 15 phút. Nếu hết hạn, cần request lại.

4. **Error handling**: Luôn kiểm tra `success` field trong response để biết request có thành công không.

5. **Content-Type**: Luôn set `Content-Type: application/json` cho POST/PUT requests.

6. **Authorization header**: Format: `Authorization: Bearer <access_token>`

7. **Role-based access**: Một số endpoints yêu cầu ADMIN role:
   - File Upload (`POST /api/v1/files/upload`)
   - Update User Role (`PUT /api/v1/auth/users/{userId}/role`)

8. **Role trong JWT token**: JWT token chứa role của user. Sau khi role được cập nhật, user cần login lại để nhận token mới.

9. **Rate Limiting**: Một số endpoints có rate limiting để bảo vệ khỏi abuse:
   - **Login**: 5 attempts/minute
   - **Register**: 3 attempts/hour
   - **Forgot Password**: 3 attempts/hour
   - **File Upload**: 10 requests/minute
   
   Khi vượt quá giới hạn, bạn sẽ nhận được `429 Too Many Requests` với error code `RATE_LIMIT_EXCEEDED`.

---

## 🚦 Rate Limiting

### Rate Limit Rules

Các endpoints sau có rate limiting để bảo vệ khỏi abuse:

| Endpoint | Limit | Window |
|----------|-------|--------|
| `POST /api/v1/auth/login` | 5 requests | 1 minute |
| `POST /api/v1/auth/register` | 3 requests | 1 hour |
| `POST /api/v1/auth/forgot-password` | 3 requests | 1 hour |
| `POST /api/v1/files/upload` | 10 requests | 1 minute |

### Rate Limit Exceeded Response

Khi vượt quá giới hạn, bạn sẽ nhận được:

**Status Code**: `429 Too Many Requests`

**Response:**
```json
{
  "success": false,
  "message": "Quá nhiều lần đăng nhập. Vui lòng thử lại sau 1 phút.",
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Quá nhiều lần đăng nhập. Vui lòng thử lại sau 1 phút.",
    "details": null
  },
  "timestamp": "2026-01-13T22:30:00"
}
```

**Lưu ý:**
- Rate limiting được áp dụng dựa trên IP address
- Sau khi hết thời gian window, bạn có thể thử lại
- Rate limits được reset tự động sau mỗi window

---

**Last Updated**: 2026-01-13
