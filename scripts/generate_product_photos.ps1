param(
  [string]$MysqlBin = "mysql",
  [string]$DbName = "mall_system",
  [string]$DbHost = "localhost",
  [string]$DbPort = "3306",
  [string]$DbUser = "root",
  [string]$DbPassword = $env:MALL_DB_PASSWORD
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
  $DbPassword = $env:MYSQL_PASSWORD
}

Add-Type -AssemblyName System.Drawing

$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$FrontendDir = Get-ChildItem -LiteralPath $Root -Directory | Where-Object {
  Test-Path (Join-Path $_.FullName "src\utils\images.js")
} | Select-Object -First 1
if ($null -eq $FrontendDir) {
  throw "Frontend directory not found"
}
$ImageRoot = Join-Path $FrontendDir.FullName "public\products\generated"
$SqlDir = Join-Path $Root "docs\sql"
$UpdateSql = Join-Path $SqlDir "update_1000_product_photos.sql"
$RollbackSql = Join-Path $SqlDir "rollback_1000_product_photos.sql"
$StartCode = "SP300001"
$EndCode = "SP301000"

function Sql-Quote([string]$value) {
  return "'" + (($value -replace "\\", "\\\\") -replace "'", "''") + "'"
}

function Get-ProductRows {
  $sql = "USE $DbName; SELECT product_no,name,IFNULL(category_name,''),IFNULL(brand_name,''),price,IFNULL(image_url,'') FROM product WHERE product_no BETWEEN '$StartCode' AND '$EndCode' ORDER BY product_no;"
  $args = @("-h$DbHost", "-P$DbPort", "--protocol=TCP", "-u$DbUser", "--default-character-set=utf8mb4", "-N", "-B", "-e", $sql)
  if (-not [string]::IsNullOrWhiteSpace($DbPassword)) {
    $args = @("-h$DbHost", "-P$DbPort", "--protocol=TCP", "-u$DbUser", "-p$DbPassword", "--default-character-set=utf8mb4", "-N", "-B", "-e", $sql)
  }
  $raw = & $MysqlBin @args
  if ($LASTEXITCODE -ne 0) {
    throw "MySQL query failed"
  }
  $rows = @()
  foreach ($line in $raw) {
    $parts = $line -split "`t"
    if ($parts.Count -lt 6) { continue }
    $imageUrl = $parts[5]
    $slug = "digital"
    if ($imageUrl -match "/products/generated/([^/]+)/") {
      $slug = $Matches[1]
    }
    $rows += [pscustomobject]@{
      ProductNo = $parts[0]
      Name = $parts[1]
      CategoryName = $parts[2]
      BrandName = $parts[3]
      Price = $parts[4]
      Slug = $slug
    }
  }
  return $rows
}

function New-Brush([string]$hex) {
  return New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml($hex))
}

function New-Pen2([string]$hex, [float]$width = 4) {
  return New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml($hex)), $width
}

function Get-RoundRect([float]$x, [float]$y, [float]$w, [float]$h, [float]$r) {
  $path = New-Object System.Drawing.Drawing2D.GraphicsPath
  $d = $r * 2
  $path.AddArc($x, $y, $d, $d, 180, 90)
  $path.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
  $path.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
  $path.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
  $path.CloseFigure()
  return $path
}

function Fill-RoundRect($g, [string]$hex, [float]$x, [float]$y, [float]$w, [float]$h, [float]$r) {
  $brush = New-Brush $hex
  $path = Get-RoundRect $x $y $w $h $r
  $g.FillPath($brush, $path)
  $path.Dispose()
  $brush.Dispose()
}

function Stroke-RoundRect($g, [string]$hex, [float]$x, [float]$y, [float]$w, [float]$h, [float]$r, [float]$width) {
  $pen = New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml($hex)), $width
  $path = Get-RoundRect $x $y $w $h $r
  $g.DrawPath($pen, $path)
  $path.Dispose()
  $pen.Dispose()
}

function Draw-Text($g, [string]$text, [float]$x, [float]$y, [float]$size, [string]$color, [string]$style = "Regular") {
  $fontStyle = [System.Drawing.FontStyle]::Regular
  if ($style -eq "Bold") { $fontStyle = [System.Drawing.FontStyle]::Bold }
  $font = New-Object System.Drawing.Font("Microsoft YaHei UI", $size, $fontStyle, [System.Drawing.GraphicsUnit]::Pixel)
  $brush = New-Brush $color
  $g.DrawString($text, $font, $brush, $x, $y)
  $brush.Dispose()
  $font.Dispose()
}

