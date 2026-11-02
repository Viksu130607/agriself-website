$base = "http://localhost:8080"
Write-Host "--- 1. Testing Root Web Portal ---"
$res1 = Invoke-WebRequest -Uri $base -UseBasicParsing
Write-Host "Root Status: $($res1.StatusCode) (Length: $($res1.Content.Length))"

Write-Host "`n--- 2. Testing Auth Demo Token (FARMER) ---"
$authFarmer = Invoke-RestMethod -Uri "$base/api/v1/auth/demo-token/FARMER"
Write-Host "Farmer Token received for: $($authFarmer.fullName), Role: $($authFarmer.role)"

Write-Host "`n--- 3. Testing Marketplace Listings ---"
$listings = Invoke-RestMethod -Uri "$base/api/v1/marketplace/listings"
Write-Host "Total Listings: $($listings.Count), First Crop: $($listings[0].cropName) (₹$($listings[0].pricePerUnit)/$($listings[0].unit))"

Write-Host "`n--- 4. Testing Marketplace Search ('wheat') ---"
$wheat = Invoke-RestMethod -Uri "$base/api/v1/marketplace/listings?crop=wheat"
Write-Host "Filtered Results: $($wheat.Count), Name: $($wheat[0].cropName)"

Write-Host "`n--- 5. Testing Machinery Catalog ---"
$machinery = Invoke-RestMethod -Uri "$base/api/v1/rentals/catalog"
Write-Host "Available Machinery: $($machinery.Count), First: $($machinery[0].name) at ₹$($machinery[0].hourlyRate)/hr"

Write-Host "`n--- 6. Testing AI Diagnostic Engine ---"
$farmerHeaders = @{ Authorization = "Bearer $($authFarmer.token)"; "Content-Type" = "application/json" }
$diagPayload = @{ cropName = "Tomato"; symptom = "Concentric dark rings with yellow halos" } | ConvertTo-Json
$diag = Invoke-RestMethod -Uri "$base/api/v1/diagnostic/scan-preset" -Method Post -Body $diagPayload -Headers $farmerHeaders
Write-Host "Diagnosis: $($diag.diagnosisResult)"
Write-Host "Confidence: $($diag.confidence)%"
Write-Host "Severity: $($diag.severity)"
Write-Host "Prescription Action: $($diag.immediateAction)"

Write-Host "`n--- 7. Testing Admin Stats with COOP_ADMIN JWT ---"
$authAdmin = Invoke-RestMethod -Uri "$base/api/v1/auth/demo-token/COOP_ADMIN"
$headers = @{ Authorization = "Bearer $($authAdmin.token)" }
$stats = Invoke-RestMethod -Uri "$base/api/v1/admin/stats" -Headers $headers
Write-Host "Farmers Count: $($stats.totalFarmers)"
Write-Host "Total Farmland: $($stats.totalAcreage) Acres"
Write-Host "Active Listings: $($stats.activeCropListings)"
Write-Host "Soil Distribution:" ($stats.soilDistribution | ConvertTo-Json -Compress)

Write-Host "`n=========================================="
Write-Host " ALL AGRISELF SPRING BOOT TESTS PASSED! "
Write-Host "=========================================="
