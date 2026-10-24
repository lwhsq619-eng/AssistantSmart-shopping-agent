$ErrorActionPreference = "Stop"

$localConfig = Join-Path $PSScriptRoot "application-local.yml"
$exampleConfig = Join-Path $PSScriptRoot "application-local.example.yml"

if (-not (Test-Path $localConfig)) {
    Copy-Item $exampleConfig $localConfig
    Write-Host "已生成本地数据库配置：$localConfig"
    Write-Host "请把 password: your_mysql_password_here 改成你的 MySQL root 密码后重新运行。"
    exit 1
}

$content = Get-Content -Raw -Encoding UTF8 $localConfig
if ($content -match "your_mysql_password_here") {
    Write-Host "请先修改 $localConfig 中的 MySQL 密码。"
    exit 1
}

mvn spring-boot:run