function Draw-StringCentered($g, [string]$text, [float]$x, [float]$y, [float]$w, [float]$size, [string]$color, [string]$style = "Regular") {
  $fontStyle = [System.Drawing.FontStyle]::Regular
  if ($style -eq "Bold") { $fontStyle = [System.Drawing.FontStyle]::Bold }
  $font = New-Object System.Drawing.Font("Microsoft YaHei UI", $size, $fontStyle, [System.Drawing.GraphicsUnit]::Pixel)
  $brush = New-Brush $color
  $format = New-Object System.Drawing.StringFormat
  $format.Alignment = [System.Drawing.StringAlignment]::Center
  $g.DrawString($text, $font, $brush, (New-Object System.Drawing.RectangleF($x, $y, $w, 80)), $format)
  $format.Dispose()
  $brush.Dispose()
  $font.Dispose()
}

function Fill-Ellipse($g, [string]$hex, [float]$x, [float]$y, [float]$w, [float]$h) {
  $brush = New-Brush $hex
  $g.FillEllipse($brush, $x, $y, $w, $h)
  $brush.Dispose()
}

function Fill-Rect($g, [string]$hex, [float]$x, [float]$y, [float]$w, [float]$h) {
  $brush = New-Brush $hex
  $g.FillRectangle($brush, $x, $y, $w, $h)
  $brush.Dispose()
}

function Stroke-Line($g, [string]$hex, [float]$x1, [float]$y1, [float]$x2, [float]$y2, [float]$width = 5) {
  $pen = New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml($hex)), $width
  $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
  $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
  $g.DrawLine($pen, $x1, $y1, $x2, $y2)
  $pen.Dispose()
}

function Draw-Projector($g) {
  Fill-RoundRect $g "#111827" 150 95 900 455 26
  Fill-RoundRect $g "#1f2937" 172 117 856 410 18
  Fill-Rect $g "#0f172a" 190 135 820 374
  Fill-Ellipse $g "#334155" 230 170 140 140
  Fill-Ellipse $g "#64748b" 252 192 96 96
  Fill-RoundRect $g "#e5e7eb" 450 185 420 210 14
  Fill-RoundRect $g "#3b82f6" 485 220 150 70 10
  Fill-RoundRect $g "#f97316" 660 220 160 70 10
  Draw-Text $g "HD SMART PROJECTOR" 485 335 34 "#111827" "Bold"
  Fill-RoundRect $g "#374151" 320 575 560 95 22
  Fill-Ellipse $g "#111827" 380 594 58 58
  Fill-Ellipse $g "#111827" 762 594 58 58
  Stroke-Line $g "#111827" 285 670 225 770 12
  Stroke-Line $g "#111827" 915 670 975 770 12
}

function Draw-Phone($g) {
  Fill-RoundRect $g "#111827" 420 115 360 610 48
  Fill-RoundRect $g "#f8fafc" 445 155 310 520 32
  Fill-RoundRect $g "#dbeafe" 475 205 250 120 18
  Fill-RoundRect $g "#bfdbfe" 475 350 105 105 18
  Fill-RoundRect $g "#93c5fd" 620 350 105 105 18
  Fill-RoundRect $g "#e0f2fe" 475 500 250 60 18
  Fill-Ellipse $g "#111827" 580 690 38 38
}

function Draw-Computer($g) {
  Fill-RoundRect $g "#111827" 210 120 780 450 28
  Fill-RoundRect $g "#dbeafe" 245 155 710 370 18
  Fill-RoundRect $g "#2563eb" 300 215 260 90 16
  Fill-RoundRect $g "#60a5fa" 595 215 300 90 16
  Fill-RoundRect $g "#ffffff" 300 340 595 92 16
  Fill-Rect $g "#334155" 540 570 120 85
  Fill-RoundRect $g "#111827" 380 650 440 55 18
  Fill-RoundRect $g "#e2e8f0" 330 730 540 80 20
}

