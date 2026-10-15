$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$toolsRoot = Join-Path $projectRoot '.tools'
$jdkRoot = Join-Path $toolsRoot 'jdk-17'
$mavenRoot = Join-Path $toolsRoot 'apache-maven-3.9.9'
$downloadRoot = Join-Path $toolsRoot 'downloads'

New-Item -ItemType Directory -Force -Path $toolsRoot, $downloadRoot | Out-Null

function Install-PortableZip {
    param(
        [string]$Url,
        [string]$ArchivePath,
        [string]$Destination,
        [string]$ExpectedFolder
    )

    Write-Host "Descargando $ExpectedFolder..."
    if (-not (Get-Command curl.exe -ErrorAction SilentlyContinue)) {
        throw 'No se encontró curl.exe, necesario para descargar las herramientas portables.'
    }
    & curl.exe --fail --location --tlsv1.2 --retry 3 --output $ArchivePath $Url
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo descargar $ExpectedFolder desde $Url"
    }
    $extractRoot = Join-Path $downloadRoot ([IO.Path]::GetFileNameWithoutExtension($ArchivePath))
    if (Test-Path -LiteralPath $extractRoot) { Remove-Item -LiteralPath $extractRoot -Recurse -Force }
    Expand-Archive -LiteralPath $ArchivePath -DestinationPath $extractRoot -Force
    $source = Get-ChildItem -LiteralPath $extractRoot -Directory | Where-Object { $_.Name -like $ExpectedFolder } | Select-Object -First 1
    if ($null -eq $source) { throw "No se encontró la carpeta esperada $ExpectedFolder" }
    if (Test-Path -LiteralPath $Destination) { Remove-Item -LiteralPath $Destination -Recurse -Force }
    Move-Item -LiteralPath $source.FullName -Destination $Destination
    Remove-Item -LiteralPath $ArchivePath -Force
    Remove-Item -LiteralPath $extractRoot -Recurse -Force
}

$javaExecutable = Join-Path $jdkRoot 'bin\java.exe'
$mavenExecutable = Join-Path $mavenRoot 'bin\mvn.cmd'

if (-not (Test-Path -LiteralPath $javaExecutable)) {
    Install-PortableZip `
        'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' `
        (Join-Path $downloadRoot 'jdk-17.zip') `
        $jdkRoot `
        'jdk-17*'
}

if (-not (Test-Path -LiteralPath $mavenExecutable)) {
    Install-PortableZip `
        'https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip' `
        (Join-Path $downloadRoot 'maven-3.9.9.zip') `
        $mavenRoot `
        'apache-maven-3.9.9'
}

Write-Host 'Java y Maven portables están disponibles en .tools.'
