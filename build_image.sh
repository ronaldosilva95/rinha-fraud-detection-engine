#!/bin/bash
set -e

# Default to JAR build
BUILD_TYPE="${1:-jar}"

if [ "$BUILD_TYPE" = "native" ] || [ "$BUILD_TYPE" = "n" ]; then
    echo "🔨 Building GraalVM Native Image..."
    echo "=================================================="
    docker build -f Dockerfile.native -t rinha-fraud-detection-engine:latest-native .

    echo ""
    echo "✅ Native image built successfully!"
    echo ""
    echo "To run the native container, use:"
    echo "  docker run -p 8080:8080 rinha-fraud-detection-engine:latest-native"
    echo ""
    echo "Or update docker-compose to use: rinha-fraud-detection-engine:latest-native"
    echo ""
    echo "⚡ Advantages of Native Image:"
    echo "  - Faster startup time (~100ms vs 3-5s)"
    echo "  - Lower memory footprint"
    echo "  - Smaller image size"

elif [ "$BUILD_TYPE" = "jar" ] || [ "$BUILD_TYPE" = "j" ] || [ -z "$BUILD_TYPE" ]; then
    echo "📦 Building JAR image..."
    echo "=================================================="
    docker build -t rinha-fraud-detection-engine:latest .

    echo ""
    echo "✅ JAR image built successfully!"
    echo ""
    echo "To run the container, use:"
    echo "  docker run -p 8080:8080 rinha-fraud-detection-engine:latest"
    echo ""
    echo "Or use docker-compose:"
    echo "  docker-compose up"

else
    echo "❌ Invalid build type: $BUILD_TYPE"
    echo ""
    echo "Usage: ./build_image.sh [jar|native]"
    echo "  jar    - Build JAR version (default, more compatible)"
    echo "  native - Build GraalVM Native Image (faster startup)"
    echo ""
    echo "Examples:"
    echo "  ./build_image.sh          # Builds JAR (default)"
    echo "  ./build_image.sh jar      # Explicitly build JAR"
    echo "  ./build_image.sh native   # Build Native Image"
    echo "  ./build_image.sh n        # Short form for native"
    exit 1
fi