function Draw-Headphones($g) {
  $pen = New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml("#111827")), 28
  $g.DrawArc($pen, 355, 165, 490, 410, 205, 130)
  $pen.Dispose()
  Fill-RoundRect $g "#2563eb" 285 365 155 230 46
  Fill-RoundRect $g "#2563eb" 760 365 155 230 46
  Fill-RoundRect $g "#1e293b" 330 410 80 150 30
  Fill-RoundRect $g "#1e293b" 790 410 80 150 30
  Fill-RoundRect $g "#f8fafc" 460 610 280 70 28
}

function Draw-Clothing($g) {
  Fill-RoundRect $g "#f97316" 455 140 290 520 38
  $brush = New-Brush "#fb923c"
  $points = @(
    (New-Object System.Drawing.PointF(455,180)),
    (New-Object System.Drawing.PointF(310,300)),
    (New-Object System.Drawing.PointF(405,410)),
    (New-Object System.Drawing.PointF(455,335))
  )
  $g.FillPolygon($brush, $points)
  $points = @(
    (New-Object System.Drawing.PointF(745,180)),
    (New-Object System.Drawing.PointF(890,300)),
    (New-Object System.Drawing.PointF(795,410)),
    (New-Object System.Drawing.PointF(745,335))
  )
  $g.FillPolygon($brush, $points)
  $brush.Dispose()
  Fill-RoundRect $g "#ffffff" 520 160 160 125 62
  Fill-RoundRect $g "#ea580c" 510 380 180 70 16
  Fill-RoundRect $g "#111827" 360 690 480 70 24
}

function Draw-Shoe($g) {
  Fill-RoundRect $g "#1e293b" 270 455 610 135 64
  Fill-RoundRect $g "#f97316" 405 365 340 150 48
  Fill-RoundRect $g "#ffffff" 240 570 700 65 28
  Stroke-Line $g "#ffffff" 460 430 620 390 8
  Stroke-Line $g "#ffffff" 510 455 660 415 8
  Fill-Ellipse $g "#e5e7eb" 750 450 84 84
}

function Draw-Home($g) {
  Fill-RoundRect $g "#94a3b8" 210 600 780 65 20
  Fill-RoundRect $g "#f8fafc" 310 245 220 300 22
  Fill-RoundRect $g "#22c55e" 350 315 140 72 16
  Fill-RoundRect $g "#fde68a" 625 220 190 245 28
  Stroke-Line $g "#111827" 720 465 720 610 14
  Fill-RoundRect $g "#111827" 650 610 140 42 18
  Fill-Ellipse $g "#dbeafe" 790 275 155 155
}

function Draw-Beauty($g) {
  Fill-RoundRect $g "#fdf2f8" 310 245 150 360 32
  Fill-RoundRect $g "#be185d" 335 175 100 90 22
  Fill-RoundRect $g "#f9a8d4" 520 190 170 430 40
  Fill-RoundRect $g "#db2777" 555 120 100 90 20
  Fill-RoundRect $g "#ffffff" 555 390 100 95 18
  Fill-RoundRect $g "#fce7f3" 740 300 150 310 34
}

function Draw-Food($g) {
  Fill-RoundRect $g "#7c2d12" 310 225 250 360 36
  Fill-RoundRect $g "#fed7aa" 345 280 180 120 22
  Draw-StringCentered $g "COFFEE" 345 312 180 38 "#7c2d12" "Bold"
  Fill-RoundRect $g "#f97316" 640 290 235 240 34
  Fill-RoundRect $g "#fff7ed" 680 330 155 85 20
  Fill-Ellipse $g "#fde68a" 230 520 220 120
  Fill-Ellipse $g "#facc15" 730 555 190 82
}

function Draw-Sport($g) {
  Fill-Ellipse $g "#f97316" 285 255 260 260
  Stroke-Line $g "#7c2d12" 415 260 415 510 8
  Stroke-Line $g "#7c2d12" 290 385 540 385 8
  Fill-RoundRect $g "#0f766e" 610 280 110 370 45
  Fill-RoundRect $g "#14b8a6" 735 335 230 80 32
  Fill-RoundRect $g "#111827" 620 650 310 45 18
}

