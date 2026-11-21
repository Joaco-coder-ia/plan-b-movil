param([switch]$Compilar)
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
if ($Compilar) {
    Push-Location (Join-Path $proyecto 'backend')
    try { & .\mvnw.cmd clean package; if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación del backend' } }
    finally { Pop-Location }
}
$registro = Join-Path $proyecto '.tools/servicios'
New-Item -ItemType Directory -Force -Path $registro | Out-Null
foreach ($servicio in @('lugares','planes')) {
    $carpeta = Join-Path $proyecto "backend/$servicio"
    $jar = Join-Path $carpeta "target/$servicio-1.0.0.jar"
    if (!(Test-Path -LiteralPath $jar)) { throw "Falta $jar. Ejecuta backend/mvnw.cmd package primero." }
    $pidFile = Join-Path $registro "$servicio.pid"
    if (Test-Path -LiteralPath $pidFile) {
        $previo = Get-Process -Id ([int](Get-Content -LiteralPath $pidFile)) -ErrorAction SilentlyContinue
        if ($previo) { Write-Output "$servicio ya tiene un proceso registrado: $($previo.Id)"; continue }
    }
    $proceso = Start-Process -FilePath 'java' -ArgumentList @('-jar',('"{0}"' -f $jar)) -WorkingDirectory $carpeta -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $registro "$servicio.log") -RedirectStandardError (Join-Path $registro "$servicio-error.log")
    $proceso.Id | Set-Content -LiteralPath $pidFile
    Write-Output "$servicio iniciado, PID $($proceso.Id). Revisa el log para confirmar el arranque."
}
