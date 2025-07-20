#!/bin/bash

# Step 2: Build Java Components
# This script builds the required Java artifacts and dependencies

set -e

echo "🔨 Starting Java Components Build..."

# Step 1: Check prerequisites
echo "📋 Checking build prerequisites..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 11 or higher."
    echo "On macOS: brew install openjdk@11"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo "✅ Java found: $JAVA_VERSION"

# Check if we're in the right directory
if [ ! -d "../org-code-javabuilder" ]; then
    echo "❌ org-code-javabuilder directory not found. Please run from dev-deployment directory."
    exit 1
fi

# Step 2: Build org-code-javabuilder
echo "🏗️  Building org-code-javabuilder..."
cd ../org-code-javabuilder

# Check if Gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Gradle wrapper not found in org-code-javabuilder directory"
    exit 1
fi

# Set Java home for Gradle (if needed)
export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(which java)))}
echo "Using JAVA_HOME: $JAVA_HOME"

# Build the project
echo "🔧 Running Gradle build..."
./gradlew clean build -x test --info

# Check if build artifacts were created
if [ ! -f "lib/build/distributions/lib.zip" ]; then
    echo "❌ Java build artifacts not found after build. Build may have failed."
    exit 1
fi

echo "✅ Java artifacts built successfully"

# Step 3: Create required directories and files
echo "🗂️  Creating required directories and files..."

# Create change_runtime_directory if it doesn't exist
if [ ! -d "change_runtime_directory" ]; then
    mkdir -p change_runtime_directory
    echo "#!/bin/bash" > change_runtime_directory/change_runtime_directory
    echo "# Runtime directory change script" >> change_runtime_directory/change_runtime_directory
    chmod +x change_runtime_directory/change_runtime_directory
    echo "✅ Created change_runtime_directory"
fi

# Create font_config.zip if it doesn't exist
if [ ! -f "font_config.zip" ]; then
    echo "🔤 Creating font configuration..."
    mkdir -p temp_font_config
    echo "# Font configuration for Java Lambda" > temp_font_config/fonts.conf
    cd temp_font_config
    zip -r ../font_config.zip .
    cd ..
    rm -rf temp_font_config
    echo "✅ Created font_config.zip"
fi

# Step 4: Build javabuilder-authorizer
echo "🔐 Building javabuilder-authorizer..."
cd ../javabuilder-authorizer

# Check if bundler is available
if ! command -v bundler &> /dev/null; then
    echo "⚠️  Bundler not found. Installing gems with gem install..."
    gem install bundler
fi

# Try to install gems (skip if Ruby version mismatch)
echo "💎 Installing Ruby gems..."
if bundle install --quiet 2>/dev/null; then
    echo "✅ Ruby gems installed successfully"
else
    echo "⚠️  Ruby gems installation skipped due to version mismatch"
    echo "    This may cause issues during deployment"
fi

# Step 5: Build api-gateway-routes
echo "🌐 Building api-gateway-routes..."
cd ../api-gateway-routes

# Try to install gems (skip if Ruby version mismatch)
echo "💎 Installing Ruby gems..."
if bundle install --quiet 2>/dev/null; then
    echo "✅ Ruby gems installed successfully"
else
    echo "⚠️  Ruby gems installation skipped due to version mismatch"
    echo "    This may cause issues during deployment"
fi

# Return to dev-deployment directory
cd ../dev-deployment

echo "✅ Java components build completed successfully!"
echo ""
echo "📝 Built artifacts:"
echo "   - org-code-javabuilder/lib/build/distributions/lib.zip"
echo "   - org-code-javabuilder/change_runtime_directory/"
echo "   - org-code-javabuilder/font_config.zip"
echo "   - javabuilder-authorizer/ (Ruby gems)"
echo "   - api-gateway-routes/ (Ruby gems)"
echo ""
echo "🔄 Next step:"
echo "   Run: ./03-deploy-application.sh"
