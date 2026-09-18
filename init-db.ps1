# ============================================================================
# 搭把手数据库一键初始化脚本 (Windows PowerShell)
# 用法:
#   .\init-db.ps1                          # 默认 root 无密码
#   .\init-db.ps1 -Password "mypassword"   # 指定密码
#   .\init-db.ps1 -Port 3307               # 自定义端口
#   .\init-db.ps1 -Reset                   # 清空重建（危险！）
# ============================================================================

param(
    [string]$Host = "127.0.0.1",
    [int]$Port = 3306,
    [string]$Username = "root",
    [string]$Password = "",
    [string]$Database = "dabashou",
    [switch]$Reset,
    [switch]$NoPause
)

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$MigrationDir = "$ProjectRoot\database\migration"

function Banner {
    Write-Host ""
    Write-Host "  =========================================" -ForegroundColor Cyan
    Write-Host "    搭把手数据库初始化工具" -ForegroundColor Cyan
    Write-Host "  =========================================" -ForegroundColor Cyan
    Write-Host ""
}

function Test-MySQL {
    Write-Host "  [CHECK] MySQL 连接 " -NoNewline -ForegroundColor Yellow
    $mysqlArgs = @("-h", $Host, "-P", $Port, "-u", $Username)
    if ($Password) { $mysqlArgs += "-p$Password" }
    $mysqlArgs += "-e", "SELECT 1"

    try {
        $null = & mysql @mysqlArgs 2>&1
        if ($LASTEXITCODE -ne 0) { throw "连接失败" }
        Write-Host "OK" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "FAIL: $_" -ForegroundColor Red
        Write-Host "  请确保 MySQL 已启动，且 mysql 客户端在 PATH 中" -ForegroundColor DarkGray
        return $false
    }
}

function Invoke-SQL {
    param([string]$Sql, [string]$Database = "")
    $mysqlArgs = @("-h", $Host, "-P", $Port, "-u", $Username)
    if ($Password) { $mysqlArgs += "-p$Password" }
    if ($Database) { $mysqlArgs += "-D", $Database }
    $mysqlArgs += "--default-character-set=utf8mb4"

    $result = $Sql | & mysql @mysqlArgs 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "SQL 执行失败: $result"
    }
    return $result
}

function Invoke-SQLFile {
    param([string]$File, [string]$Database = "")
    $mysqlArgs = @("-h", $Host, "-P", $Port, "-u", $Username)
    if ($Password) { $mysqlArgs += "-p$Password" }
    if ($Database) { $mysqlArgs += "-D", $Database }
    $mysqlArgs += "--default-character-set=utf8mb4"
    $mysqlArgs += "-f"  # 忽略非致命错误继续执行

    $result = & mysql @mysqlArgs -e "source $File" 2>&1
    return $LASTEXITCODE
}

function Initialize-Database {
    Banner

    # 1. 检查 MySQL
    if (-not (Test-MySQL)) {
        if (-not $NoPause) { Read-Host "  按 Enter 退出" }
        return
    }

    # 2. 创建数据库
    Write-Host "  [SETUP] 创建数据库 '$Database' " -NoNewline -ForegroundColor Yellow
    Invoke-SQL "CREATE DATABASE IF NOT EXISTS ``$Database`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    Write-Host "OK" -ForegroundColor Green

    # 3. 如果是重置模式，先清空
    if ($Reset) {
        Write-Host "  [RESET] 清空数据库 " -NoNewline -ForegroundColor Red
        $confirm = Read-Host "  确认清空数据库 '$Database'? 输入 YES 确认"
        if ($confirm -ne "YES") {
            Write-Host "  已取消" -ForegroundColor Yellow
            return
        }
        Invoke-SQL "DROP DATABASE ``$Database``;"
        Invoke-SQL "CREATE DATABASE ``$Database`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
        Write-Host "OK" -ForegroundColor Green
    }

    # 4. 执行迁移脚本
    $migrations = Get-ChildItem "$MigrationDir\V*.sql" | Sort-Object {
        # 按版本号排序（提取 V 和 __ 之间的数字）
        if ($_.Name -match '^V([\d.]+)') {
            [version]$Matches[1]
        } else {
            [version]"0.0.0"
        }
    }

    Write-Host ""
    Write-Host "  执行 $($migrations.Count) 个迁移脚本..." -ForegroundColor Cyan
    Write-Host ""

    $success = 0
    $failed = 0

    foreach ($m in $migrations) {
        $name = $m.Name
        Write-Host "  [MIGRATE] $name " -NoNewline -ForegroundColor Yellow

        $exitCode = Invoke-SQLFile -File $m.FullName -Database $Database

        if ($exitCode -eq 0) {
            Write-Host "OK" -ForegroundColor Green
            $success++
        } else {
            Write-Host "WARN (exit=$exitCode)" -ForegroundColor DarkYellow
            $failed++
        }
    }

    # 5. 验证
    Write-Host ""
    Write-Host "  [VERIFY] 检查表结构 " -NoNewline -ForegroundColor Yellow
    $tables = Invoke-SQL "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$Database';" -Database $Database
    Write-Host "OK ($tables 张表)" -ForegroundColor Green

    Write-Host "  [VERIFY] 检查用户数据 " -NoNewline -ForegroundColor Yellow
    $users = Invoke-SQL "SELECT COUNT(*) FROM dbs_user;" -Database $Database
    Write-Host "OK ($users 个用户)" -ForegroundColor Green

    Write-Host "  [VERIFY] 检查订单数据 " -NoNewline -ForegroundColor Yellow
    $orders = Invoke-SQL "SELECT COUNT(*) FROM dbs_order;" -Database $Database
    Write-Host "OK ($orders 个订单)" -ForegroundColor Green

    # 6. 汇总
    Write-Host ""
    Write-Host "  ======== 初始化完成 ========" -ForegroundColor Cyan
    Write-Host "  成功: $success / $($migrations.Count)" -ForegroundColor Green
    if ($failed -gt 0) {
        Write-Host "  警告: $failed (非致命，可忽略)" -ForegroundColor DarkYellow
    }
    Write-Host ""
    Write-Host "  账号信息:" -ForegroundColor Cyan
    Write-Host "    管理员: admin / 123456" -ForegroundColor DarkGray
    Write-Host "    测试用户: zhangsan / 123456" -ForegroundColor DarkGray
    Write-Host "    测试用户: lisi / 123456" -ForegroundColor DarkGray
    Write-Host "    测试用户: wangwu / 123456" -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "  启动服务: .\start-all.ps1 start" -ForegroundColor DarkGray
    Write-Host ""
}

# ======================== Entry ========================
Initialize-Database

if (-not $NoPause) {
    Read-Host "  按 Enter 关闭"
}
