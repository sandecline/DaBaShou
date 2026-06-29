#!/bin/bash
# 搭把手 - 构建脚本
# 用法: ./scripts/build.sh

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT/backend"

echo "========================================="
echo " 搭把手 - 后端构建"
echo "========================================="

echo "[1/2] 编译..."
mvn clean compile -q

echo "[2/2] 打包 (跳过测试)..."
mvn package -DskipTests -q

echo ""
echo "✅ 构建成功!"
echo "产出: backend/dabashou-api/target/*.jar"
