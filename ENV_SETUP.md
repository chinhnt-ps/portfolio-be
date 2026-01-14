# Environment Variables Setup

File này hướng dẫn cách setup environment variables cho project.

## Tạo file .env

1. Tạo file `.env` trong thư mục `portfolio-be/` (cùng cấp với `pom.xml`)
2. Copy nội dung dưới đây vào file `.env`
3. Điền các giá trị thực tế của bạn

## Template .env

```properties
# ============================================
# Application
# ============================================
APP_ENV=development
PORT=8080

# ============================================
# MongoDB Atlas
# ============================================
# Format: mongodb+srv://username:password@cluster.xxxxx.mongodb.net/auth_service_db?retryWrites=true&w=majority
MONGODB_URI=mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@YOUR_CLUSTER.mongodb.net/auth_service_db?retryWrites=true&w=majority

# ============================================
# JWT Configuration
# ============================================
# Generate a secure secret key (min 256 bits = 32 characters)
# You can generate one using: openssl rand -base64 32
JWT_SECRET=your-secret-key-min-32-characters-change-in-production
JWT_EXPIRATION=3600
REFRESH_TOKEN_EXPIRATION=7

# ============================================
# SendGrid SMTP Configuration
# ============================================
# Hướng dẫn setup: docs/ai/deployment/sendgrid-smtp-setup.md
# 
# SendGrid Free Tier: 100 emails/ngày (3,000 emails/tháng)
# - Không cần verify domain để bắt đầu (có thể dùng email bất kỳ)
# - Verify domain để production tốt hơn (deliverability cao hơn)
#
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
MAIL_FROM=noreply@example.com
MAIL_FROM_NAME=ChinhNT Auth Service

# ============================================
# Firebase Configuration
# ============================================
# Firebase Service Account JSON (toàn bộ JSON string, escape quotes nếu cần)
# Format: {"type":"service_account","project_id":"...",...}
# Lấy từ Firebase Console → Project Settings → Service Accounts → Generate new private key
FIREBASE_SERVICE_ACCOUNT={"type":"service_account","project_id":"chinhnt-ps",...}

# Firebase Storage Bucket Name
# Format: project-id.firebasestorage.app hoặc project-id.appspot.com
# Lấy từ Firebase Console → Project Settings → General → Storage bucket
FIREBASE_STORAGE_BUCKET=chinhnt-ps.firebasestorage.app

# ============================================
# Sentry (Optional - Error Tracking)
# ============================================
# SENTRY_DSN=https://xxxxx@xxxxx.ingest.sentry.io/xxxxx
```

## Cách sử dụng trong IntelliJ IDEA

### Option 1: Sử dụng EnvFile plugin (Recommended)

1. Cài đặt plugin **EnvFile**: File → Settings → Plugins → Search "EnvFile"
2. Tạo Run Configuration:
   - Run → Edit Configurations
   - Click **"+"** → **"Application"**
   - Main class: `com.portfolio.PortfolioApplication`
   - Working directory: `$PROJECT_DIR$/portfolio-be`
   - Trong tab **"EnvFile"**, check **"Enable EnvFile"**
   - Click **"+"** và chọn file `.env` của bạn
3. Apply và Run

### Option 2: Set Environment Variables trong Run Configuration

1. Run → Edit Configurations
2. Tạo hoặc edit Application configuration
3. Trong **"Environment variables"**, thêm từng biến:
   - `MONGODB_URI=your_value`
   - `JWT_SECRET=your_value`
   - etc.

### Option 3: Sử dụng IntelliJ Environment Variables

1. File → Settings → Build, Execution, Deployment → Build Tools → Maven → Runner
2. Trong **"Environment variables"**, thêm các biến

## Generate JWT Secret

Để generate một JWT secret key an toàn:

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

**Linux/Mac:**
```bash
openssl rand -base64 32
```

**Online tool (nếu cần):**
- https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
- Chọn "256-bit" và "Base64"

## Lưu ý

- ⚠️ **KHÔNG BAO GIỜ** commit file `.env` vào git
- File `.env` đã được thêm vào `.gitignore`
- Luôn sử dụng strong password và secret keys
- Thay đổi tất cả default values trước khi deploy production
