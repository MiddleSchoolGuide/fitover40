param()

Add-Type -AssemblyName System.Drawing

$repoRoot = Split-Path -Parent $PSScriptRoot
$resRoot = Join-Path $repoRoot "app\src\main\res"
$assetRoot = Join-Path $repoRoot "playstore-assets"
$iconRoot = Join-Path $assetRoot "icon"
$featureRoot = Join-Path $assetRoot "feature-graphic"
$phoneScreenshotRoot = Join-Path $assetRoot "screenshots\phone"
$tabletScreenshotRoot = Join-Path $assetRoot "screenshots\tablet-7in"
$tablet10ScreenshotRoot = Join-Path $assetRoot "screenshots\tablet-10in"

$densities = @(
    @{ Folder = "mipmap-mdpi"; Size = 48 },
    @{ Folder = "mipmap-hdpi"; Size = 72 },
    @{ Folder = "mipmap-xhdpi"; Size = 96 },
    @{ Folder = "mipmap-xxhdpi"; Size = 144 },
    @{ Folder = "mipmap-xxxhdpi"; Size = 192 }
)

$teal = [System.Drawing.Color]::FromArgb(0, 137, 123)
$navy = [System.Drawing.Color]::FromArgb(16, 42, 67)
$blue = [System.Drawing.Color]::FromArgb(21, 101, 192)
$softTeal = [System.Drawing.Color]::FromArgb(41, 182, 166)
$offWhite = [System.Drawing.Color]::FromArgb(246, 248, 252)
$white = [System.Drawing.Color]::White

function Ensure-Directory([string]$path) {
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Path $path | Out-Null
    }
}

function New-SolidBitmap([int]$size) {
    return New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
}

function New-TransparentBitmap([int]$size) {
    return New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

function Draw-Runner([System.Drawing.Graphics]$graphics, [int]$size, [bool]$roundBackground) {
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $padding = if ($roundBackground) { $size * 0.09 } else { $size * 0.14 }
    $workSize = $size - ($padding * 2)
    $offsetX = $padding
    $offsetY = $padding
    $stroke = [Math]::Max(4, [int][Math]::Round($workSize * 0.075))
    $dumbbellStroke = [Math]::Max(4, [int][Math]::Round($workSize * 0.075))
    $thinStroke = [Math]::Max(2, [int][Math]::Round($workSize * 0.03))

    $pen = New-Object System.Drawing.Pen($white, $stroke)
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round

    $dumbbellPen = New-Object System.Drawing.Pen($white, $dumbbellStroke)
    $dumbbellPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $dumbbellPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round

    $weightPen = New-Object System.Drawing.Pen($white, [Math]::Max(3, [int][Math]::Round($workSize * 0.045)))
    $weightPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $weightPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round

    $accentPen = New-Object System.Drawing.Pen($white, $thinStroke)
    $accentPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $accentPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round

    $headDiameter = $workSize * 0.14
    $headX = $offsetX + ($workSize * 0.27)
    $headY = $offsetY + ($workSize * 0.12)
    $headBrush = [System.Drawing.SolidBrush]::new($white)
    $graphics.FillEllipse($headBrush, $headX, $headY, $headDiameter, $headDiameter)
    $headBrush.Dispose()

    $points = @{
        Neck = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.35), $offsetY + ($workSize * 0.26))
        Torso = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.43), $offsetY + ($workSize * 0.39))
        Hip = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.53), $offsetY + ($workSize * 0.49))
        FrontKnee = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.68), $offsetY + ($workSize * 0.65))
        FrontFoot = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.80), $offsetY + ($workSize * 0.82))
        RearKnee = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.44), $offsetY + ($workSize * 0.66))
        RearFoot = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.24), $offsetY + ($workSize * 0.83))
        RearArm = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.31), $offsetY + ($workSize * 0.44))
        FrontArm = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.60), $offsetY + ($workSize * 0.31))
        DumbbellLeft = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.62), $offsetY + ($workSize * 0.22))
        DumbbellRight = [System.Drawing.PointF]::new($offsetX + ($workSize * 0.86), $offsetY + ($workSize * 0.22))
    }

    $graphics.DrawLine($pen, $points.Neck, $points.Torso)
    $graphics.DrawLine($pen, $points.Torso, $points.Hip)
    $graphics.DrawLine($pen, $points.Torso, $points.FrontArm)
    $graphics.DrawLine($pen, $points.Torso, $points.RearArm)
    $graphics.DrawLine($pen, $points.Hip, $points.FrontKnee)
    $graphics.DrawLine($pen, $points.FrontKnee, $points.FrontFoot)
    $graphics.DrawLine($pen, $points.Hip, $points.RearKnee)
    $graphics.DrawLine($pen, $points.RearKnee, $points.RearFoot)

    $graphics.DrawLine($dumbbellPen, $points.DumbbellLeft, $points.DumbbellRight)

    $weightBars = @(
        ($offsetX + ($workSize * 0.66)),
        ($offsetX + ($workSize * 0.81))
    )
    foreach ($x in $weightBars) {
        $graphics.DrawLine($weightPen,
            [System.Drawing.PointF]::new($x, $offsetY + ($workSize * 0.14)),
            [System.Drawing.PointF]::new($x, $offsetY + ($workSize * 0.30)))
    }

    $accentBars = @(
        ($offsetX + ($workSize * 0.62)),
        ($offsetX + ($workSize * 0.86))
    )
    foreach ($x in $accentBars) {
        $graphics.DrawLine($accentPen,
            [System.Drawing.PointF]::new($x, $offsetY + ($workSize * 0.13)),
            [System.Drawing.PointF]::new($x, $offsetY + ($workSize * 0.31)))
    }

    $pen.Dispose()
    $dumbbellPen.Dispose()
    $weightPen.Dispose()
    $accentPen.Dispose()
}

