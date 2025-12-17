#!/bin/bash

echo "=== ServiceLoader KSP Demo ==="
echo
echo "Building processor module..."
./gradlew :lib:ksp:serviceloader-demo:processor:build --info

echo
echo "Building test module (will trigger KSP processing)..."
./gradlew :lib:ksp:serviceloader-demo:test:build --info

echo
echo "Demo completed! Check the following locations:"
echo "1. Build logs for ServiceLoader output"
echo "2. build/generated/ksp directories for generated files"
echo "3. README.md for more information"