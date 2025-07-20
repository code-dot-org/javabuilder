#!/bin/bash

# Fix AWS profile credential issue with rbenv
# This script helps resolve rbenv Ruby version issues with aws-google command

set -e

echo "🔧 Fixing AWS profile credential issue..."

# Check if rbenv is available
if command -v rbenv &> /dev/null; then
    echo "📋 rbenv found, checking Ruby versions..."
    
    # Check available Ruby versions
    rbenv versions
    
    echo ""
    echo "🔍 The issue is that aws-google command is not available in the current Ruby version."
    echo "Available Ruby versions with aws-google: 3.0.5, 3.1.0"
    echo ""
    echo "To fix this, you can:"
    echo "1. Set global Ruby version to 3.0.5 or 3.1.0:"
    echo "   rbenv global 3.0.5"
    echo "   or"
    echo "   rbenv global 3.1.0"
    echo ""
    echo "2. Or set local Ruby version for this project:"
    echo "   rbenv local 3.0.5"
    echo "   or"
    echo "   rbenv local 3.1.0"
    echo ""
    echo "3. Then rehash rbenv:"
    echo "   rbenv rehash"
    echo ""
    echo "4. Alternative: Use environment variables instead of profile:"
    echo "   export AWS_PROFILE=codeorg-dev"
    echo "   export AWS_DEFAULT_REGION=us-east-1"
    echo ""
    
    # Try to set Ruby version automatically
    read -p "Would you like me to try setting Ruby version to 3.0.5 locally? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if rbenv versions | grep -q "3.0.5"; then
            rbenv local 3.0.5
            rbenv rehash
            echo "✅ Ruby version set to 3.0.5 locally"
            echo "🧪 Testing AWS profile..."
            if aws sts get-caller-identity --profile codeorg-dev &> /dev/null; then
                echo "✅ AWS profile is now working!"
            else
                echo "❌ AWS profile still not working. Please check your credentials."
            fi
        else
            echo "❌ Ruby 3.0.5 is not installed. Please install it first with:"
            echo "   rbenv install 3.0.5"
        fi
    fi
else
    echo "❌ rbenv not found. Please install rbenv or use alternative authentication."
fi

echo ""
echo "📝 Alternative approaches:"
echo "1. Use AWS SSO if available"
echo "2. Use temporary credentials"
echo "3. Set environment variables directly"
echo "4. Use default profile with proper credentials"
