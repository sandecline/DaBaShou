# ============================================================================
# DaBaShou Service Manager
# Usage: .\start-all.ps1 [start|stop|restart|status]
# ============================================================================
param(
    [string]$Action = "start",
    [switch]$NoPause
)

$ErrorActionPreference = "Continue"
$ProjectRoot = $PSScriptRoot
$LogDir = "$ProjectRoot\.logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

# ======================== Config ==============================

$RedisCandidates = @(
    "C:\Redis\redis-server.exe",
    "C:\Program Files\Redis\redis-server.exe",
    "C:\Program Files\Redis-x64\redis-server.exe"
)
$RedisExe = $null
$RedisCli = $null

function Resolve-Redis {
    $redisFromPath = Get-Command redis-server.exe -EA 0
    if ($redisFromPath) {
        $script:RedisExe = $redisFromPath.Source
    } else {
        $script:RedisExe = $RedisCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
    }

    if ($script:RedisExe) {
        $script:RedisCli = Join-Path (Split-Path -Parent $script:RedisExe) "redis-cli.exe"
    }
}

function Start-Redis {
    Resolve-Redis
    if (-not $RedisExe -or -not (Test-Path $RedisExe)) {
        Write-Warning "Redis not found. Please install Redis, add redis-server.exe to PATH, or start Redis on port 6379 manually."
        Write-Warning "Checked: $($RedisCandidates -join ', ')"
        return $false
    }
    if (Check-Tcp 6379) {
        Write-Host "(already running)" -NoNewline -ForegroundColor DarkGray
        return $true
    }
    Start-Process -FilePath $RedisExe `
        -WindowStyle Hidden `
        -RedirectStandardOutput "$LogDir\redis.log" `
        -RedirectStandardError "$LogDir\redis-error.log"
    return $true
}

function Start-Backend {
    $jar = Get-ChildItem "$ProjectRoot\backend\dabashou-api\target\dabashou-api-*.jar" -EA 0 |
           Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $jar) {
        Write-Host "(JAR not found)" -NoNewline -ForegroundColor Red
        return $false
    }
    $proc = Start-Process -FilePath "java" `
        -ArgumentList "-jar", "$($jar.FullName)", "--spring.profiles.active=dev", "--server.port=9090" `
        -NoNewWindow -PassThru `
        -RedirectStandardOutput "$LogDir\backend.log" `
        -RedirectStandardError "$LogDir\backend-error.log"
    $proc.Id | Out-File "$LogDir\backend.pid"
    Write-Host "(PID: $($proc.Id))" -NoNewline -ForegroundColor DarkGray
    return $true
}

function Start-Frontend {
    if (-not (Test-Path "$ProjectRoot\frontend\node_modules")) {
        Write-Host "(installing deps...)" -NoNewline -ForegroundColor DarkGray
        Push-Location "$ProjectRoot\frontend"
        & npm.cmd install 2>&1 | Out-Null
        Pop-Location
    }
    $proc = Start-Process -FilePath "npm.cmd" `
        -ArgumentList "run", "dev", "--", "--port", "5173", "--host" `
        -WorkingDirectory "$ProjectRoot\frontend" `
        -NoNewWindow -PassThru `
        -RedirectStandardOutput "$LogDir\frontend.log" `
        -RedirectStandardError "$LogDir\frontend-error.log"
    $proc.Id | Out-File "$LogDir\frontend.pid"
    Write-Host "(PID: $($proc.Id))" -NoNewline -ForegroundColor DarkGray
    return $true
}

function Stop-ByPidFile($pf) {
    if (Test-Path $pf) {
        $id = Get-Content $pf
        if ($id) { Stop-Process -Id $id -Force -EA 0 }
        Remove-Item $pf -EA 0
    }
}

# Service definitions
$G = @(
    @{ N="MySQL"; P=3306; D=@(); S={}; T={}; H="tcp:3306"; L="$LogDir\mysql.log"; E=$true },
    @{ N="Redis"; P=6379; D=@("MySQL"); S={ Start-Redis }; T={ Get-Process redis-server -EA 0 | Stop-Process -Force }; H="tcp:6379"; L="$LogDir\redis.log"; E=$true },
    @{ N="Backend"; P=9090; D=@("MySQL","Redis"); S={ Start-Backend }; T={ Stop-ByPidFile "$LogDir\backend.pid"; Get-Process java -EA 0 | Where-Object { $_.Id -ne $PID } | Stop-Process -Force }; H="http:localhost:9090/doc.html"; L="$LogDir\backend.log"; E=$true },
    @{ N="Frontend"; P=5173; D=@("Backend"); S={ Start-Frontend }; T={ Stop-ByPidFile "$LogDir\frontend.pid"; Get-Process node -EA 0 | Where-Object { $_.CommandLine -like "*vite*" } | Stop-Process -Force }; H="http:localhost:5173"; L="$LogDir\frontend.log"; E=$true }
)

# ======================== Utils ==============================

function Check-Tcp($port, $timeout=3) {
    try {
        $c = New-Object System.Net.Sockets.TcpClient
        $result = $c.BeginConnect("localhost", $port, $null, $null)
        $success = $result.AsyncWaitHandle.WaitOne([TimeSpan]::FromSeconds($timeout))
        if ($success) { $c.EndConnect($result); $c.Close(); return $true }
        $c.Close()
        return $false
    } catch { return $false }
}

