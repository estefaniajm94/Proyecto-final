param(
    [string]$DeviceId = "emulator-5554"
)

$adbPath = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"

if (-not (Test-Path $adbPath)) {
    Write-Error "No se encontro adb en $adbPath"
    exit 1
}

$clipboardText = Get-Clipboard
if ([string]::IsNullOrWhiteSpace($clipboardText)) {
    Write-Error "El portapapeles esta vacio."
    exit 1
}

$bootCompleted = & $adbPath -s $DeviceId shell getprop sys.boot_completed
if ($LASTEXITCODE -ne 0 -or $bootCompleted.Trim() -ne "1") {
    Write-Error "El emulador $DeviceId no esta listo."
    exit 1
}

$escapedText = $clipboardText.Replace("`r", "")
$escapedText = $escapedText.Replace("`n", " ")
$escapedText = $escapedText.Replace("%", "\%")
$escapedText = $escapedText.Replace(" ", "%s")
$escapedText = $escapedText.Replace("&", "\&")
$escapedText = $escapedText.Replace("|", "\|")
$escapedText = $escapedText.Replace("<", "\<")
$escapedText = $escapedText.Replace(">", "\>")
$escapedText = $escapedText.Replace("(", "\(")
$escapedText = $escapedText.Replace(")", "\)")
$escapedText = $escapedText.Replace(";", "\;")
$escapedText = $escapedText.Replace('"', '\"')
$escapedText = $escapedText.Replace("'", "\'")

& $adbPath -s $DeviceId shell input text $escapedText

if ($LASTEXITCODE -ne 0) {
    Write-Error "No se pudo enviar el texto al emulador."
    exit 1
}

Write-Output "Texto del portapapeles enviado a $DeviceId."
