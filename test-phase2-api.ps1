# Phase 2 APIs - Quick Test Script
# PowerShell script để test nhanh Phase 1 & 2 APIs (Core Wallet + Reports & Budgets)

# Set base URL
$BASE_URL = "http://localhost:8080"
$API_PREFIX = "/api/v1"

# Initialize variables
$TOKEN = $null
$USER_ID = $null
$ACCOUNT_ID = $null
$CATEGORY_ID = $null
$TRANSACTION_ID = $null
$BUDGET_ID = $null

Write-Host "=== Wallet API Test Script ===" -ForegroundColor Cyan
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

# Step 2: Test Accounts
Write-Host "Step 2: Testing Accounts..." -ForegroundColor Yellow
try {
    # Create Account
    $accountBody = @{
        name = "Vi tien mat"
        type = "CASH"
        currency = "VND"
        openingBalance = 1000000
    } | ConvertTo-Json

    $account = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/accounts" `
        -Method POST `
        -Headers @{ Authorization = "Bearer $TOKEN" } `
        -ContentType "application/json" `
        -Body $accountBody

    if ($account -and $account.data -and $account.data.id) {
        $ACCOUNT_ID = $account.data.id
        Write-Host "Account created: $ACCOUNT_ID" -ForegroundColor Green
    } else {
        Write-Host "Account created but ID not found in response" -ForegroundColor Yellow
    }

    # Get All Accounts
    $accounts = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/accounts" `
        -Method GET `
        -Headers @{ Authorization = "Bearer $TOKEN" }
    
    if ($accounts -and $accounts.data -and $accounts.data.content) {
        Write-Host "Found $($accounts.data.content.Count) accounts" -ForegroundColor Green
        # If ACCOUNT_ID not set, try to get from list
        if (-not $ACCOUNT_ID -and $accounts.data.content.Count -gt 0) {
            $ACCOUNT_ID = $accounts.data.content[0].id
            Write-Host "Using first account: $ACCOUNT_ID" -ForegroundColor Gray
        }
    } else {
        Write-Host "Could not retrieve accounts list" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "Account test failed: $($_.Exception.Message)" -ForegroundColor Red
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

# Step 3: Test Categories
Write-Host "Step 3: Testing Categories..." -ForegroundColor Yellow
try {
    # Get All Categories
    $categories = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/categories" `
        -Method GET `
        -Headers @{ Authorization = "Bearer $TOKEN" }

    if ($categories -and $categories.data -and $categories.data.Count -gt 0) {
        $CATEGORY_ID = $categories.data[0].id
        Write-Host "Found $($categories.data.Count) categories" -ForegroundColor Green
        Write-Host "Using category: $CATEGORY_ID" -ForegroundColor Gray
    } else {
        Write-Host "No categories found" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "Category test failed: $($_.Exception.Message)" -ForegroundColor Red
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

# Step 4: Test Transactions
Write-Host "Step 4: Testing Transactions..." -ForegroundColor Yellow
try {
    # Create Expense Transaction
    if ($ACCOUNT_ID -and $CATEGORY_ID) {
        $transactionBody = @{
            type = "EXPENSE"
            amount = 50000
            currency = "VND"
            categoryId = $CATEGORY_ID
            accountId = $ACCOUNT_ID
            note = "Test transaction"
        } | ConvertTo-Json

        $transaction = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/transactions" `
            -Method POST `
            -Headers @{ Authorization = "Bearer $TOKEN" } `
            -ContentType "application/json" `
            -Body $transactionBody

        if ($transaction -and $transaction.data -and $transaction.data.id) {
            $TRANSACTION_ID = $transaction.data.id
            Write-Host "Transaction created: $TRANSACTION_ID" -ForegroundColor Green
        }

        # Get All Transactions
        $transactions = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/transactions" `
            -Method GET `
            -Headers @{ Authorization = "Bearer $TOKEN" }
        
        if ($transactions -and $transactions.data -and $transactions.data.content) {
            Write-Host "Found $($transactions.data.content.Count) transactions" -ForegroundColor Green
        }
        Write-Host ""
    } else {
        Write-Host "Skipping transaction test (missing account or category ID)" -ForegroundColor Yellow
        Write-Host "  ACCOUNT_ID: $ACCOUNT_ID" -ForegroundColor Gray
        Write-Host "  CATEGORY_ID: $CATEGORY_ID" -ForegroundColor Gray
        Write-Host ""
    }
} catch {
    Write-Host "Transaction test failed: $($_.Exception.Message)" -ForegroundColor Red
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

# Step 5: Test Dashboard
Write-Host "Step 5: Testing Dashboard..." -ForegroundColor Yellow
try {
    # Get Dashboard Report
    $dashboard = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/reports/dashboard?period=month" `
        -Method GET `
        -Headers @{ Authorization = "Bearer $TOKEN" }
    
    if ($dashboard -and $dashboard.data) {
        Write-Host "Dashboard report retrieved" -ForegroundColor Green
        Write-Host "  Total Income: $($dashboard.data.totalIncome)" -ForegroundColor Gray
        Write-Host "  Total Expense: $($dashboard.data.totalExpense)" -ForegroundColor Gray
        Write-Host "  Net Savings: $($dashboard.data.netSavings)" -ForegroundColor Gray
    } else {
        Write-Host "Dashboard data not found" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "Dashboard test failed: $($_.Exception.Message)" -ForegroundColor Red
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

# Step 6: Test Budgets
Write-Host "Step 6: Testing Budgets..." -ForegroundColor Yellow
try {
    # Try to create Budget
    $budgetBody = @{
        month = "2025-01"
        amount = 5000000
    } | ConvertTo-Json

    try {
        $budget = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/budgets" `
            -Method POST `
            -Headers @{ Authorization = "Bearer $TOKEN" } `
            -ContentType "application/json" `
            -Body $budgetBody

        if ($budget -and $budget.data -and $budget.data.id) {
            $BUDGET_ID = $budget.data.id
            Write-Host "Budget created: $BUDGET_ID" -ForegroundColor Green
        }
    } catch {
        # If budget already exists (409), try to get existing budget
        $statusCode = $null
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }
        
        if ($statusCode -eq 409) {
            Write-Host "Budget already exists for this month, getting existing budget..." -ForegroundColor Yellow
            try {
                # Get budgets for the month
                $existingBudgets = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/budgets/month?month=2025-01" `
                    -Method GET `
                    -Headers @{ Authorization = "Bearer $TOKEN" }
                
                if ($existingBudgets -and $existingBudgets.data -and $existingBudgets.data.Count -gt 0) {
                    $BUDGET_ID = $existingBudgets.data[0].id
                    Write-Host "Found existing budget: $BUDGET_ID" -ForegroundColor Green
                }
            } catch {
                Write-Host "Could not retrieve existing budgets: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        } else {
            Write-Host "Budget creation failed: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }

    # Get Budget details
    if ($BUDGET_ID) {
        try {
            $budgetDetail = Invoke-RestMethod -Uri "$BASE_URL$API_PREFIX/wallet/budgets/$BUDGET_ID" `
                -Method GET `
                -Headers @{ Authorization = "Bearer $TOKEN" }
            
            if ($budgetDetail -and $budgetDetail.data) {
                Write-Host "Budget retrieved" -ForegroundColor Green
                Write-Host "  Amount: $($budgetDetail.data.amount)" -ForegroundColor Gray
                Write-Host "  Used: $($budgetDetail.data.usedAmount)" -ForegroundColor Gray
                Write-Host "  Remaining: $($budgetDetail.data.remainingAmount)" -ForegroundColor Gray
            } else {
                Write-Host "Budget data not found in response" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "Could not retrieve budget details: $($_.Exception.Message)" -ForegroundColor Yellow
        }
        Write-Host ""
    } else {
        Write-Host "Could not create or find budget" -ForegroundColor Yellow
        Write-Host ""
    }
} catch {
    Write-Host "Budget test failed: $($_.Exception.Message)" -ForegroundColor Red
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
if ($ACCOUNT_ID) { Write-Host "  ACCOUNT_ID: $ACCOUNT_ID" -ForegroundColor Gray }
if ($CATEGORY_ID) { Write-Host "  CATEGORY_ID: $CATEGORY_ID" -ForegroundColor Gray }
if ($TRANSACTION_ID) { Write-Host "  TRANSACTION_ID: $TRANSACTION_ID" -ForegroundColor Gray }
if ($BUDGET_ID) { Write-Host "  BUDGET_ID: $BUDGET_ID" -ForegroundColor Gray }
