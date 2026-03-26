@echo off
echo ========================================
echo   KOMPLEKSOWA NAPRAWA I BUDOWANIE
echo ========================================
echo.
echo 1. Pobieranie brakujacych plikow Gradle...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; New-Item -ItemType Directory -Force -Path 'gradle/wrapper'; Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.10.2/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'; Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradle/wrapper/gradle-wrapper.properties' -OutFile 'gradle/wrapper/gradle-wrapper.properties'; Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradlew' -OutFile 'gradlew'; Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradlew.bat' -OutFile 'gradlew.bat'"
echo.
echo 2. Czyszczenie projektu...
call gradlew clean
echo.
echo 3. Budowanie moda (to potrwa kilka minut, badz cierpliwy)...
echo Jezeli tu zobaczysz blad o Javie, zainstaluj JDK 21!
call gradlew build --no-daemon
echo.
echo 4. Sprawdzanie wyniku...
if exist build\libs\*.jar (
    mkdir target\charliekopacz 2>nul
    copy build\libs\*.jar target\charliekopacz\ 1>nul
    echo.
    echo ========================================
    echo   SUKCES! Mod jest w:
    echo   target\charliekopacz
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   NADAL BLAD. Sprawdz czy masz JDK 21!
    echo   Mozesz go pobrac z adoptium.net
    echo ========================================
)
pause
