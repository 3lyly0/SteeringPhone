param (
    [Parameter(Mandatory=$true)]
    [string]$Version
)

# Format version tag with leading 'v'
if (-not $Version.StartsWith("v")) {
    $Version = "v$Version"
}

Write-Host "Creating SteeringPhone Release Tag $Version..." -ForegroundColor Cyan

# Ensure git working tree is clean
$status = git status --porcelain
if ($status) {
    Write-Host "Error: Git working tree is not clean. Commit or stash changes first." -ForegroundColor Red
    exit 1
}

# Create and push tag to trigger GitHub Release Action
git tag -a $Version -m "Release $Version"
git push origin $Version

Write-Host "Successfully pushed tag $Version to GitHub!" -ForegroundColor Green
Write-Host "GitHub Action is now building binaries & publishing release to:" -ForegroundColor Yellow
Write-Host "https://github.com/3lyly0/SteeringPhone/releases/tag/$Version" -ForegroundColor Yellow
