@echo off
echo ==============================================
echo       GitHub Push Script for SmartTaskScheduler
echo ==============================================

:: Check if git is initialized, if not initialize it
if not exist ".git" (
    echo Initializing new Git repository...
    git init
)

:: Prompt for commit message
set /p COMMIT_MSG="Enter commit message (Press Enter for 'Initial commit'): "
if "%COMMIT_MSG%"=="" set COMMIT_MSG=Initial commit

:: Add and commit
git add .
git commit -m "%COMMIT_MSG%"
git branch -M main

:: Check if remote 'origin' exists
git remote -v | find "origin" > nul
if errorlevel 1 (
    set /p REPO_URL="Enter your GitHub repository URL (e.g., https://github.com/username/repo.git): "
    if not "!REPO_URL!"=="" (
        git remote add origin %REPO_URL%
    ) else (
        echo Error: No repository URL provided.
        pause
        exit /b
    )
)

:: Push to GitHub
echo Pushing to GitHub...
git push -u origin main

echo.
echo Done!
pause
