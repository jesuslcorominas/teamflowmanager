#!/usr/bin/env bash
# Writes build-only placeholder GoogleService-Info plists for iosApp.
#
# The Xcode project lists GoogleService-Info.plist and GoogleService-Info-Prod.plist as
# bundle resources, but both are gitignored. Compiling the app only needs the files to
# exist: their content is read at runtime by FirebaseApp.configure(). These placeholders
# carry no real project values, so an app built with them compiles but cannot talk to
# Firebase. Signed builds for TestFlight (#501) must restore the real plists from secrets
# instead of calling this script.
#
# Existing files are never overwritten, so running this on a machine that already has
# the real plists is a no-op.
set -euo pipefail

DEST_DIR="${1:-iosApp/iosApp}"

for name in GoogleService-Info.plist GoogleService-Info-Prod.plist; do
  target="$DEST_DIR/$name"
  if [ -f "$target" ]; then
    echo "Keeping existing $target"
    continue
  fi
  cat > "$target" <<'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CI_PLACEHOLDER</key>
	<string>Build-only placeholder written by scripts/ci/write-placeholder-google-service-plists.sh</string>
</dict>
</plist>
PLIST
  echo "Wrote placeholder $target"
done
