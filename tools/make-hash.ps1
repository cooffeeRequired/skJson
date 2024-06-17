# Získání parametru souboru
param (
    [string]$FilePath
)

# Kontrola, zda soubor existuje
if (-Not (Test-Path -Path $FilePath)) {
    Write-Output "Soubor nenalezen: $FilePath"
    exit
}

# Výpočet hashe souboru pomocí PowerShellového příkazu Get-FileHash
$Hash = Get-FileHash -Path $FilePath | Select-Object -ExpandProperty Hash

# Výstup hashe
Write-Output "Hash souboru '$FilePath': $Hash"
