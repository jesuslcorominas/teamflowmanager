#!/bin/bash
# Installs git hooks for this project.
# Run once after cloning: bash scripts/install-hooks.sh

HOOKS_DIR="$(git rev-parse --show-toplevel)/.git/hooks"
SCRIPTS_DIR="$(git rev-parse --show-toplevel)/scripts/hooks"

echo "Installing git hooks..."

cp "$SCRIPTS_DIR/pre-push" "$HOOKS_DIR/pre-push"
chmod +x "$HOOKS_DIR/pre-push"

echo "✅ pre-push hook installed"
echo "   Pushes to release/* branches will auto-bump versionCode."
