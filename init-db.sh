#!/usr/bin/env bash
# ============================================================================
# 搭把手数据库一键初始化脚本 (Linux/Mac)
# 用法:
#   ./init-db.sh                          # 默认 root 无密码
#   ./init-db.sh -p mypassword            # 指定密码
#   ./init-db.sh -P 3307                  # 自定义端口
#   ./init-db.sh --reset                  # 清空重建（危险！）
# ============================================================================

set -euo pipefail

# ======================== 参数解析 ========================

DB_HOST="127.0.0.1"
DB_PORT=3306
DB_USER="root"
DB_PASS=""
DB_NAME="dabashou"
RESET=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--host)   DB_HOST="$2"; shift 2;;
        -P|--port)   DB_PORT="$2"; shift 2;;
        -u|--user)   DB_USER="$2"; shift 2;;
        -p|--pass)   DB_PASS="$2"; shift 2;;
        -d|--db)     DB_NAME="$2"; shift 2;;
        --reset)     RESET=true; shift;;
        *)           echo "未知参数: $1"; exit 1;;
    esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MIGRATION_DIR="$SCRIPT_DIR/database/migration"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;37m'
NC='\033[0m'

# ======================== 函数 ========================

mysql_cmd() {
    local extra_args=("$@")
    local cmd=(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" --default-character-set=utf8mb4)
    if [[ -n "$DB_PASS" ]]; then
        cmd+=(-p"$DB_PASS")
    fi
    "${cmd[@]}" "${extra_args[@]}"
}

mysql_exec() {
    mysql_cmd -e "$1"
}

mysql_exec_db() {
    mysql_cmd -D "$DB_NAME" -e "$1"
}

mysql_source() {
    mysql_cmd -D "$DB_NAME" -f < "$1"
}

banner() {
    echo ""
    echo -e "  ${CYAN}=========================================${NC}"
    echo -e "  ${CYAN}  搭把手数据库初始化工具${NC}"
    echo -e "  ${CYAN}=========================================${NC}"
    echo ""
}

check_mysql() {
    echo -e "  ${YELLOW}[CHECK] MySQL 连接${NC} ... "
    if mysql_exec "SELECT 1" &>/dev/null; then
        echo -e "  ${GREEN}OK${NC}"
        return 0
    else
        echo -e "  ${RED}FAIL: 无法连接 MySQL ($DB_HOST:$DB_PORT)${NC}"
        echo -e "  ${GRAY}请确保 MySQL 已启动，且 mysql 客户端在 PATH 中${NC}"
        return 1
    fi
}

# ======================== 主流程 ========================

banner

# 1. 检查 MySQL
check_mysql || exit 1

# 2. 创建数据库
echo -e "  ${YELLOW}[SETUP] 创建数据库 '$DB_NAME'${NC} ... "
mysql_exec "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
echo -e "  ${GREEN}OK${NC}"

# 3. 重置模式
if $RESET; then
    echo -e "  ${RED}[RESET] 清空数据库${NC}"
    read -p "  确认清空数据库 '$DB_NAME'? 输入 YES 确认: " confirm
    if [[ "$confirm" != "YES" ]]; then
        echo -e "  ${YELLOW}已取消${NC}"
        exit 0
    fi
    mysql_exec "DROP DATABASE \`$DB_NAME\`;"
    mysql_exec "CREATE DATABASE \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    echo -e "  ${GREEN}OK${NC}"
fi

# 4. 执行迁移脚本
MIGRATIONS=()
while IFS= read -r f; do
    MIGRATIONS+=("$f")
done < <(ls "$MIGRATION_DIR"/V*.sql 2>/dev/null | sort -t'V' -k2 -V)

echo ""
echo -e "  执行 ${#MIGRATIONS[@]} 个迁移脚本..."
echo ""

SUCCESS=0
FAILED=0

for m in "${MIGRATIONS[@]}"; do
    name=$(basename "$m")
    printf "  ${YELLOW}[MIGRATE] %-45s${NC} " "$name"

    if mysql_source "$m" &>/dev/null; then
        echo -e "${GREEN}OK${NC}"
        ((SUCCESS++))
    else
        echo -e "${YELLOW}WARN${NC}"
        ((FAILED++))
    fi
done

# 5. 验证
echo ""
printf "  ${YELLOW}[VERIFY] 检查表结构${NC} ... "
TABLES=$(mysql_exec_db "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME';" -N 2>/dev/null)
echo -e "${GREEN}OK ($TABLES 张表)${NC}"

printf "  ${YELLOW}[VERIFY] 检查用户数据${NC} ... "
USERS=$(mysql_exec_db "SELECT COUNT(*) FROM dbs_user;" -N 2>/dev/null)
echo -e "${GREEN}OK ($USERS 个用户)${NC}"

printf "  ${YELLOW}[VERIFY] 检查订单数据${NC} ... "
ORDERS=$(mysql_exec_db "SELECT COUNT(*) FROM dbs_order;" -N 2>/dev/null)
echo -e "${GREEN}OK ($ORDERS 个订单)${NC}"

# 6. 汇总
echo ""
echo -e "  ${CYAN}======== 初始化完成 ========${NC}"
echo -e "  成功: ${GREEN}$SUCCESS${NC} / ${#MIGRATIONS[@]}"
if [[ $FAILED -gt 0 ]]; then
    echo -e "  警告: ${YELLOW}$FAILED${NC} (非致命，可忽略)"
fi
echo ""
echo -e "  ${CYAN}账号信息:${NC}"
echo -e "    ${GRAY}管理员: admin / 123456${NC}"
echo -e "    ${GRAY}测试用户: zhangsan / 123456${NC}"
echo -e "    ${GRAY}测试用户: lisi / 123456${NC}"
echo -e "    ${GRAY}测试用户: wangwu / 123456${NC}"
echo ""
echo -e "  ${GRAY}启动后端: cd backend && mvn spring-boot:run${NC}"
echo ""