function Draw-Book($g) {
  Fill-RoundRect $g "#fbbf24" 275 180 260 460 22
  Fill-RoundRect $g "#ffffff" 310 245 190 290 14
  Fill-RoundRect $g "#2563eb" 565 225 95 430 18
  Fill-RoundRect $g "#ef4444" 690 250 95 405 18
  Fill-RoundRect $g "#10b981" 815 205 95 450 18
  Stroke-Line $g "#111827" 260 675 940 675 12
}

function Draw-Baby($g) {
  Fill-RoundRect $g "#fde68a" 320 310 180 280 50
  Fill-RoundRect $g "#fef3c7" 360 230 100 100 38
  Fill-Ellipse $g "#fbbf24" 610 295 230 230
  Fill-Ellipse $g "#ffffff" 675 360 42 42
  Fill-Ellipse $g "#ffffff" 735 360 42 42
  Fill-RoundRect $g "#60a5fa" 585 580 280 72 24
}

function Draw-Pet($g) {
  Fill-Ellipse $g "#a78bfa" 410 235 300 250
  Fill-Ellipse $g "#8b5cf6" 335 240 105 120
  Fill-Ellipse $g "#8b5cf6" 680 240 105 120
  Fill-Ellipse $g "#111827" 500 345 28 28
  Fill-Ellipse $g "#111827" 595 345 28 28
  Fill-Ellipse $g "#fca5a5" 540 405 70 50
  Fill-RoundRect $g "#ffffff" 380 590 440 90 42
}

function Draw-Gift($g) {
  Fill-RoundRect $g "#ef4444" 330 310 540 330 34
  Fill-Rect $g "#fde68a" 565 310 80 330
  Fill-Rect $g "#fde68a" 330 445 540 78
  Fill-RoundRect $g "#f97316" 445 210 145 115 48
  Fill-RoundRect $g "#f97316" 610 210 145 115 48
  Draw-StringCentered $g "GIFT CARD" 415 690 370 42 "#7f1d1d" "Bold"
}

function Draw-Bag($g) {
  Fill-RoundRect $g "#111827" 395 220 410 450 56
  Fill-RoundRect $g "#374151" 450 300 300 290 36
  Stroke-Line $g "#94a3b8" 500 225 500 115 18
  Stroke-Line $g "#94a3b8" 700 225 700 115 18
  Stroke-Line $g "#94a3b8" 500 115 700 115 18
  Fill-RoundRect $g "#f97316" 520 380 160 90 22
}

function U([int[]]$codes) {
  return -join ($codes | ForEach-Object { [char]$_ })
}

function Text-HasAny([string]$text, [string[]]$words) {
  foreach ($word in $words) {
    if (-not [string]::IsNullOrWhiteSpace($word) -and $text.Contains($word)) {
      return $true
    }
  }
  return $false
}

