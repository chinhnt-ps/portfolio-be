# Phase 3 APIs - cURL Test Commands

**Base URL**: `http://localhost:8080`  
**API Prefix**: `/api/v1`

---

## 🔐 Prerequisites

### Login & Get Token

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123456!"
  }'

# Set token (Bash)
export TOKEN="your-token-here"

# Set token (PowerShell)
$TOKEN = "your-token-here"
```

---

## 💰 Receivables (Cho vay) APIs

### Create Receivable

```bash
curl -X POST http://localhost:8080/api/v1/wallet/receivables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "counterpartyName": "Nguyen Van A",
    "amount": 5000000,
    "currency": "VND",
    "occurredAt": "2025-01-15T10:00:00",
    "dueAt": "2025-02-15T23:59:59",
    "note": "Cho vay tiền mua nhà"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Receivable created successfully",
  "data": {
    "id": "receivable-id",
    "counterpartyName": "Nguyen Van A",
    "amount": 5000000,
    "status": "OPEN",
    "paidAmount": 0,
    "remainingAmount": 5000000,
    "isOverdue": false,
    ...
  }
}
```

### Get All Receivables

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/receivables?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Receivable By ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/receivables/{receivable-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Update Receivable

```bash
curl -X PUT http://localhost:8080/api/v1/wallet/receivables/{receivable-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "counterpartyName": "Nguyen Van A (Updated)",
    "amount": 6000000,
    "note": "Updated note"
  }'
```

### Delete Receivable

```bash
curl -X DELETE http://localhost:8080/api/v1/wallet/receivables/{receivable-id} \
  -H "Authorization: Bearer $TOKEN"
```

---

## 💸 Liabilities (Nợ) APIs

### Create Liability

```bash
curl -X POST http://localhost:8080/api/v1/wallet/liabilities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "counterpartyName": "Tran Thi B",
    "amount": 3000000,
    "currency": "VND",
    "occurredAt": "2025-01-10T09:00:00",
    "dueAt": "2025-03-10T23:59:59",
    "note": "Vay tiền mua xe"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Liability created successfully",
  "data": {
    "id": "liability-id",
    "counterpartyName": "Tran Thi B",
    "amount": 3000000,
    "status": "OPEN",
    "paidAmount": 0,
    "remainingAmount": 3000000,
    "isOverdue": false,
    ...
  }
}
```

### Get All Liabilities

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/liabilities?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Liability By ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/liabilities/{liability-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Update Liability

```bash
curl -X PUT http://localhost:8080/api/v1/wallet/liabilities/{liability-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 3500000,
    "note": "Updated note"
  }'
```

### Delete Liability

```bash
curl -X DELETE http://localhost:8080/api/v1/wallet/liabilities/{liability-id} \
  -H "Authorization: Bearer $TOKEN"
```

---

## 💵 Settlements (Thanh toán) APIs

### Create Settlement for Receivable

```bash
curl -X POST http://localhost:8080/api/v1/wallet/settlements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "type": "RECEIVABLE",
    "receivableId": "{receivable-id}",
    "amount": 2000000,
    "currency": "VND",
    "occurredAt": "2025-01-20T14:00:00",
    "note": "Thanh toán một phần"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Settlement created successfully",
  "data": {
    "id": "settlement-id",
    "type": "RECEIVABLE",
    "receivableId": "{receivable-id}",
    "amount": 2000000,
    ...
  }
}
```

**Note:** Sau khi tạo settlement, receivable status sẽ tự động update:
- Nếu paidAmount < amount → `PARTIALLY_PAID`
- Nếu paidAmount == amount → `PAID`

### Create Settlement for Liability

```bash
curl -X POST http://localhost:8080/api/v1/wallet/settlements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "type": "LIABILITY",
    "liabilityId": "{liability-id}",
    "amount": 1000000,
    "currency": "VND",
    "occurredAt": "2025-01-25T10:00:00",
    "note": "Trả nợ một phần"
  }'
```

### Get All Settlements

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/settlements?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Settlements by Receivable ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/settlements/receivable/{receivable-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Get Settlements by Liability ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/settlements/liability/{liability-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Get Settlement By ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/settlements/{settlement-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Update Settlement

```bash
curl -X PUT http://localhost:8080/api/v1/wallet/settlements/{settlement-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 2500000,
    "note": "Updated settlement amount"
  }'
```

**Note:** Nếu update amount, hệ thống sẽ validate lại tổng settlements <= amount gốc.

### Delete Settlement

```bash
curl -X DELETE http://localhost:8080/api/v1/wallet/settlements/{settlement-id} \
  -H "Authorization: Bearer $TOKEN"
