#Requires -Version 5.1
<#
.SYNOPSIS
    搭把手项目一键启动脚本（Windows）
.DESCRIPTION
    依次启动 MySQL、Redis、后端、前端服务
.EXAMPLE
    .\scripts\start-all.ps1
#>

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)

# ========== 配置区域 ==========
$MySQLService   = "MySQL97"
$RedisDir       = "C:\Redis"
$BackendJar     = "$ProjectRoot\backend\dabashou-api\target\dabashou-api-1.0.0-SNAPSHOT.jar"
$BackendLog     = "$env:TEMP\dabashou-backend.log"
$FrontendDir    = "$ProjectRoot\frontend"
$BackendPort    = 8080
$FrontendPort   = 3099
# ==============================

function Write-Step($msg) { Write-Host "`n>>> $msg" -ForegroundColor Cyan }
function Write-OK($msg)   { Write-Host "    [OK] $msg" -ForegroundColor Green }
function Write-Skip($msg) { Write-Host "    [跳过] $msg" -ForegroundColor Yellow }

# ----- 1. MySQL -----
Write-Step "检查 MySQL 服务 ($MySQLService)"
$svc = Get-Service -Name $MySQLService -ErrorAction SilentlyContinue
if ($svc) {
    if ($svc.Status -eq "Running") {
        Write-OK "MySQL 已在运行"
    } else {
        Write-Host "    启动 MySQL..." -NoNewline
        Start-Service $MySQLService
        Start-Sleep 2
        Write-Host " done" -ForegroundColor Green
        Write-OK "MySQL 已启动"
    }
} else {
    Write-Skip "未找到服务 $MySQLService，请确认 MySQL 已安装并启动"
}

# ----- 2. Redis -----
Write-Step "检查 Redis"
$redisProc = Get-Process redis-server -ErrorAction SilentlyContinue
if ($redisProc) {
    Write-OK "Redis 已在运行 (PID: $($redisProc.Id -join ','))"
} else {
    $redisExe = "$RedisDir\redis-server.exe"
    if (Test-Path $redisExe) {
        Start-Process $redisExe -WorkingDirectory $RedisDir -WindowStyle Minimized
        Start-Sleep 1
        Write-OK "Redis 已启动"
    } else {
        Write-Skip "未找到 $redisExe，请确认 Redis 已安装"
    }
}

# ----- 3. 后端 -----
Write-Step "检查后端服务 (port $BackendPort)"
$backendConn = Get-NetTCPConnection -LocalPort $BackendPort -ErrorAction SilentlyContinue
if ($backendConn) {
    Write-OK "后端已在运行 (port $BackendPort)"
} else {
    if (-not (Test-Path $BackendJar)) {
        Write-Host "    JAR 不存在，执行 mvn package..." -ForegroundColor Yellow
        & mvn clean package -DskipTests -f "$ProjectRoot\backend\pom.xml"
    }
    Write-Host "    启动后端 (日志: $BackendLog)..." -NoNewline
    Start-Process java -ArgumentList "-jar", "`"$BackendJar`"" `
        -RedirectStandardOutput $BackendLog `
        -RedirectStandardError "$BackendLog.err" `
        -WindowStyle Hidden
    Write-Host " 等待启动" -NoNewline
    $timeout = 60
    for ($i = 0; $i -lt $timeout; $i++) {
        Start-Sleep 1
        Write-Host "." -NoNewline
        if (Get-NetTCPConnection -LocalPort $BackendPort -ErrorAction SilentlyContinue) {
            Write-Host ""
            Write-OK "后端已启动 (port $BackendPort, ${i}s)"
            break
        }
        if ($i -eq $timeout - 1) {
            Write-Host ""
            Write-Host "    [超时] 后端 ${timeout}s 内未启动，请检查日志: $BackendLog" -ForegroundColor Red
        }
    }
}

# ----- 4. 前端 -----
Write-Step "检查前端服务 (port $FrontendPort)"
$frontendConn = Get-NetTCPConnection -LocalPort $FrontendPort -ErrorAction SilentlyContinue
if ($frontendConn) {
    Write-OK "前端已在运行 (port $FrontendPort)"
} else {
    if (-not (Test-Path "$FrontendDir\node_modules")) {
        Write-Host "    node_modules 不存在，执行 npm install..." -ForegroundColor Yellow
        Push-Location $FrontendDir
        npm install
        Pop-Location
    }
    Write-Host "    启动前端..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$FrontendDir'; npm run dev" -WindowStyle Normal
    Start-Sleep 5
    if (Get-NetTCPConnection -LocalPort $FrontendPort -ErrorAction SilentlyContinue) {
        Write-OK "前端已启动 (port $FrontendPort)"
    } else {
        Write-Host "    [等待] 前端可能仍在编译中，请查看弹出的终端窗口" -ForegroundColor Yellow
    }
}

# ----- 汇总 -----
Write-Host "`n" -NoNewline
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  搭把手服务启动完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  后端 API:  http://localhost:$BackendPort" -ForegroundColor White
Write-Host "  前端页面:  http://localhost:$FrontendPort" -ForegroundColor White
Write-Host "  API文档:   http://localhost:$BackendPort/doc.html" -ForegroundColor White
Write-Host "  MySQL:     localhost:3306/dabashou" -ForegroundColor White
Write-Host "  Redis:     localhost:6379" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