function Get-VisualKind($row) {
  $text = "$($row.Name) $($row.CategoryName)"
  if (Text-HasAny $text @((U @(0x6295,0x5f71)), "projector")) { return "projector" }
  if (Text-HasAny $text @((U @(0x8033,0x673a)), "headphone", "earphone")) { return "headphones" }
  if (Text-HasAny $text @((U @(0x624b,0x673a)), (U @(0x652f,0x67b6)), "phone")) { return "phone" }
  if (Text-HasAny $text @((U @(0x7535,0x8111)), (U @(0x952e,0x76d8)), (U @(0x663e,0x793a)), (U @(0x529e,0x516c)), "keyboard", "monitor", "computer")) { return "computer" }
  if (Text-HasAny $text @((U @(0x978b)), "shoe")) { return "shoe" }
  if (Text-HasAny $text @((U @(0x5305)), (U @(0x7bb1,0x5305)), "bag")) { return "bag" }
  if (Text-HasAny $text @((U @(0x8863)), (U @(0x886b)), (U @(0x88e4)), (U @(0x88d9)), (U @(0x536b,0x8863)), "shirt", "clothing")) { return "clothing" }
  if (Text-HasAny $text @((U @(0x706f)), (U @(0x6536,0x7eb3)), (U @(0x676f)), (U @(0x5bb6,0x5c45)), "lamp", "home")) { return "home" }
  if (Text-HasAny $text @((U @(0x9999,0x6c34)), (U @(0x62a4,0x80a4)), (U @(0x9762,0x819c)), (U @(0x53e3,0x7ea2)), "beauty")) { return "beauty" }
  if (Text-HasAny $text @((U @(0x5496,0x5561)), (U @(0x96f6,0x98df)), (U @(0x8336)), (U @(0x996e)), (U @(0x98df,0x54c1)), "coffee", "food")) { return "food" }
  if (Text-HasAny $text @((U @(0x7bee,0x7403)), (U @(0x745c,0x4f3d)), (U @(0x8fd0,0x52a8)), (U @(0x6237,0x5916)), "sport", "fitness")) { return "sport" }
  if (Text-HasAny $text @((U @(0x56fe,0x4e66)), (U @(0x6587,0x5177)), (U @(0x4e66)), (U @(0x7b14)), "book", "office")) { return "book" }
  if (Text-HasAny $text @((U @(0x6bcd,0x5a74)), (U @(0x5a74,0x513f)), (U @(0x5976,0x74f6)), (U @(0x73a9,0x5177)), "baby")) { return "baby" }
  if (Text-HasAny $text @((U @(0x5ba0,0x7269)), "pet")) { return "pet" }
  if (Text-HasAny $text @((U @(0x793c,0x54c1)), (U @(0x793c,0x76d2)), (U @(0x793c,0x54c1,0x5361)), "gift")) { return "gift" }
  switch ($row.Slug) {
    "digital" { return "projector" }
    "mobile" { return "phone" }
    "computer" { return "computer" }
    "fashion" { return "clothing" }
    "clothing" { return "clothing" }
    "home" { return "home" }
    "beauty" { return "beauty" }
    "food" { return "food" }
    "outdoor" { return "sport" }
    "fitness" { return "sport" }
    "book" { return "book" }
    "office" { return "book" }
    "baby" { return "baby" }
    "pet" { return "pet" }
    "gift" { return "gift" }
    "bag" { return "bag" }
    default { return "projector" }
  }
}

function Draw-ObjectByKind($g, [string]$kind, [string]$slug, [int]$variant) {
  switch ($kind) {
    "projector" { Draw-Projector $g }
    "phone" { Draw-Phone $g }
    "computer" { Draw-Computer $g }
    "headphones" { Draw-Headphones $g }
    "clothing" { Draw-Clothing $g }
    "shoe" { Draw-Shoe $g }
    "home" { Draw-Home $g }
    "beauty" { Draw-Beauty $g }
    "food" { Draw-Food $g }
    "sport" { Draw-Sport $g }
    "book" { Draw-Book $g }
    "baby" { Draw-Baby $g }
    "pet" { Draw-Pet $g }
    "gift" { Draw-Gift $g }
    "bag" { Draw-Bag $g }
    default { Draw-ObjectBySlug $g $slug $variant }
  }
}

function Draw-ObjectBySlug($g, [string]$slug, [int]$variant) {
  switch ($slug) {
    "digital" { if ($variant % 3 -eq 0) { Draw-Projector $g } elseif ($variant % 3 -eq 1) { Draw-Headphones $g } else { Draw-Computer $g } }
    "mobile" { Draw-Phone $g }
    "computer" { Draw-Computer $g }
    "fashion" { if ($variant % 2 -eq 0) { Draw-Clothing $g } else { Draw-Shoe $g } }
    "clothing" { Draw-Clothing $g }
    "home" { Draw-Home $g }
    "beauty" { Draw-Beauty $g }
    "food" { Draw-Food $g }
    "outdoor" { Draw-Sport $g }
    "fitness" { Draw-Sport $g }
    "book" { Draw-Book $g }
    "office" { Draw-Book $g }
    "baby" { Draw-Baby $g }
    "pet" { Draw-Pet $g }
    "gift" { Draw-Gift $g }
    "bag" { Draw-Bag $g }
    default { Draw-Projector $g }
  }
}