function Save-SquareIcon([string]$path, [int]$size) {
    $bitmap = New-TransparentBitmap $size
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $backgroundBrush = [System.Drawing.SolidBrush]::new($teal)
    $inset = [int][Math]::Round($size * 0.08)
    $backgroundPath = Get-RoundedRectanglePath -x $inset -y $inset -width ($size - ($inset * 2)) -height ($size - ($inset * 2)) -radius ([Math]::Max(4, [int][Math]::Round($size * 0.18)))
    $graphics.FillPath($backgroundBrush, $backgroundPath)
    $backgroundBrush.Dispose()
    $backgroundPath.Dispose()
    Draw-Runner -graphics $graphics -size $size -roundBackground:$false
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

function Save-RoundIcon([string]$path, [int]$size) {
    $bitmap = New-TransparentBitmap $size
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

    $circleBrush = [System.Drawing.SolidBrush]::new($teal)
    $circleInset = [int][Math]::Round($size * 0.02)
    $graphics.FillEllipse($circleBrush, $circleInset, $circleInset, $size - ($circleInset * 2), $size - ($circleInset * 2))
    $circleBrush.Dispose()

    Draw-Runner -graphics $graphics -size $size -roundBackground:$true
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

function Get-RoundedRectanglePath([float]$x, [float]$y, [float]$width, [float]$height, [float]$radius) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $radius * 2
    $path.AddArc($x, $y, $diameter, $diameter, 180, 90)
    $path.AddArc($x + $width - $diameter, $y, $diameter, $diameter, 270, 90)
    $path.AddArc($x + $width - $diameter, $y + $height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($x, $y + $height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    return $path
}

function DrawPhonePreview([System.Drawing.Graphics]$graphics, [System.Drawing.Image]$image, [float]$x, [float]$y, [float]$width, [float]$height) {
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $outerPath = Get-RoundedRectanglePath -x $x -y $y -width $width -height $height -radius 26
    $shadowBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(56, 8, 22, 36))
    $shadowPath = Get-RoundedRectanglePath -x ($x + 8) -y ($y + 12) -width $width -height $height -radius 26
    $graphics.FillPath($shadowBrush, $shadowPath)
    $shadowBrush.Dispose()
    $shadowPath.Dispose()

    $frameBrush = [System.Drawing.SolidBrush]::new($navy)
    $graphics.FillPath($frameBrush, $outerPath)
    $frameBrush.Dispose()

    $screenInset = 10
    $screenX = $x + $screenInset
    $screenY = $y + $screenInset + 6
    $screenWidth = $width - ($screenInset * 2)
    $screenHeight = $height - ($screenInset * 2) - 10
    $screenPath = Get-RoundedRectanglePath -x $screenX -y $screenY -width $screenWidth -height $screenHeight -radius 18

    $previousClip = $graphics.Clip
    $graphics.SetClip($screenPath)
    $graphics.DrawImage($image, [System.Drawing.RectangleF]::new($screenX, $screenY, $screenWidth, $screenHeight))
    $graphics.Clip = $previousClip

    $notchBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(220, 7, 18, 29))
    $graphics.FillRectangle($notchBrush, $x + ($width * 0.35), $y + 8, $width * 0.30, 8)
    $notchBrush.Dispose()

    $screenPath.Dispose()
    $outerPath.Dispose()
}

function DrawTabletScreenshot([System.Drawing.Graphics]$graphics, [System.Drawing.Image]$image, [string]$title, [string]$subtitle) {
    $width = 1200
    $height = 1920
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $graphics.Clear($offWhite)

    $heroBrush = [System.Drawing.SolidBrush]::new($navy)
    $graphics.FillRectangle($heroBrush, 0, 0, $width, 440)
    $heroBrush.Dispose()

    $tealShape = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(32, $softTeal))
    $graphics.FillEllipse($tealShape, -120, -80, 520, 520)
    $graphics.FillEllipse($tealShape, 830, 210, 420, 300)
    $tealShape.Dispose()

    $diagonalBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(36, $teal))
    $graphics.FillPolygon($diagonalBrush, @(
        [System.Drawing.Point]::new(520, -20),
        [System.Drawing.Point]::new(760, -20),
        [System.Drawing.Point]::new(470, 480),
        [System.Drawing.Point]::new(230, 480)
    ))
    $diagonalBrush.Dispose()

    $iconPath = Join-Path $iconRoot "fitover40-playstore-icon-512.png"
    $icon = [System.Drawing.Image]::FromFile($iconPath)
    $graphics.DrawImage($icon, 90, 88, 116, 116)
    $icon.Dispose()

    $titleFont = New-Object System.Drawing.Font("Segoe UI", 44, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $subtitleFont = New-Object System.Drawing.Font("Segoe UI", 28, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $brandFont = New-Object System.Drawing.Font("Segoe UI", 34, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $whiteBrush = [System.Drawing.SolidBrush]::new($white)
    $offWhiteBrush = [System.Drawing.SolidBrush]::new($offWhite)

    $graphics.DrawString("FitOver40", $brandFont, $whiteBrush, 236, 106)
    $graphics.DrawString($title, $titleFont, $whiteBrush, [System.Drawing.RectangleF]::new(90, 240, 700, 70))
    $graphics.DrawString($subtitle, $subtitleFont, $offWhiteBrush, [System.Drawing.RectangleF]::new(90, 318, 780, 80))

    $frameX = 160
    $frameY = 520
    $frameWidth = 880
    $frameHeight = 1260
    $framePath = Get-RoundedRectanglePath -x $frameX -y $frameY -width $frameWidth -height $frameHeight -radius 48
    $shadowPath = Get-RoundedRectanglePath -x ($frameX + 14) -y ($frameY + 18) -width $frameWidth -height $frameHeight -radius 48
    $shadowBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(46, 10, 25, 40))
    $frameBrush = [System.Drawing.SolidBrush]::new($navy)
    $graphics.FillPath($shadowBrush, $shadowPath)
    $graphics.FillPath($frameBrush, $framePath)
    $shadowBrush.Dispose()
    $frameBrush.Dispose()
    $shadowPath.Dispose()

    $screenInsetX = 24
    $screenInsetTop = 26
    $screenInsetBottom = 30
    $screenX = $frameX + $screenInsetX
    $screenY = $frameY + $screenInsetTop
    $screenWidth = $frameWidth - ($screenInsetX * 2)
    $screenHeight = $frameHeight - $screenInsetTop - $screenInsetBottom
    $screenPath = Get-RoundedRectanglePath -x $screenX -y $screenY -width $screenWidth -height $screenHeight -radius 30

    $previousClip = $graphics.Clip
    $graphics.SetClip($screenPath)
    $graphics.DrawImage($image, [System.Drawing.RectangleF]::new($screenX, $screenY, $screenWidth, $screenHeight))
    $graphics.Clip = $previousClip

    $cameraBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(220, 8, 20, 32))
    $graphics.FillEllipse($cameraBrush, ($frameX + ($frameWidth / 2) - 10), ($frameY + 8), 20, 20)
    $cameraBrush.Dispose()

    $homeBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(170, 220, 230, 240))
    $graphics.FillEllipse($homeBrush, ($frameX + ($frameWidth / 2) - 26), ($frameY + $frameHeight - 18), 52, 8)
    $homeBrush.Dispose()

    $titleFont.Dispose()
    $subtitleFont.Dispose()
    $brandFont.Dispose()
    $whiteBrush.Dispose()
    $offWhiteBrush.Dispose()
    $framePath.Dispose()
    $screenPath.Dispose()
}

