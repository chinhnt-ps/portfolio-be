# Wallet API - Test Plan & cURL Commands

**Last Updated**: 2025-01-XX  
**Base URL**: `http://localhost:8080`  
**API Prefix**: `/api/v1`

---

## 📋 Test Plan Overview

### Test Phases

1. **Phase 1: Authentication** - Login để lấy JWT token
2. **Phase 2: Accounts Management** - CRUD operations
3. **Phase 3: Categories Management** - CRUD operations + default categories
4. **Phase 4: Transactions Management** - CRUD + filters
5. **Phase 5: Dashboard Reports** - Dashboard summary
6. **Phase 6: Budgets Management** - CRUD operations

---

## 🔐 Phase 1: Authentication

### Step 1.1: Register (nếu chưa có account)

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123456!",
    "fullName": "Test User"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác nhận tài khoản.",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "expiresIn": 3600,
    "user": {...}
  }
}
```

### Step 1.2: Verify Email (nếu cần)

```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "123456"
  }'
```

### Step 1.3: Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123456!"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "expiresIn": 3600,
    "user": {
      "id": "user-id-here",
      "email": "test@example.com",
      "fullName": "Test User",
      "status": "ACTIVE",
      "role": "USER"
    }
  }
}
```

**⚠️ Lưu lại `accessToken` để dùng cho các requests sau!**

**Set token vào biến (PowerShell):**
```powershell
$TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Set token vào biến (Bash):**
```bash
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 💰 Phase 2: Accounts Management

### Test 2.1: Create Account

```bash
curl -X POST http://localhost:8080/api/v1/wallet/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Ví tiền mặt",
    "type": "CASH",
    "currency": "VND",
    "openingBalance": 1000000,
    "note": "Ví tiền mặt chính"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Account created successfully",
  "data": {
    "id": "account-id-1",
    "name": "Ví tiền mặt",
    "type": "CASH",
    "currency": "VND",
    "openingBalance": 1000000,
    "note": "Ví tiền mặt chính",
    "createdAt": "2025-01-XX...",
    "updatedAt": "2025-01-XX..."
  }
}
```

**Lưu lại `id` của account để dùng cho các test sau!**

### Test 2.2: Create More Accounts

```bash
# Bank account
curl -X POST http://localhost:8080/api/v1/wallet/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Ngân hàng ABC",
    "type": "BANK",
    "currency": "VND",
    "openingBalance": 5000000,
    "note": "Tài khoản ngân hàng chính"
  }'

# E-wallet
curl -X POST http://localhost:8080/api/v1/wallet/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Ví MoMo",
    "type": "E_WALLET",
    "currency": "VND",
    "openingBalance": 500000,
    "note": "Ví điện tử"
  }'
```

