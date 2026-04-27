param(
    [string]$DataFile = "data/winning-numbers.csv"
)

$ErrorActionPreference = "Stop"

$resolvedDataFile = Join-Path (Get-Location) $DataFile
$dataDirectory = Split-Path -Parent $resolvedDataFile
if (-not (Test-Path $dataDirectory)) {
    New-Item -ItemType Directory -Path $dataDirectory | Out-Null
}

$header = "drawNumber,drawDate,number1,number2,number3,number4,number5,number6,bonusNumber"
if (-not (Test-Path $resolvedDataFile)) {
    [System.IO.File]::WriteAllLines($resolvedDataFile, @($header), [System.Text.UTF8Encoding]::new($false))
}

$rows = Import-Csv -Path $resolvedDataFile
$latestDrawNumber = 0
if ($rows.Count -gt 0) {
    $latestDrawNumber = ($rows | ForEach-Object { [int]$_.drawNumber } | Measure-Object -Maximum).Maximum
}

$linesToAppend = New-Object System.Collections.Generic.List[string]
$nextDrawNumber = $latestDrawNumber + 1

while ($true) {
    $uri = "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=$nextDrawNumber"
    $result = Invoke-RestMethod -Uri $uri -Method Get -TimeoutSec 20

    if ($result.returnValue -ne "success") {
        break
    }

    $line = @(
        $result.drwNo,
        $result.drwNoDate,
        $result.drwtNo1,
        $result.drwtNo2,
        $result.drwtNo3,
        $result.drwtNo4,
        $result.drwtNo5,
        $result.drwtNo6,
        $result.bnusNo
    ) -join ","

    $linesToAppend.Add($line)
    $nextDrawNumber++

    if ($linesToAppend.Count -ge 20) {
        break
    }
}

if ($linesToAppend.Count -gt 0) {
    Add-Content -Path $resolvedDataFile -Value $linesToAppend -Encoding UTF8
    Write-Output "updated=$($linesToAppend.Count), latest=$($nextDrawNumber - 1)"
} else {
    Write-Output "updated=0, latest=$latestDrawNumber"
}