function Save-TabletScreenshot([string]$path, [string]$sourcePath, [string]$title, [string]$subtitle) {
    $canvas = New-Object System.Drawing.Bitmap(1200, 1920, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    $source = [System.Drawing.Image]::FromFile($sourcePath)
    DrawTabletScreenshot -graphics $graphics -image $source -title $title -subtitle $subtitle
    $source.Dispose()
    $graphics.Dispose()
    $canvas.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $canvas.Dispose()
}

function DrawTablet10Screenshot([System.Drawing.Graphics]$graphics, [System.Drawing.Image]$image, [string]$title, [string]$subtitle) {
    $width = 1920
    $height = 1200
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $graphics.Clear($offWhite)

    $heroBrush = [System.Drawing.SolidBrush]::new($navy)
    $graphics.FillRectangle($heroBrush, 0, 0, 760, $height)
    $heroBrush.Dispose()

    $shapeBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(32, $softTeal))
    $graphics.FillEllipse($shapeBrush, -120, -80, 560, 560)
    $graphics.FillEllipse($shapeBrush, 420, 780, 420, 320)
    $shapeBrush.Dispose()

    $diagonalBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(38, $teal))
    $graphics.FillPolygon($diagonalBrush, @(
        [System.Drawing.Point]::new(360, -40),
        [System.Drawing.Point]::new(560, -40),
        [System.Drawing.Point]::new(280, 1240),
        [System.Drawing.Point]::new(80, 1240)
    ))
    $diagonalBrush.Dispose()

    $iconPath = Join-Path $iconRoot "fitover40-playstore-icon-512.png"
    $icon = [System.Drawing.Image]::FromFile($iconPath)
    $graphics.DrawImage($icon, 88, 94, 118, 118)
    $icon.Dispose()

    $brandFont = New-Object System.Drawing.Font("Segoe UI", 36, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $titleFont = New-Object System.Drawing.Font("Segoe UI", 48, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $subtitleFont = New-Object System.Drawing.Font("Segoe UI", 28, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $whiteBrush = [System.Drawing.SolidBrush]::new($white)
    $offWhiteBrush = [System.Drawing.SolidBrush]::new($offWhite)

    $graphics.DrawString("FitOver40", $brandFont, $whiteBrush, 236, 116)
    $graphics.DrawString($title, $titleFont, $whiteBrush, [System.Drawing.RectangleF]::new(88, 270, 540, 140))
    $graphics.DrawString($subtitle, $subtitleFont, $offWhiteBrush, [System.Drawing.RectangleF]::new(88, 430, 560, 180))

    $frameX = 860
    $frameY = 122
    $frameWidth = 920
    $frameHeight = 956
    $framePath = Get-RoundedRectanglePath -x $frameX -y $frameY -width $frameWidth -height $frameHeight -radius 42
    $shadowPath = Get-RoundedRectanglePath -x ($frameX + 14) -y ($frameY + 18) -width $frameWidth -height $frameHeight -radius 42
    $shadowBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(46, 10, 25, 40))
    $frameBrush = [System.Drawing.SolidBrush]::new($navy)
    $graphics.FillPath($shadowBrush, $shadowPath)
    $graphics.FillPath($frameBrush, $framePath)
    $shadowBrush.Dispose()
    $frameBrush.Dispose()
    $shadowPath.Dispose()

    $screenInset = 22
    $screenX = $frameX + $screenInset
    $screenY = $frameY + $screenInset
    $screenWidth = $frameWidth - ($screenInset * 2)
    $screenHeight = $frameHeight - ($screenInset * 2)
    $screenPath = Get-RoundedRectanglePath -x $screenX -y $screenY -width $screenWidth -height $screenHeight -radius 24

    $previousClip = $graphics.Clip
    $graphics.SetClip($screenPath)
    $graphics.DrawImage($image, [System.Drawing.RectangleF]::new($screenX, $screenY, $screenWidth, $screenHeight))
    $graphics.Clip = $previousClip

    $cameraBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(220, 8, 20, 32))
    $graphics.FillEllipse($cameraBrush, ($frameX + 10), ($frameY + ($frameHeight / 2) - 10), 20, 20)
    $cameraBrush.Dispose()

    $homeBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(170, 220, 230, 240))
    $graphics.FillEllipse($homeBrush, ($frameX + $frameWidth - 22), ($frameY + ($frameHeight / 2) - 24), 8, 48)
    $homeBrush.Dispose()

    $brandFont.Dispose()
    $titleFont.Dispose()
    $subtitleFont.Dispose()
    $whiteBrush.Dispose()
    $offWhiteBrush.Dispose()
    $framePath.Dispose()
    $screenPath.Dispose()
}

