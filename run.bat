@echo off
REM AFK2 Console Chess - Windows Build & Run Script
REM Usage: run.bat [command]
REM Commands: run (default), compile, clean, test, help

setlocal enabledelayedexpansion

set "SRC_DIR=src"
set "BIN_DIR=bin"
set "MAIN_CLASS=chess.Main"
set "TEST_CLASS=TestBotGame"

REM Default to 'run' if no argument provided
if "%1"=="" (
    set "COMMAND=run"
) else (
    set "COMMAND=%1"
)

if /i "%COMMAND%"=="help" (
    echo AFK2 Console Chess - Windows Build Script
    echo ==========================================
    echo.
    echo Usage: run.bat [command]
    echo.
    echo Commands:
    echo   run       - Compile and run the chess game (default)
    echo   compile   - Compile all Java source files
    echo   clean     - Remove all compiled files
    echo   test      - Compile and run bot integration tests
    echo   help      - Show this help message
    echo.
    echo Quick Start: run.bat run
    echo Or simply:  run.bat
    goto :eof
)

if /i "%COMMAND%"=="compile" (
    echo Compiling Java source files...
    if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
    javac -d %BIN_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\chess\*.java %SRC_DIR%\chess\core\*.java %SRC_DIR%\chess\pieces\*.java %SRC_DIR%\chess\rules\*.java %SRC_DIR%\chess\io\*.java %SRC_DIR%\chess\pgn\*.java %SRC_DIR%\chess\engine\*.java %SRC_DIR%\chess\util\*.java
    if errorlevel 1 (
        echo Error: Compilation failed
        exit /b 1
    )
    echo Compilation complete
    goto :eof
)

if /i "%COMMAND%"=="run" (
    echo Compiling Java source files...
    if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
    javac -d %BIN_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\chess\*.java %SRC_DIR%\chess\core\*.java %SRC_DIR%\chess\pieces\*.java %SRC_DIR%\chess\rules\*.java %SRC_DIR%\chess\io\*.java %SRC_DIR%\chess\pgn\*.java %SRC_DIR%\chess\engine\*.java %SRC_DIR%\chess\util\*.java
    if errorlevel 1 (
        echo Error: Compilation failed
        exit /b 1
    )
    echo.
    echo Starting AFK2 Console Chess...
    echo.
    java -cp %BIN_DIR% %MAIN_CLASS%
    goto :eof
)

if /i "%COMMAND%"=="clean" (
    echo Cleaning compiled files...
    if exist "%BIN_DIR%" (
        rmdir /s /q "%BIN_DIR%"
    )
    echo Clean complete
    goto :eof
)

if /i "%COMMAND%"=="test" (
    echo Compiling Java source files...
    if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
    javac -d %BIN_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\chess\*.java %SRC_DIR%\chess\core\*.java %SRC_DIR%\chess\pieces\*.java %SRC_DIR%\chess\rules\*.java %SRC_DIR%\chess\io\*.java %SRC_DIR%\chess\pgn\*.java %SRC_DIR%\chess\engine\*.java %SRC_DIR%\chess\util\*.java
    if errorlevel 1 (
        echo Error: Compilation failed
        exit /b 1
    )
    echo Running bot integration tests...
    javac -d %BIN_DIR% -cp %BIN_DIR% %TEST_CLASS%.java
    java -cp %BIN_DIR% TestBotGame
    goto :eof
)

echo Unknown command: %COMMAND%
echo Use 'run.bat help' for available commands
exit /b 1