function Check-Http($url, $timeout=10) {
    try {
        $r = Invoke-WebRequest -Uri $url -TimeoutSec $timeout -UseBasicParsing -EA Stop
        return ($r.StatusCode -eq 200)
    } catch { return $false }
}

function HealthCheck($svc) {
    $h = $svc.H
    if ($h -like "tcp:*") {
        $p = [int]($h -replace "tcp:","")
        return Check-Tcp $p
    }
    elseif ($h -like "http:*") {
        $url = $h -replace "^http:","http://"
        return Check-Http $url
    }
    return $false
}

function Banner {
    Write-Host ""
    Write-Host "  =========================================" -ForegroundColor Cyan
    Write-Host "    DaBaShou Service Manager" -ForegroundColor Cyan
    Write-Host "  =========================================" -ForegroundColor Cyan
    Write-Host ""
}

# ======================== Core ==============================

function Invoke-Start {
    Banner
    Write-Host "  Starting all services..." -ForegroundColor Cyan
    Write-Host ""
    Remove-Item "$LogDir\*.pid" -EA 0
    $st = @{}
    foreach ($svc in $G) {
        if (-not $svc.E) { continue }
        $n = $svc.N
        $blocked = $false
        foreach ($d in $svc.D) {
            if ($st[$d] -ne "running") { $blocked = $true; break }
        }
        if ($blocked) {
            Write-Host "  [SKIP] $n - dependency not ready" -ForegroundColor DarkGray
            $st[$n] = "blocked"
            continue
        }
        Write-Host "  [START] $n ... " -NoNewline -ForegroundColor Yellow
        if (HealthCheck $svc) {
            Write-Host "OK (already running)" -ForegroundColor Green
            $st[$n] = "running"
            continue
        }
        try {
            $result = & $svc.S
            if ($result -eq $false) { throw "start command failed" }
        } catch {
            Write-Host "FAIL: $_" -ForegroundColor Red
            $st[$n] = "failed"
            continue
        }
        $healthy = $false
        $maxWait = if ($n -eq "Backend") { 60 } else { 30 }
        for ($i = 0; $i -lt $maxWait; $i++) {
            Start-Sleep 1
            if (HealthCheck $svc) { $healthy = $true; break }
            if (($i + 1) % 5 -eq 0) { Write-Host "." -NoNewline -ForegroundColor DarkGray }
        }
        if ($healthy) {
            Write-Host " OK (port $($svc.P))" -ForegroundColor Green
            $st[$n] = "running"
        } else {
            Write-Host " WARN (health check timeout)" -ForegroundColor DarkYellow
            $st[$n] = "unknown"
        }
    }
    Write-Host ""
    Write-Host "  ======== Status ========" -ForegroundColor Cyan
    $alerts = @()
    foreach ($svc in $G) {
        if (-not $svc.E) { continue }
        $s = $st[$svc.N]
        $icon = @{running="[OK]"; unknown="[??]"; failed="[FAIL]"; blocked="[--]"}[$s]
        $color = @{running="Green"; unknown="DarkYellow"; failed="Red"; blocked="DarkGray"}[$s]
        Write-Host "  $icon $($svc.N.PadRight(12)) : $s  (port $($svc.P))" -ForegroundColor $color
        if ($s -in @("failed","unknown")) { $alerts += $svc }
    }
    if ($alerts.Count -gt 0) {
        Write-Host ""
        Write-Host "  [!] Troubleshooting:" -ForegroundColor Red
        foreach ($a in $alerts) {
            Write-Host "      - $($a.N):" -ForegroundColor Red
            Write-Host "        Log: $($a.L)" -ForegroundColor DarkGray
        }
        if (-not $NoPause) {
            Write-Host ""
            Read-Host "  Press Enter to exit" | Out-Null
        }
    }
    Write-Host ""
}

function Invoke-Stop {
    Banner
    Write-Host "  Stopping all services..." -ForegroundColor Cyan
    Write-Host ""
    for ($i = $G.Count - 1; $i -ge 0; $i--) {
        $svc = $G[$i]
        if (-not $svc.E) { continue }
        Write-Host "  [STOP] $($svc.N) ... " -NoNewline -ForegroundColor Yellow
        try { & $svc.T; Write-Host "OK" -ForegroundColor Green }
        catch { Write-Host "WARN: $_" -ForegroundColor DarkYellow }
    }
    Remove-Item "$LogDir\*.pid" -EA 0
    Write-Host ""
    Write-Host "  All stopped." -ForegroundColor Green
    Write-Host ""
}

function Invoke-Status {
    Banner
    foreach ($svc in $G) {
        if (-not $svc.E) { continue }
        Write-Host "  $($svc.N.PadRight(12)) " -NoNewline
        if (HealthCheck $svc) { Write-Host "RUNNING (port $($svc.P))" -ForegroundColor Green }
        else { Write-Host "DOWN" -ForegroundColor Red }
    }
    Write-Host ""
}

# ======================== Entry ==============================

switch ($Action) {
    "start"   { Invoke-Start }
    "stop"    { Invoke-Stop }
    "restart" { Invoke-Stop; Start-Sleep 2; Invoke-Start }
    "status"  { Invoke-Status }
    default   { Write-Host "Usage: .\start-all.ps1 [start|stop|restart|status]" -ForegroundColor Yellow }
}
