@echo off
echo ========================================
echo   BUDOWANIE MODA CHARLIEKOPACZ
echo ========================================
echo.
echo 1. Czyszczenie starych plikow...
call gradlew clean
echo.
echo 2. Budowanie moda (moze potrwac kilka minut)...
call gradlew build
echo.
echo 3. Sprawdzanie czy plik jest w folderze target...
if exist target\charliekopacz\*.jar (
    echo.
    echo ========================================
    echo   SUKCES! Mod jest gotowy w:
    echo   target\charliekopacz
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   BLAD! Mod sie nie zbudowal. 
    echo   Upewnij sie, ze masz JDK 21 zainstalowane.
    echo ========================================
)
pause