function Save-Tablet10Screenshot([string]$path, [string]$sourcePath, [string]$title, [string]$subtitle) {
    $canvas = New-Object System.Drawing.Bitmap(1920, 1200, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    $source = [System.Drawing.Image]::FromFile($sourcePath)
    DrawTablet10Screenshot -graphics $graphics -image $source -title $title -subtitle $subtitle
    $source.Dispose()
    $graphics.Dispose()
    $canvas.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $canvas.Dispose()
}

function Save-FeatureGraphic([string]$path, [string]$iconPath, [string]$welcomePath, [string]$disclaimerPath) {
    $width = 1024
    $height = 500
    $canvas = New-Object System.Drawing.Bitmap($width, $height, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $backgroundBrush = [System.Drawing.SolidBrush]::new($navy)
    $graphics.FillRectangle($backgroundBrush, 0, 0, $width, $height)
    $backgroundBrush.Dispose()

    $leftGlow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(28, $softTeal))
    $graphics.FillEllipse($leftGlow, -120, -80, 360, 360)
    $leftGlow.Dispose()

    $rightGlow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(30, $blue))
    $graphics.FillEllipse($rightGlow, 710, 120, 360, 280)
    $rightGlow.Dispose()

    $diagonalBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(42, $teal))
    $graphics.FillPolygon($diagonalBrush, @(
        [System.Drawing.Point]::new(420, -20),
        [System.Drawing.Point]::new(610, -20),
        [System.Drawing.Point]::new(350, 520),
        [System.Drawing.Point]::new(160, 520)
    ))
    $diagonalBrush.Dispose()

    $icon = [System.Drawing.Image]::FromFile($iconPath)
    $graphics.DrawImage($icon, 66, 70, 122, 122)

    $titleFont = New-Object System.Drawing.Font("Segoe UI", 40, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $bodyFont = New-Object System.Drawing.Font("Segoe UI", 19, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $pillFont = New-Object System.Drawing.Font("Segoe UI", 17, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $smallFont = New-Object System.Drawing.Font("Segoe UI", 18, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)

    $whiteBrush = [System.Drawing.SolidBrush]::new($white)
    $offWhiteBrush = [System.Drawing.SolidBrush]::new($offWhite)
    $tealBrush = [System.Drawing.SolidBrush]::new($teal)

    $graphics.DrawString("FitOver40", $titleFont, $whiteBrush, 210, 76)
    $graphics.DrawString("Strength and interval running for the 40+ athlete", $bodyFont, $offWhiteBrush, [System.Drawing.RectangleF]::new(70, 208, 360, 80))

    $pillPath = Get-RoundedRectanglePath -x 70 -y 310 -width 176 -height 42 -radius 20
    $graphics.FillPath($tealBrush, $pillPath)
    $graphics.DrawString("Private. Offline. No ads.", $pillFont, $whiteBrush, [System.Drawing.RectangleF]::new(92, 319, 150, 24))
    $pillPath.Dispose()

    $graphics.DrawString("Joint-friendly tracking and training", $smallFont, $offWhiteBrush, 70, 380)

    $welcome = [System.Drawing.Image]::FromFile($welcomePath)
    $disclaimer = [System.Drawing.Image]::FromFile($disclaimerPath)
    DrawPhonePreview -graphics $graphics -image $welcome -x 570 -y 48 -width 176 -height 368
    DrawPhonePreview -graphics $graphics -image $disclaimer -x 772 -y 82 -width 176 -height 368

    $icon.Dispose()
    $welcome.Dispose()
    $disclaimer.Dispose()
    $titleFont.Dispose()
    $bodyFont.Dispose()
    $pillFont.Dispose()
    $smallFont.Dispose()
    $whiteBrush.Dispose()
    $offWhiteBrush.Dispose()
    $tealBrush.Dispose()
    $graphics.Dispose()
    $canvas.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $canvas.Dispose()
}

Ensure-Directory $iconRoot
Ensure-Directory $featureRoot
Ensure-Directory $phoneScreenshotRoot
Ensure-Directory $tabletScreenshotRoot
Ensure-Directory $tablet10ScreenshotRoot

foreach ($density in $densities) {
    $folder = Join-Path $resRoot $density.Folder
    Ensure-Directory $folder
    Save-SquareIcon -path (Join-Path $folder "ic_launcher.png") -size $density.Size
    Save-RoundIcon -path (Join-Path $folder "ic_launcher_round.png") -size $density.Size
}

Save-SquareIcon -path (Join-Path $iconRoot "fitover40-playstore-icon-512.png") -size 512

Copy-Item -LiteralPath (Join-Path $repoRoot "app_discovery_result.png") -Destination (Join-Path $phoneScreenshotRoot "01-welcome.png") -Force
Copy-Item -LiteralPath (Join-Path $repoRoot "app_after_click.png") -Destination (Join-Path $phoneScreenshotRoot "02-health-disclaimer.png") -Force

Save-TabletScreenshot `
    -path (Join-Path $tabletScreenshotRoot "01-welcome-tablet.png") `
    -sourcePath (Join-Path $phoneScreenshotRoot "01-welcome.png") `
    -title "Built for training after 40" `
    -subtitle "Interval running and strength plans with a simple, private workflow."

Save-TabletScreenshot `
    -path (Join-Path $tabletScreenshotRoot "02-health-disclaimer-tablet.png") `
    -sourcePath (Join-Path $phoneScreenshotRoot "02-health-disclaimer.png") `
    -title "Clear guidance before you start" `
    -subtitle "Onboarding keeps health and safety front and center from the first launch."

Save-Tablet10Screenshot `
    -path (Join-Path $tablet10ScreenshotRoot "01-welcome-tablet-10in.png") `
    -sourcePath (Join-Path $phoneScreenshotRoot "01-welcome.png") `
    -title "Training that fits the 40+ athlete" `
    -subtitle "Simple interval running and strength tracking with no account, no ads, and no clutter."

Save-Tablet10Screenshot `
    -path (Join-Path $tablet10ScreenshotRoot "02-health-disclaimer-tablet-10in.png") `
    -sourcePath (Join-Path $phoneScreenshotRoot "02-health-disclaimer.png") `
    -title "Start with safety and clarity" `
    -subtitle "Health guidance is built into onboarding so the first launch sets the right expectations."

Save-FeatureGraphic `
    -path (Join-Path $featureRoot "fitover40-feature-graphic-1024x500.png") `
    -iconPath (Join-Path $iconRoot "fitover40-playstore-icon-512.png") `
    -welcomePath (Join-Path $phoneScreenshotRoot "01-welcome.png") `
    -disclaimerPath (Join-Path $phoneScreenshotRoot "02-health-disclaimer.png")
