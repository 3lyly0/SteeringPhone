# SteeringPhone ViGEmBus Kernel Driver Verification & Setup Script
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " SteeringPhone ViGEmBus Driver Installer" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Check if administrator
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Warning "Administrator rights required to install ViGEmBus driver. Please run script as Administrator."
}

# Check service existence
$service = Get-Service -Name "ViGEmBus" -ErrorAction SilentlyContinue
if ($service) {
    Write-Host "ViGEmBus driver is already installed and registered (Status: $($service.Status))." -ForegroundColor Green
    exit 0
}

Write-Host "ViGEmBus driver not found. Downloading latest release..." -ForegroundColor Yellow
$downloadUrl = "https://github.com/ViGEm/ViGEmBus/releases/download/v1.22.0/ViGEmBus_1.22.0_x64_x86_arm64.exe"
$outputFile = Join-Path $env:TEMP "ViGEmBus_Setup.exe"

try {
    Invoke-WebRequest -Uri $downloadUrl -OutFile $outputFile -UseBasicParsing
    Write-Host "Download complete. Starting installation..." -ForegroundColor Yellow
    Start-Process -FilePath $outputFile -ArgumentList "/quiet" -Wait
    
    $checkService = Get-Service -Name "ViGEmBus" -ErrorAction SilentlyContinue
    if ($checkService) {
        Write-Host "ViGEmBus successfully installed!" -ForegroundColor Green
    } else {
        Write-Host "Driver installer executed. Please reboot if virtual controller does not initialize." -ForegroundColor Yellow
    }
} catch {
    Write-Error "Failed to download ViGEmBus installer: $_"
    exit 1
} finally {
    if (Test-Path $outputFile) { Remove-Item $outputFile -Force }
}
