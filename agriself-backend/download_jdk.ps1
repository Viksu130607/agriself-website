$toolsDir = "C:\Users\Prashant\tools"
$jdkZip = Join-Path $toolsDir "jdk17.zip"

if (Test-Path $jdkZip) {
    Write-Host "Extracting existing jdk17.zip..."
    Expand-Archive -Path $jdkZip -DestinationPath $toolsDir -Force
    Remove-Item $jdkZip -Force
}

$jdkDir = Get-ChildItem -Path $toolsDir -Directory -Filter "jdk-17*" | Select-Object -First 1
if ($jdkDir) {
    Write-Host "Found JDK at $($jdkDir.FullName)"
    $javaExe = Join-Path $jdkDir.FullName "bin\java.exe"
    & $javaExe -version
} else {
    Write-Host "JDK not found."
}

$mvnZip = Join-Path $toolsDir "maven.zip"
$mvnDir = Get-ChildItem -Path $toolsDir -Directory -Filter "apache-maven*" | Select-Object -First 1
if (!$mvnDir) {
    Write-Host "Downloading Apache Maven..."
    curl.exe -L -o $mvnZip "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    Start-Sleep -Seconds 2
    Write-Host "Extracting Apache Maven..."
    Expand-Archive -Path $mvnZip -DestinationPath $toolsDir -Force
    Remove-Item $mvnZip -Force
    $mvnDir = Get-ChildItem -Path $toolsDir -Directory -Filter "apache-maven*" | Select-Object -First 1
}

if ($mvnDir) {
    Write-Host "Found Maven at $($mvnDir.FullName)"
    $mvnCmd = Join-Path $mvnDir.FullName "bin\mvn.cmd"
    & $mvnCmd -version
}