### Test 2.3: Get All Accounts

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/accounts?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 2.4: Get Account By ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/accounts/{account-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Test 2.5: Update Account

```bash
curl -X PUT http://localhost:8080/api/v1/wallet/accounts/{account-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Ví tiền mặt (Updated)",
    "note": "Updated note"
  }'
```

### Test 2.6: Delete Account (Soft Delete)

```bash
curl -X DELETE http://localhost:8080/api/v1/wallet/accounts/{account-id} \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📁 Phase 3: Categories Management

### Test 3.1: Get All Categories (should include default categories)

```bash
curl -X GET http://localhost:8080/api/v1/wallet/categories \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:** Should include 12 default categories (Ăn uống, Di chuyển, Mua sắm, etc.)

### Test 3.2: Get Category By ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/categories/{category-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Test 3.3: Create Custom Category

```bash
curl -X POST http://localhost:8080/api/v1/wallet/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Quà tặng",
    "icon": "Gift",
    "color": "#FF6B9D"
  }'
```

**Lưu lại `id` của category để dùng cho transactions!**

### Test 3.4: Update Category

```bash
curl -X PUT http://localhost:8080/api/v1/wallet/categories/{category-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Quà tặng (Updated)",
    "color": "#FF0000"
  }'
```

### Test 3.5: Try to Update System Category (should fail)

```bash
# Try to update a system category (should return error)
curl -X PUT http://localhost:8080/api/v1/wallet/categories/{system-category-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Changed System Category"
  }'
```

**Expected:** Error message "Cannot update system category"

### Test 3.6: Delete Category

```bash
curl -X DELETE http://localhost:8080/api/v1/wallet/categories/{category-id} \
  -H "Authorization: Bearer $TOKEN"
```

---

## 💸 Phase 4: Transactions Management

### Test 4.1: Create Expense Transaction

```bash
curl -X POST http://localhost:8080/api/v1/wallet/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "type": "EXPENSE",
    "amount": 50000,
    "currency": "VND",
    "occurredAt": "2025-01-15T10:30:00",
    "categoryId": "{category-id}",
    "accountId": "{account-id}",
    "note": "Mua đồ ăn trưa"
  }'
```

**Lưu lại `id` của transaction!**

### Test 4.2: Create Income Transaction

```bash
curl -X POST http://localhost:8080/api/v1/wallet/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "type": "INCOME",
    "amount": 10000000,
    "currency": "VND",
    "occurredAt": "2025-01-01T09:00:00",
    "categoryId": "{income-category-id}",
    "accountId": "{account-id}",
    "note": "Lương tháng 1"
  }'
```

### Test 4.3: Create Transfer Transaction

```bash
curl -X POST http://localhost:8080/api/v1/wallet/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "type": "TRANSFER",
    "amount": 2000000,
    "currency": "VND",
    "occurredAt": "2025-01-10T14:00:00",
    "fromAccountId": "{account-id-1}",
    "toAccountId": "{account-id-2}",
    "note": "Chuyển tiền từ ví sang ngân hàng"
  }'
```

### Test 4.4: Get All Transactions (with pagination)

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/transactions?page=0&size=20&sort=occurredAt,desc" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 4.5: Filter Transactions by Type

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/transactions?type=EXPENSE&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 4.6: Filter Transactions by Date Range

```bash
# Note: Date format may need URL encoding
curl -X GET "http://localhost:8080/api/v1/wallet/transactions?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Or use ISO format
curl -X GET "http://localhost:8080/api/v1/wallet/transactions?startDate=2025-01-01T00:00:00.000Z&endDate=2025-01-31T23:59:59.999Z&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 4.7: Filter Transactions by Category

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/transactions?categoryId={category-id}&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 4.8: Filter Transactions by Account

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/transactions?accountId={account-id}&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 4.9: Filter Transactions by Amount Range

```bash
# Note: Amount values as numbers (no currency symbol)
curl -X GET "http://localhost:8080/api/v1/wallet/transactions?minAmount=100000&maxAmount=1000000&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 4.10: Search Transactions by Keyword

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/transactions?keyword=ăn&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 4.11: Get Transaction By ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/transactions/{transaction-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Test 4.12: Update Transaction

```bash
curl -X PUT http://localhost:8080/api/v1/wallet/transactions/{transaction-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 60000,
    "note": "Updated note"
  }'
```

### Test 4.13: Delete Transaction

```bash
curl -X DELETE http://localhost:8080/api/v1/wallet/transactions/{transaction-id} \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 Phase 5: Dashboard Reports

### Test 5.1: Get Dashboard Report (Month)

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/reports/dashboard?period=month" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Dashboard report retrieved successfully",
  "data": {
    "totalIncome": 10000000,
    "totalExpense": 50000,
    "netSavings": 9950000,
    "accountsOverview": [
      {
        "accountId": "...",
        "accountName": "Ví tiền mặt",
        "balance": 995000
      }
    ],
    "topCategories": [
      {
        "categoryId": "...",
        "categoryName": "Ăn uống",
        "totalAmount": 50000,
        "transactionCount": 1
      }
    ]
  }
}
```

### Test 5.2: Get Dashboard Report (Week)

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/reports/dashboard?period=week" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 5.3: Get Dashboard Report (Day)

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/reports/dashboard?period=day" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 5.4: Get Dashboard Report (Year)

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/reports/dashboard?period=year" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 💵 Phase 6: Budgets Management

### Test 6.1: Create Total Budget (for month)

```bash
curl -X POST http://localhost:8080/api/v1/wallet/budgets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "month": "2025-01",
    "amount": 5000000
  }'
```

**Note:** `categoryId` is null for total budget

### Test 6.2: Create Category Budget

```bash
curl -X POST http://localhost:8080/api/v1/wallet/budgets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "month": "2025-01",
    "categoryId": "{category-id}",
    "amount": 1000000
  }'
```

### Test 6.3: Try to Create Duplicate Total Budget (should fail)

```bash
# Should return error: "Total budget already exists for this month"
curl -X POST http://localhost:8080/api/v1/wallet/budgets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "month": "2025-01",
    "amount": 6000000
  }'
```

### Test 6.4: Get All Budgets

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/budgets?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 6.5: Get Budgets by Month

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/budgets/month?month=2025-01" \
  -H "Authorization: Bearer $TOKEN"
```

### Test 6.6: Get Budget By ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/budgets/{budget-id} \
  -H "Authorization: Bearer $TOKEN"
```

**Expected:** Should include `usedAmount`, `remainingAmount`, `percentageUsed` calculated from transactions

### Test 6.7: Update Budget

```bash
curl -X PUT http://localhost:8080/api/v1/wallet/budgets/{budget-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 6000000
  }'
```

### Test 6.8: Delete Budget

```bash
curl -X DELETE http://localhost:8080/api/v1/wallet/budgets/{budget-id} \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🧪 Test Scenarios

### Scenario 1: Complete Flow Test

1. Login → Get token
2. Create 2 accounts (Cash, Bank)
3. Create 5 expense transactions
4. Create 2 income transactions
5. Create 1 transfer transaction
6. Get dashboard report → Verify totals
7. Create total budget for current month
8. Get budget → Verify `usedAmount` is calculated correctly

### Scenario 2: Filter Test

1. Create multiple transactions with different:
   - Types (EXPENSE, INCOME, TRANSFER)
   - Categories
   - Accounts
   - Dates
   - Amounts
2. Test all filter combinations
3. Verify results match filters

### Scenario 3: Validation Test

1. Try to create transaction without required fields → Should fail
2. Try to create TRANSFER without fromAccountId/toAccountId → Should fail
3. Try to create duplicate total budget → Should fail
4. Try to update system category → Should fail
5. Try to delete system category → Should fail

### Scenario 4: User Isolation Test

1. Login as User A → Create accounts/transactions
2. Login as User B → Should not see User A's data
3. Try to access User A's account by ID → Should return 404

---

## 📝 Test Checklist

### Accounts
- [ ] Create account (all types: CASH, BANK, E_WALLET, OTHER)
- [ ] Get all accounts (paginated)
- [ ] Get account by ID
- [ ] Update account
- [ ] Delete account (soft delete)
- [ ] Verify deleted account doesn't appear in list

### Categories
- [ ] Get all categories (should include 12 default)
- [ ] Get category by ID
- [ ] Create custom category
- [ ] Update custom category
- [ ] Try to update system category (should fail)
- [ ] Delete custom category
- [ ] Try to delete system category (should fail)

### Transactions
- [ ] Create EXPENSE transaction
- [ ] Create INCOME transaction
- [ ] Create TRANSFER transaction
- [ ] Get all transactions (paginated, sorted)
- [ ] Filter by type
- [ ] Filter by category
- [ ] Filter by account
- [ ] Filter by date range
- [ ] Filter by amount range
- [ ] Search by keyword
- [ ] Get transaction by ID
- [ ] Update transaction
- [ ] Delete transaction
- [ ] Validate TRANSFER requires fromAccountId/toAccountId
- [ ] Validate EXPENSE/INCOME require accountId/categoryId

### Reports
- [ ] Get dashboard report (month)
- [ ] Get dashboard report (week)
- [ ] Get dashboard report (day)
- [ ] Get dashboard report (year)
- [ ] Verify totals match transactions
- [ ] Verify account balances are calculated correctly
- [ ] Verify top categories are correct

### Budgets
- [ ] Create total budget
- [ ] Create category budget
- [ ] Try to create duplicate total budget (should fail)
- [ ] Get all budgets
- [ ] Get budgets by month
- [ ] Get budget by ID
- [ ] Verify `usedAmount` is calculated from transactions
- [ ] Verify `remainingAmount` and `percentageUsed` are correct
- [ ] Update budget
- [ ] Delete budget

---

## 🐛 Common Issues & Debugging

### Issue 1: 401 Unauthorized
**Cause:** Token expired or invalid  
**Solution:** Login again to get new token

### Issue 2: 404 Not Found
**Cause:** Resource doesn't exist or belongs to another user  
**Solution:** Check ID and verify user owns the resource

### Issue 3: 400 Bad Request
**Cause:** Validation error  
**Solution:** Check request body matches DTO requirements

### Issue 4: 409 Conflict
**Cause:** Duplicate resource (e.g., duplicate total budget)  
**Solution:** Check business rules

### Issue 5: YearMonth parsing error
**Cause:** Wrong format  
**Solution:** Use format `YYYY-MM` (e.g., `2025-01`)

---

## 📚 Additional Resources

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api-docs`
- **Health Check**: `http://localhost:8080/api/v1/health`

---

**Happy Testing! 🚀**
