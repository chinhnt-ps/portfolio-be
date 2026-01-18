# Wallet Backend APIs - Test Index

**Tổng hợp các test files và APIs theo từng phase**

---

## 📁 Test Files

### Phase 1 & 2 APIs
- **Script**: `test-phase2-api.ps1` (bao gồm Phase 1 + Phase 2)
- **cURL Commands**: `WALLET_API_TEST.md`

### Phase 3 APIs
- **Script**: `test-phase3-api.ps1`
- **cURL Commands**: `PHASE3_API_CURL.md`

---

## 📋 APIs theo Phase

### Phase 1: Core Wallet APIs

#### Accounts Management
- `GET /api/v1/wallet/accounts` - List accounts (paginated)
- `POST /api/v1/wallet/accounts` - Create account
- `GET /api/v1/wallet/accounts/{id}` - Get account
- `PUT /api/v1/wallet/accounts/{id}` - Update account
- `DELETE /api/v1/wallet/accounts/{id}` - Delete account (soft delete)

#### Categories Management
- `GET /api/v1/wallet/categories` - List categories (includes default)
- `POST /api/v1/wallet/categories` - Create category
- `GET /api/v1/wallet/categories/{id}` - Get category
- `PUT /api/v1/wallet/categories/{id}` - Update category
- `DELETE /api/v1/wallet/categories/{id}` - Delete category (soft delete)

#### Transactions Management
- `GET /api/v1/wallet/transactions` - List transactions (filters, pagination, sort)
- `POST /api/v1/wallet/transactions` - Create transaction
- `GET /api/v1/wallet/transactions/{id}` - Get transaction
- `PUT /api/v1/wallet/transactions/{id}` - Update transaction
- `DELETE /api/v1/wallet/transactions/{id}` - Delete transaction (soft delete)

**Filters**: type, categoryId, accountId, startDate, endDate, minAmount, maxAmount, keyword

---

### Phase 2: Reports & Analytics

#### Dashboard Reports
- `GET /api/v1/wallet/reports/dashboard?period={day|week|month|year}` - Dashboard summary

**Response includes:**
- Total income/expense
- Net savings
- Account balances
- Top categories

#### Budgets Management
- `GET /api/v1/wallet/budgets` - List budgets (paginated)
- `POST /api/v1/wallet/budgets` - Create budget
- `GET /api/v1/wallet/budgets/{id}` - Get budget
- `GET /api/v1/wallet/budgets/month?month={YYYY-MM}` - Get budgets by month
- `PUT /api/v1/wallet/budgets/{id}` - Update budget
- `DELETE /api/v1/wallet/budgets/{id}` - Delete budget (soft delete)

**Features:**
- Total budget (1 per month)
- Category budgets (multiple per month)
- Auto-calculate usedAmount from transactions
- Auto-calculate remainingAmount and percentageUsed

---

### Phase 3: Advanced Features

#### Receivables Management (Cho vay)
- `GET /api/v1/wallet/receivables` - List receivables (paginated)
- `POST /api/v1/wallet/receivables` - Create receivable
- `GET /api/v1/wallet/receivables/{id}` - Get receivable
- `PUT /api/v1/wallet/receivables/{id}` - Update receivable
- `DELETE /api/v1/wallet/receivables/{id}` - Delete receivable (soft delete)

**Features:**
- Auto-update status (OPEN → PARTIALLY_PAID → PAID → OVERDUE)
- Auto-detect overdue when dueAt < now and status != PAID

#### Liabilities Management (Nợ)
- `GET /api/v1/wallet/liabilities` - List liabilities (paginated)
- `POST /api/v1/wallet/liabilities` - Create liability
- `GET /api/v1/wallet/liabilities/{id}` - Get liability
- `PUT /api/v1/wallet/liabilities/{id}` - Update liability
- `DELETE /api/v1/wallet/liabilities/{id}` - Delete liability (soft delete)

**Features:**
- Auto-update status tương tự Receivables

#### Settlements Management (Thanh toán)
- `GET /api/v1/wallet/settlements` - List settlements (paginated)
- `POST /api/v1/wallet/settlements` - Create settlement
- `GET /api/v1/wallet/settlements/{id}` - Get settlement
- `GET /api/v1/wallet/settlements/receivable/{receivableId}` - Get settlements for receivable
- `GET /api/v1/wallet/settlements/liability/{liabilityId}` - Get settlements for liability
- `PUT /api/v1/wallet/settlements/{id}` - Update settlement
- `DELETE /api/v1/wallet/settlements/{id}` - Delete settlement (soft delete)

**Features:**
- Validation: tổng settlements <= amount gốc
- Auto-update paidAmount và status của Receivable/Liability

#### Assets Management (Sở hữu)
- `GET /api/v1/wallet/assets` - List assets (paginated)
- `POST /api/v1/wallet/assets` - Create asset
- `GET /api/v1/wallet/assets/{id}` - Get asset
- `GET /api/v1/wallet/assets/total-value` - Get total asset value
- `PUT /api/v1/wallet/assets/{id}` - Update asset
- `DELETE /api/v1/wallet/assets/{id}` - Delete asset (soft delete)

**Asset Types**: CASH, ITEM, DEVICE, OTHER

---

## 🧪 Quick Start

### Test Phase 1 & 2
```powershell
cd portfolio-be
.\test-phase2-api.ps1
```

### Test Phase 3
```powershell
cd portfolio-be
.\test-phase3-api.ps1
```

---

## 📚 Documentation Files

- `API_TEST_INDEX.md` - This file (tổng hợp APIs theo phase)
- `WALLET_API_TEST.md` - Chi tiết cURL commands cho Phase 1 & 2
- `PHASE3_API_CURL.md` - Chi tiết cURL commands cho Phase 3
- `API_CURL_EXAMPLES.md` - cURL examples cho Auth & File APIs (non-wallet)
- `README.md` - Project documentation chính
- `ENV_SETUP.md` - Hướng dẫn setup environment variables
- `MAVEN_SETUP.md` - Hướng dẫn setup Maven

---

**Last Updated**: 2025-01-XX