```

**Note:** Sau khi delete settlement, paidAmount của receivable/liability sẽ tự động giảm.

---

## 🏠 Assets (Sở hữu) APIs

### Create Asset

```bash
curl -X POST http://localhost:8080/api/v1/wallet/assets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Laptop MacBook Pro",
    "type": "DEVICE",
    "estimatedValue": 35000000,
    "currency": "VND",
    "acquiredAt": "2024-06-01T00:00:00",
    "note": "MacBook Pro M2 14 inch"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Asset created successfully",
  "data": {
    "id": "asset-id",
    "name": "Laptop MacBook Pro",
    "type": "DEVICE",
    "estimatedValue": 35000000,
    "currency": "VND",
    ...
  }
}
```

### Get All Assets

```bash
curl -X GET "http://localhost:8080/api/v1/wallet/assets?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Asset By ID

```bash
curl -X GET http://localhost:8080/api/v1/wallet/assets/{asset-id} \
  -H "Authorization: Bearer $TOKEN"
```

### Get Total Asset Value

```bash
curl -X GET http://localhost:8080/api/v1/wallet/assets/total-value \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Total asset value retrieved successfully",
  "data": 50000000
}
```

### Update Asset

```bash
curl -X PUT http://localhost:8080/api/v1/wallet/assets/{asset-id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Laptop MacBook Pro (Updated)",
    "estimatedValue": 40000000,
    "note": "Updated value"
  }'
```

### Delete Asset

```bash
curl -X DELETE http://localhost:8080/api/v1/wallet/assets/{asset-id} \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🧪 Test Scenarios

### Scenario 1: Complete Receivable Flow

1. Create Receivable (amount: 5M)
2. Create Settlement 1 (amount: 2M) → Status: PARTIALLY_PAID
3. Get Receivable → Verify paidAmount = 2M, remainingAmount = 3M
4. Create Settlement 2 (amount: 3M) → Status: PAID
5. Get Receivable → Verify paidAmount = 5M, remainingAmount = 0, status = PAID

### Scenario 2: Overdue Detection

1. Create Receivable với dueAt trong quá khứ
2. Get Receivable → Verify isOverdue = true, status = OVERDUE

### Scenario 3: Settlement Validation

1. Create Receivable (amount: 5M)
2. Try to create Settlement (amount: 6M) → Should fail with 409 Conflict
3. Create Settlement (amount: 5M) → Should succeed
4. Try to create another Settlement → Should fail (total > amount)

### Scenario 4: Liability Flow

1. Create Liability (amount: 3M)
2. Create Settlement (amount: 1M) → Status: PARTIALLY_PAID
3. Update Settlement (amount: 2M) → Verify paidAmount updated
4. Delete Settlement → Verify paidAmount reset to 0

### Scenario 5: Assets Total Value

1. Create Asset 1 (value: 10M)
2. Create Asset 2 (value: 20M)
3. Create Asset 3 (value: 15M, no value)
4. Get Total Value → Should return 30M (only assets with value)

---

## 📝 Test Checklist

### Receivables
- [ ] Create receivable
- [ ] Get all receivables (paginated)
- [ ] Get receivable by ID
- [ ] Update receivable
- [ ] Delete receivable
- [ ] Verify status auto-update (OPEN → PARTIALLY_PAID → PAID)
- [ ] Verify overdue detection

### Liabilities
- [ ] Create liability
- [ ] Get all liabilities (paginated)
- [ ] Get liability by ID
- [ ] Update liability
- [ ] Delete liability
- [ ] Verify status auto-update

### Settlements
- [ ] Create settlement for receivable
- [ ] Create settlement for liability
- [ ] Get all settlements
- [ ] Get settlements by receivable ID
- [ ] Get settlements by liability ID
- [ ] Get settlement by ID
- [ ] Update settlement
- [ ] Delete settlement
- [ ] Verify paidAmount auto-update
- [ ] Verify validation (total <= amount)
- [ ] Verify status auto-update after settlement

### Assets
- [ ] Create asset (all types: CASH, ITEM, DEVICE, OTHER)
- [ ] Get all assets (paginated)
- [ ] Get asset by ID
- [ ] Get total asset value
- [ ] Update asset
- [ ] Delete asset
- [ ] Verify total value calculation (only assets with value)

---

## 🐛 Common Issues

### Issue 1: 409 Conflict when creating settlement
**Cause:** Tổng settlements vượt quá amount gốc  
**Solution:** Kiểm tra tổng settlements hiện tại trước khi tạo mới

### Issue 2: Status không update sau settlement
**Cause:** Service chưa gọi updatePaidAmount  
**Solution:** Verify settlement được save và service được gọi

### Issue 3: Overdue không đúng
**Cause:** dueAt null hoặc status đã PAID  
**Solution:** Verify dueAt được set và status != PAID

---

**Happy Testing! 🚀**
