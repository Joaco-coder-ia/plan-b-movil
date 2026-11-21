$ErrorActionPreference = 'Stop'
$lugares = @(
    @{nombre='Biblioteca de ejemplo';descripcion='Una pausa para leer y avanzar en tus apuntes. Lugar ficticio de demostración.';costo=0;minutos=30;interior=$true;latitud=-33.45;longitud=-70.66},
    @{nombre='Café de ejemplo';descripcion='Conversar o dibujar con algo caliente. Precio ilustrativo, no comercial.';costo=2500;minutos=45;interior=$true;latitud=-33.45;longitud=-70.66},
    @{nombre='Parque de ejemplo';descripcion='Caminar y despejarte al aire libre. Lugar ficticio de demostración.';costo=0;minutos=40;interior=$false;latitud=-33.45;longitud=-70.66}
)
$actuales = Invoke-RestMethod 'http://localhost:8081/api/lugares'
foreach ($lugar in $lugares) {
    if ($actuales.nombre -notcontains $lugar.nombre) {
        $json = $lugar | ConvertTo-Json
        Invoke-RestMethod 'http://localhost:8081/api/lugares' -Method Post -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($json)) | Out-Null
    }
}
Write-Output 'Catálogo de demostración disponible. Los lugares y precios son ficticios.'
