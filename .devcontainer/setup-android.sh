#!/usr/bin/env bash
set -euo pipefail

# Install a reproducible Android build toolchain in a GitHub Codespace.
sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
  openjdk-17-jdk-headless curl unzip ca-certificates
sudo rm -rf /var/lib/apt/lists/*

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:/opt/gradle-8.9/bin:$PATH"

if [ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
  mkdir -p /tmp/android-cli "$ANDROID_HOME/cmdline-tools"
  curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/android-cli.zip
  unzip -q /tmp/android-cli.zip -d /tmp/android-cli
  sudo rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  sudo mv /tmp/android-cli/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
  rm -rf /tmp/android-cli /tmp/android-cli.zip
fi

if [ ! -x /opt/gradle-8.9/bin/gradle ]; then
  curl -fsSL https://services.gradle.org/distributions/gradle-8.9-bin.zip -o /tmp/gradle-8.9-bin.zip
  sudo unzip -q /tmp/gradle-8.9-bin.zip -d /opt
  sudo chown -R root:root /opt/gradle-8.9
  rm /tmp/gradle-8.9-bin.zip
fi

yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses >/dev/null
sdkmanager --sdk_root="$ANDROID_HOME" \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;35.0.0" \
  "ndk;27.2.12479018" \
  "cmake;3.22.1"

{
  echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64'
  echo 'export ANDROID_HOME=/opt/android-sdk'
  echo 'export ANDROID_SDK_ROOT="$ANDROID_HOME"'
  echo 'export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:/opt/gradle-8.9/bin:$PATH"'
} >> "$HOME/.bashrc"

echo "Android toolchain installed. Java: $(java -version 2>&1 | head -n 1)"
echo "Gradle: $(/opt/gradle-8.9/bin/gradle --version | grep '^Gradle ' | head -n 1)"
echo "SDK packages installed. Build with: gradle -p android :app:assembleDebug"
