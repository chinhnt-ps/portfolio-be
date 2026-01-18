# Phase 3 APIs - Quick Test Script
# PowerShell script de test nhanh cac Phase 3 APIs

# Set base URL
$BASE_URL = "http://localhost:8080"
$API_PREFIX = "/api/v1"

# Initialize variables
$TOKEN = $null
$USER_ID = $null
$RECEIVABLE_ID = $null
$LIABILITY_ID = $null
$SETTLEMENT_ID = $null
$ASSET_ID = $null

Write-Host "=== Phase 3 APIs Test Script ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Login va lay token
Write-Host "Step 1: Login..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body (@{
            email = "chinhthe.ccpr@gmail.com"
            password = "Password123"
        } | ConvertTo-Json)

    $TOKEN = $loginResponse.data.accessToken
    $USER_ID = $loginResponse.data.user.id

    Write-Host "Login successful!" -ForegroundColor Green
    Write-Host "Token: $TOKEN" -ForegroundColor Gray
    Write-Host "User ID: $USER_ID" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Test Receivables
Write-Host "Step 2: Testing Receivables..." -ForegroundColor Yellow
try {
    # Create Receivable
    $receivableBody = @{
        counterpartyName = "Nguyen Van A"
        amount = 5000000
        currency = "VND"
        occurredAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
        dueAt = (Get-Date).AddMonths(1).ToString("yyyy-MM-ddTHH:mm:ss")
        note = "Cho vay tien mua nha"
    } | ConvertTo-Json

    $receivable = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/receivables" `
        -Method POST `
        -Headers @{ Authorization = "Bearer $TOKEN" } `
        -ContentType "application/json" `
        -Body $receivableBody

    if ($receivable -and $receivable.data -and $receivable.data.id) {
        $RECEIVABLE_ID = $receivable.data.id
        Write-Host "Receivable created: $RECEIVABLE_ID" -ForegroundColor Green
        Write-Host "  Status: $($receivable.data.status)" -ForegroundColor Gray
        Write-Host "  Amount: $($receivable.data.amount)" -ForegroundColor Gray
        Write-Host "  Remaining: $($receivable.data.remainingAmount)" -ForegroundColor Gray
    } else {
        Write-Host "Receivable created but ID not found" -ForegroundColor Yellow
    }

    # Get All Receivables
    $receivables = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/receivables" `
        -Method GET `
        -Headers @{ Authorization = "Bearer $TOKEN" }
    
    if ($receivables -and $receivables.data -and $receivables.data.content) {
        Write-Host "Found $($receivables.data.content.Count) receivables" -ForegroundColor Green
    }
    Write-Host ""
} catch {
    Write-Host "Receivable test failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response: $responseBody" -ForegroundColor Red
        } catch {
            # Ignore if can't read response
        }
    }
}

# Step 3: Test Liabilities
Write-Host "Step 3: Testing Liabilities..." -ForegroundColor Yellow
try {
    # Create Liability
    $liabilityBody = @{
        counterpartyName = "Tran Thi B"
        amount = 3000000
        currency = "VND"
        occurredAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
        dueAt = (Get-Date).AddMonths(2).ToString("yyyy-MM-ddTHH:mm:ss")
        note = "Vay tien mua xe"
    } | ConvertTo-Json

    $liability = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/liabilities" `
        -Method POST `
        -Headers @{ Authorization = "Bearer $TOKEN" } `
        -ContentType "application/json" `
        -Body $liabilityBody

    if ($liability -and $liability.data -and $liability.data.id) {
        $LIABILITY_ID = $liability.data.id
        Write-Host "Liability created: $LIABILITY_ID" -ForegroundColor Green
        Write-Host "  Status: $($liability.data.status)" -ForegroundColor Gray
        Write-Host "  Amount: $($liability.data.amount)" -ForegroundColor Gray
        Write-Host "  Remaining: $($liability.data.remainingAmount)" -ForegroundColor Gray
    } else {
        Write-Host "Liability created but ID not found" -ForegroundColor Yellow
    }

    # Get All Liabilities
    $liabilities = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/liabilities" `
        -Method GET `
        -Headers @{ Authorization = "Bearer $TOKEN" }
    
    if ($liabilities -and $liabilities.data -and $liabilities.data.content) {
        Write-Host "Found $($liabilities.data.content.Count) liabilities" -ForegroundColor Green
    }
    Write-Host ""
} catch {
    Write-Host "Liability test failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response: $responseBody" -ForegroundColor Red
        } catch {
            # Ignore if can't read response
        }
    }
}

# Step 4: Test Settlements
Write-Host "Step 4: Testing Settlements..." -ForegroundColor Yellow
try {
    if ($RECEIVABLE_ID) {
        # Create Settlement for Receivable
        $settlementBody = @{
            type = "RECEIVABLE"
            receivableId = $RECEIVABLE_ID
            amount = 2000000
            currency = "VND"
            occurredAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
            note = "Thanh toan mot phan"
        } | ConvertTo-Json

        $settlement = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/settlements" `
            -Method POST `
            -Headers @{ Authorization = "Bearer $TOKEN" } `
            -ContentType "application/json" `
            -Body $settlementBody

        if ($settlement -and $settlement.data -and $settlement.data.id) {
            $SETTLEMENT_ID = $settlement.data.id
            Write-Host "Settlement created: $SETTLEMENT_ID" -ForegroundColor Green
            Write-Host "  Amount: $($settlement.data.amount)" -ForegroundColor Gray
        }

        # Get Receivable again to verify paidAmount updated
        $receivableUpdated = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/receivables/$RECEIVABLE_ID" `
            -Method GET `
            -Headers @{ Authorization = "Bearer $TOKEN" }
        
        if ($receivableUpdated -and $receivableUpdated.data) {
            Write-Host "Receivable updated:" -ForegroundColor Green
            Write-Host "  Paid Amount: $($receivableUpdated.data.paidAmount)" -ForegroundColor Gray
            Write-Host "  Remaining: $($receivableUpdated.data.remainingAmount)" -ForegroundColor Gray
            Write-Host "  Status: $($receivableUpdated.data.status)" -ForegroundColor Gray
        }

        # Get Settlements by Receivable ID
        $settlements = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/settlements/receivable/$RECEIVABLE_ID" `
            -Method GET `
            -Headers @{ Authorization = "Bearer $TOKEN" }
        
        if ($settlements -and $settlements.data) {
            Write-Host "Found $($settlements.data.Count) settlements for receivable" -ForegroundColor Green
        }
    } else {
        Write-Host "Skipping settlement test (missing receivable ID)" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "Settlement test failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response: $responseBody" -ForegroundColor Red
        } catch {
            # Ignore if can't read response
        }
    }
}

# Step 5: Test Assets
Write-Host "Step 5: Testing Assets..." -ForegroundColor Yellow
try {
    # Create Asset
    $assetBody = @{
        name = "Laptop MacBook Pro"
        type = "DEVICE"
        estimatedValue = 35000000
        currency = "VND"
        acquiredAt = "2024-06-01T00:00:00"
        note = "MacBook Pro M2 14 inch"
    } | ConvertTo-Json

    $asset = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/assets" `
        -Method POST `
        -Headers @{ Authorization = "Bearer $TOKEN" } `
        -ContentType "application/json" `
        -Body $assetBody

    if ($asset -and $asset.data -and $asset.data.id) {
        $ASSET_ID = $asset.data.id
        Write-Host "Asset created: $ASSET_ID" -ForegroundColor Green
        Write-Host "  Name: $($asset.data.name)" -ForegroundColor Gray
        Write-Host "  Type: $($asset.data.type)" -ForegroundColor Gray
        Write-Host "  Value: $($asset.data.estimatedValue)" -ForegroundColor Gray
    } else {
        Write-Host "Asset created but ID not found" -ForegroundColor Yellow
    }

    # Get Total Asset Value
    $totalValue = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/assets/total-value" `
        -Method GET `
        -Headers @{ Authorization = "Bearer $TOKEN" }
    
    if ($totalValue -and $totalValue.data) {
        Write-Host "Total Asset Value: $($totalValue.data)" -ForegroundColor Green
    }

    # Get All Assets
    $assets = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/assets" `
        -Method GET `
        -Headers @{ Authorization = "Bearer $TOKEN" }
    
    if ($assets -and $assets.data -and $assets.data.content) {
        Write-Host "Found $($assets.data.content.Count) assets" -ForegroundColor Green
    }
    Write-Host ""
} catch {
    Write-Host "Asset test failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response: $responseBody" -ForegroundColor Red
        } catch {
            # Ignore if can't read response
        }
    }
}

Write-Host "=== Test Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Variables saved:" -ForegroundColor Yellow
Write-Host "  TOKEN: $TOKEN" -ForegroundColor Gray
Write-Host "  USER_ID: $USER_ID" -ForegroundColor Gray
if ($RECEIVABLE_ID) { Write-Host "  RECEIVABLE_ID: $RECEIVABLE_ID" -ForegroundColor Gray }
if ($LIABILITY_ID) { Write-Host "  LIABILITY_ID: $LIABILITY_ID" -ForegroundColor Gray }
if ($SETTLEMENT_ID) { Write-Host "  SETTLEMENT_ID: $SETTLEMENT_ID" -ForegroundColor Gray }
if ($ASSET_ID) { Write-Host "  ASSET_ID: $ASSET_ID" -ForegroundColor Gray }