function Get-Palette([string]$slug) {
  $map = @{
    digital = @("#e0f2fe", "#2563eb", "#0f172a")
    mobile = @("#e0f2fe", "#0284c7", "#0c4a6e")
    computer = @("#ede9fe", "#7c3aed", "#312e81")
    home = @("#dcfce7", "#16a34a", "#14532d")
    fashion = @("#fce7f3", "#db2777", "#831843")
    clothing = @("#ffe4e6", "#e11d48", "#881337")
    beauty = @("#fae8ff", "#c026d3", "#701a75")
    food = @("#ffedd5", "#ea580c", "#7c2d12")
    outdoor = @("#ccfbf1", "#0d9488", "#134e4a")
    fitness = @("#dcfce7", "#22c55e", "#14532d")
    book = @("#fef3c7", "#d97706", "#78350f")
    office = @("#e2e8f0", "#475569", "#0f172a")
    baby = @("#fef9c3", "#ca8a04", "#713f12")
    pet = @("#ede9fe", "#8b5cf6", "#4c1d95")
    gift = @("#fee2e2", "#ef4444", "#7f1d1d")
    bag = @("#f3e8ff", "#9333ea", "#581c87")
  }
  if ($map.ContainsKey($slug)) { return $map[$slug] }
  return $map["digital"]
}

function Draw-ProductPhoto($row, [string]$kind, [string]$path) {
  $palette = Get-Palette $row.Slug
  $bg = $palette[0]
  $primary = $palette[1]
  $dark = $palette[2]
  $bmp = New-Object System.Drawing.Bitmap 1200, 900
  $g = [System.Drawing.Graphics]::FromImage($bmp)
  $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit

  $rect = New-Object System.Drawing.Rectangle 0, 0, 1200, 900
  $c1 = [System.Drawing.ColorTranslator]::FromHtml($bg)
  $c2 = [System.Drawing.Color]::White
  $brush = New-Object System.Drawing.Drawing2D.LinearGradientBrush($rect, $c1, $c2, 45)
  $g.FillRectangle($brush, $rect)
  $brush.Dispose()

  Fill-RoundRect $g "#ffffff" 55 45 1090 785 34
  Fill-Ellipse $g $primary 865 80 260 260
  Fill-Ellipse $g "#ffffff" 920 130 150 150
  Fill-Rect $g "#f8fafc" 55 700 1090 130
  Fill-Ellipse $g "#cbd5e1" 260 710 680 55

  $variant = [Math]::Abs($row.ProductNo.GetHashCode())
  $visualKind = Get-VisualKind $row
  if ($kind -eq "scene") {
    Fill-RoundRect $g "#f8fafc" 120 120 960 545 28
    Fill-Rect $g "#e2e8f0" 120 560 960 105
    Draw-ObjectByKind $g $visualKind $row.Slug ($variant + 1)
    Draw-StringCentered $g "Scene Display" 410 95 380 34 $dark "Bold"
  } elseif ($kind -eq "detail") {
    Draw-ObjectByKind $g $visualKind $row.Slug ($variant + 2)
    Fill-RoundRect $g "#ffffff" 105 120 260 115 20
    Fill-RoundRect $g "#ffffff" 835 120 260 115 20
    Fill-RoundRect $g "#ffffff" 105 565 260 115 20
    Draw-StringCentered $g "Detail" 105 143 260 34 $dark "Bold"
    Draw-StringCentered $g "Local Image" 835 143 260 34 $dark "Bold"
    Draw-StringCentered $g "1200 x 900" 105 588 260 34 $dark "Bold"
  } else {
    Draw-ObjectByKind $g $visualKind $row.Slug $variant
  }

  $displayName = $row.Name
  if ([string]::IsNullOrWhiteSpace($displayName) -or $displayName.Length -gt 42) {
    $displayName = "$($row.BrandName) $($row.CategoryName)"
  }
  if ([string]::IsNullOrWhiteSpace($displayName)) {
    $displayName = $row.ProductNo
  }
  Draw-Text $g $displayName 90 735 42 "#111827" "Bold"
  Draw-Text $g ("RMB " + $row.Price) 90 795 36 "#f97316" "Bold"
  Draw-Text $g $row.ProductNo 930 790 28 "#64748b" "Bold"

  $dir = Split-Path -Parent $path
  if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
  $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
  $g.Dispose()
  $bmp.Dispose()
}

function Remove-OldBatchImages {
  if (-not (Test-Path $ImageRoot)) { New-Item -ItemType Directory -Force -Path $ImageRoot | Out-Null }
  $removed = 0
  Get-ChildItem -LiteralPath $ImageRoot -Recurse -File | Where-Object {
    $_.Name -like "SP30*.svg" -or $_.Name -like "SP30*.png"
  } | ForEach-Object {
    $stem = $_.BaseName.Split("-")[0]
    if ($stem -ge $StartCode -and $stem -le $EndCode) {
      Remove-Item -LiteralPath $_.FullName -Force
      $removed++
    }
  }
  return $removed
}

