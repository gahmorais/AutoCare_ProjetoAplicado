#!/usr/bin/env bash
# Instala o build debug e abre o app.
# Uso: ./install-app.sh [serial-do-device]
# Sem argumento, usa o único device conectado (ou ANDROID_SERIAL, se definido).
# Para ver os seriais disponíveis: adb devices
set -e

if [ -n "$1" ]; then
  export ANDROID_SERIAL="$1"
fi

./gradlew installDebug
adb shell am start -n "br.com.gabrielmorais.autocare/.ui.activities.main_screen.MainActivity"
