@REM Maven Wrapper - Maven kurulumu olmadan projeyi calistirmak icin
@REM Kullanim: mvnw.cmd spring-boot:run

@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"

@REM Maven wrapper olmadigi icin direkt Spring Boot JAR calistir
echo.
echo ============================================
echo   FitOl - Fitness ve Diyet Takip Uygulamasi
echo ============================================
echo.

@REM JAVA_HOME kontrolu
where java >nul 2>nul
if errorlevel 1 (
    echo [HATA] Java bulunamadi! Java 17 kurun.
    echo Indirme: https://adoptium.net/
    exit /b 1
)

echo Java bulundu:
java -version 2>&1
echo.

@REM Maven wrapper download URL
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"

if not exist "%MAVEN_PROJECTBASEDIR%.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%.mvn\wrapper"

if not exist "%WRAPPER_JAR%" (
    echo Maven wrapper indiriliyor...
    powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

@REM Properties dosyasi
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"
if not exist "%WRAPPER_PROPERTIES%" (
    echo distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip > "%WRAPPER_PROPERTIES%"
    echo wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar >> "%WRAPPER_PROPERTIES%"
)

java -jar "%WRAPPER_JAR%" %*
