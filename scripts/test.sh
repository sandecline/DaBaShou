#!/bin/bash
# 搭把手 - 测试脚本
# 用法: ./scripts/test.sh

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT/backend"

echo "========================================="
echo " 搭把手 - 单元测试"
echo "========================================="

echo "[1/1] 运行测试..."
mvn test

echo ""
echo "✅ 全部测试通过!"
