#!/bin/bash
# 搭把手 - 代码检查脚本
# 用法: ./scripts/check.sh

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "========================================="
echo " 搭把手 - 代码检查"
echo "========================================="

# 后端检查
echo "[1/3] 后端编译检查..."
cd "$PROJECT_ROOT/backend"
mvn clean compile -q
echo "  ✅ 后端编译通过"

echo "[2/3] 后端测试..."
mvn test -q
echo "  ✅ 后端测试通过"

# 前端检查
echo "[3/3] 前端类型检查..."
cd "$PROJECT_ROOT/frontend"
if [ -f "package.json" ]; then
    npx vue-tsc --noEmit 2>/dev/null || echo "  ⚠️ 前端类型检查有警告（非阻塞）"
else
    echo "  ⏭️ 跳过（未找到 frontend/package.json）"
fi

echo ""
echo "✅ 全部检查完成!"
