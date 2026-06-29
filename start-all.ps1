# ============================================================================
# 搭把手 (DaBaShou) 一键启动/停止脚本
# 用法: .\start-all.ps1 [start|stop|restart|status]
# ============================================================================
param([string]$Action = "start")

$ErrorActionPreference = "Continue"
$ProjectRoot = $PSScriptRoot
$LogDir = "$ProjectRoot\.logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

# ======================== 配置区域（可按需修改） ==============================

function Start-Redis {
    $path = "C:\Redis\redis-server.exe"
    if (Test-Path $path) { Start-Process -FilePath $path -WindowStyle Hidden; return $true }
    Write-Warning "Redis not found: $path"
    return $false
}

function Start-Backend {
    $jar = Get-ChildItem "$ProjectRoot\backend\dabashou-api\target\dabashou-api-*.jar" -EA 0 |
           Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $jar) { Write-Error "JAR not found, run mvn clean package first"; return $false }
    $proc = Start-Process -FilePath "java" `
        -ArgumentList "-jar", "$($jar.FullName)", "--spring.profiles.active=dev", "--server.port=9090" `
        -NoNewWindow -PassThru `
        -RedirectStandardOutput "$LogDir\backend.log" `
        -RedirectStandardError "$LogDir\backend-error.log"
    $proc.Id | Out-File "$LogDir\backend.pid"
    Write-Host "      Backend PID: $($proc.Id)"
    return $true
}

function Start-Frontend {
    $proc = Start-Process -FilePath "npx" `
        -ArgumentList "vite", "--port", "5173", "--host" `
        -WorkingDirectory "$ProjectRoot\frontend" `
        -NoNewWindow -PassThru `
        -RedirectStandardOutput "$LogDir\frontend.log" `
        -RedirectStandardError "$LogDir\frontend-error.log"
    $proc.Id | Out-File "$LogDir\frontend.pid"
    Write-Host "      Frontend PID: $($proc.Id)"
    return $true
}

function Stop-ByPidFile($pf) {
    if (Test-Path $pf) { $id = Get-Content $pf; Stop-Process -Id $id -Force -EA 0; Remove-Item $pf -EA 0 }
}

# 服务定义: Name Port Deps StartBlock StopBlock HealthType LogFile Enabled
$G = @(
    @{ N="MySQL";    P=3306; D=@();           S={};                           T={};                                          H="tcp:3306";             L="$LogDir\mysql.log";    E=$true },
    @{ N="Redis";    P=6379; D=@("MySQL");    S={ Start-Redis };             T={ Get-Process redis-server -EA 0 | Stop-Process -Force }; H="redis:6379";           L="$LogDir\redis.log";    E=$true },
    @{ N="Backend";  P=9090; D=@("MySQL","Redis"); S={ Start-Backend };      T={ Stop-ByPidFile "$LogDir\backend.pid";  Get-Process java -EA 0 | Where-Object { $_.Id -ne $PID } | Stop-Process -Force }; H="http://localhost:9090/doc.html"; L="$LogDir\backend.log";  E=$true },
    @{ N="Frontend"; P=5173; D=@("Backend");       S={ Start-Frontend };     T={ Stop-ByPidFile "$LogDir\frontend.pid"; Get-Process node -EA 0 | Where-Object { $_.CommandLine -like "*vite*" } | Stop-Process -Force }; H="http://localhost:5173";     L="$LogDir\frontend.log"; E=$true }
)

# ======================== 工具函数 ==============================

function Check-Tcp($port) {
    try { $c = New-Object System.Net.Sockets.TcpClient; $c.Connect("localhost", $port); $c.Close(); return $true }
    catch { return $false }
}

function Check-Http($url) {
    try { $r = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing -EA Stop; return ($r.StatusCode -eq 200) }
    catch { return $false }
}

function Check-Redis {
    try { return ((& redis-cli -h localhost -p 6379 ping 2>$null) -eq "PONG") }
    catch { return $false }
}

function HealthCheck($svc) {
    switch ($svc.H) {
        { $_ -like "tcp:*" }   { $p = [int]($_ -replace "tcp:",""); return Check-Tcp $p }
        { $_ -like "http:*" }  { return Check-Http $_ }
        { $_ -like "redis:*" } { return Check-Redis }
        default { return $false }
    }
}

function Banner {
    Write-Host ""
    Write-Host "  =========================================" -ForegroundColor Cyan
    Write-Host "    DaBaShou Service Manager" -ForegroundColor Cyan
    Write-Host "  =========================================" -ForegroundColor Cyan
    Write-Host ""
}

# ======================== 核心 ==============================

function Invoke-Start {
    Banner
    Write-Host "  Starting all services..." -ForegroundColor Cyan
    Write-Host ""
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
        try {
            $result = & $svc.S
            if ($result -eq $false) { throw "start command failed" }
        } catch {
            Write-Host "FAIL: $_" -ForegroundColor Red
            $st[$n] = "failed"
            continue
        }
        $healthy = $false
        for ($i = 0; $i -lt 30; $i++) {
            if (HealthCheck $svc) { $healthy = $true; break }
            Start-Sleep 1
        }
        if ($healthy) {
            Write-Host "OK  (port $($svc.P))" -ForegroundColor Green
            $st[$n] = "running"
        } else {
            Write-Host "WARN (health check timeout)" -ForegroundColor DarkYellow
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
        Write-Host "  $icon $($svc.N.PadRight(12)) : $s  (port $($svc.P))"
        if ($s -in @("failed","unknown")) { $alerts += $svc }
    }
    if ($alerts.Count -gt 0) {
        Write-Host ""
        Write-Host "  [!] Abnormal services:" -ForegroundColor Red
        $alerts | ForEach-Object { Write-Host "      - $($_.N) -> log: $($_.L)" -ForegroundColor Red }
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
    Write-Host ""
    Write-Host "  All stopped." -ForegroundColor Green
    Write-Host ""
}

function Invoke-Status {
    Banner
    foreach ($svc in $G) {
        if (-not $svc.E) { continue }
        Write-Host "  $($svc.N.PadRight(12)) " -NoNewline
        if (Check-Tcp $svc.P) { Write-Host "RUNNING (port $($svc.P))" -ForegroundColor Green }
        else { Write-Host "DOWN" -ForegroundColor Red }
    }
    Write-Host ""
}

# ======================== 入口 ==============================

switch ($Action) {
    "start"   { Invoke-Start }
    "stop"    { Invoke-Stop }
    "restart" { Invoke-Stop; Start-Sleep 2; Invoke-Start }
    "status"  { Invoke-Status }
    default   { Write-Host "Usage: .\start-all.ps1 [start|stop|restart|status]" -ForegroundColor Yellow }
}
