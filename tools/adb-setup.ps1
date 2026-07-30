# SteeringPhone ADB Forwarding Setup Script
Param(
    [int]$WsPort = 45679,
    [int]$UdpPort = 45680
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " SteeringPhone ADB Port Forwarding Setup" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Check ADB availability
$adbPath = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adbPath) {
    Write-Error "ADB tool is not found in PATH. Please install Android Platform Tools."
    exit 1
}

Write-Host "Checking for connected Android devices..." -ForegroundColor Yellow
$devices = adb devices | Select-String -Pattern "\tdevice$"

if ($devices.Count -eq 0) {
    Write-Error "No connected Android devices found. Ensure USB debugging is enabled on your phone."
    exit 1
}

Write-Host "Device detected: $($devices[0])" -ForegroundColor Green

Write-Host "Setting up ADB port forwarding..." -ForegroundColor Yellow
# Forward WebSocket control port (TCP)
adb forward tcp:$WsPort tcp:$WsPort
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to forward TCP port $WsPort"
    exit 1
}

Write-Host "ADB forward tcp:$WsPort tcp:$WsPort established successfully." -ForegroundColor Green
Write-Host "Setup complete. SteeringPhone Desktop can now communicate over USB." -ForegroundColor Cyan