function Write-PhotoSql($rows) {
  if (-not (Test-Path $SqlDir)) { New-Item -ItemType Directory -Force -Path $SqlDir | Out-Null }
  $update = New-Object System.Collections.Generic.List[string]
  $rollback = New-Object System.Collections.Generic.List[string]
  $update.Add("-- Generated by scripts/generate_product_photos.ps1")
  $update.Add("SET NAMES utf8mb4;")
  $update.Add("START TRANSACTION;")
  $update.Add("DELETE pi FROM product_image pi JOIN product p ON p.id = pi.product_id WHERE p.product_no BETWEEN '$StartCode' AND '$EndCode';")
  $rollback.Add("-- Generated by scripts/generate_product_photos.ps1")
  $rollback.Add("SET NAMES utf8mb4;")
  $rollback.Add("START TRANSACTION;")
  $rollback.Add("DELETE pi FROM product_image pi JOIN product p ON p.id = pi.product_id WHERE p.product_no BETWEEN '$StartCode' AND '$EndCode';")
  foreach ($row in $rows) {
    $base = "/products/generated/$($row.Slug)/$($row.ProductNo)"
    $main = "$base-main.png"
    $scene = "$base-scene.png"
    $detail = "$base-detail.png"
    $update.Add("UPDATE product SET image_url=$(Sql-Quote $main), detail_image_url=$(Sql-Quote ($scene + ',' + $detail)), update_time=NOW() WHERE product_no=$(Sql-Quote $row.ProductNo) AND product_no BETWEEN '$StartCode' AND '$EndCode';")
    $sort = 1
    foreach ($p in @($main, $scene, $detail)) {
      $update.Add("INSERT INTO product_image (product_id,image_url,sort_no) SELECT id,$(Sql-Quote $p),$sort FROM product WHERE product_no=$(Sql-Quote $row.ProductNo);")
      $sort++
    }
    $oldMain = "$base-main.svg"
    $oldScene = "$base-scene.svg"
    $oldDetail = "$base-detail.svg"
    $rollback.Add("UPDATE product SET image_url=$(Sql-Quote $oldMain), detail_image_url=$(Sql-Quote ($oldScene + ',' + $oldDetail)), update_time=NOW() WHERE product_no=$(Sql-Quote $row.ProductNo) AND product_no BETWEEN '$StartCode' AND '$EndCode';")
    $sort = 1
    foreach ($p in @($oldMain, $oldScene, $oldDetail)) {
      $rollback.Add("INSERT INTO product_image (product_id,image_url,sort_no) SELECT id,$(Sql-Quote $p),$sort FROM product WHERE product_no=$(Sql-Quote $row.ProductNo);")
      $sort++
    }
  }
  $update.Add("COMMIT;")
  $rollback.Add("COMMIT;")
  [System.IO.File]::WriteAllLines($UpdateSql, $update, [System.Text.UTF8Encoding]::new($false))
  [System.IO.File]::WriteAllLines($RollbackSql, $rollback, [System.Text.UTF8Encoding]::new($false))
}

$rows = Get-ProductRows
if ($rows.Count -ne 1000) {
  throw "Expected 1000 products, got $($rows.Count)"
}

$removed = Remove-OldBatchImages
foreach ($row in $rows) {
  $baseDir = Join-Path $ImageRoot $row.Slug
  Draw-ProductPhoto $row "main" (Join-Path $baseDir "$($row.ProductNo)-main.png")
  Draw-ProductPhoto $row "scene" (Join-Path $baseDir "$($row.ProductNo)-scene.png")
  Draw-ProductPhoto $row "detail" (Join-Path $baseDir "$($row.ProductNo)-detail.png")
}
Write-PhotoSql $rows

Write-Host "Loaded products: $($rows.Count)"
Write-Host "Removed old batch images: $removed"
Write-Host "Generated PNG images: $($rows.Count * 3)"
Write-Host "Update SQL: $UpdateSql"
Write-Host "Rollback SQL: $RollbackSql"
