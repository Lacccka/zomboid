param(
    [string]$RepoRoot,
    [string]$ZomboidHome,
    [string]$Destination,
    [string]$ServerIni,
    [switch]$SkipServerIni,
    [switch]$NoAttackPoC
)

$ErrorActionPreference = "Stop"

$WorkshopId = "3268487204"
$ModId = "Bandits2"
$RequiredModFolders = "mods,workshop,steam"
$AttackPoCMarker = "upstream-coordinate-pursuit-v2"

if (-not $RepoRoot) {
    $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
} else {
    $RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
}

if (-not $ZomboidHome) {
    $ZomboidHome = Join-Path $env:USERPROFILE "Zomboid"
}

if (-not $Destination) {
    $Destination = Join-Path $ZomboidHome "mods\Bandits-LCC-Dev"
}

if (-not $ServerIni) {
    $ServerIni = Join-Path $ZomboidHome "Server\servertest.ini"
}

$ReadySource = Join-Path $RepoRoot "WorkshopPatches\Bandits-LCC-Dev"
$CleanSource = Join-Path $RepoRoot "3268487204\mods\Bandits"
$Source = if ($NoAttackPoC) { $CleanSource } else { $ReadySource }
$SourceModInfo = Join-Path $Source "42.20\mod.info"
$DestinationModInfo = Join-Path $Destination "42.20\mod.info"
$DestinationBanditUpdate = Join-Path $Destination "42.20\media\lua\client\BanditUpdate.lua"

function Assert-BanditsModInfo([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Bandits mod.info not found: $Path"
    }

    $text = [System.IO.File]::ReadAllText($Path)
    if ($text -notmatch '(?m)^id=Bandits2\s*$') {
        throw "Expected id=Bandits2 in $Path"
    }
}

function Find-OtherBanditsCopies([string]$Root, [string]$ExcludedRoot) {
    if (-not (Test-Path -LiteralPath $Root -PathType Container)) {
        return @()
    }

    $excluded = [System.IO.Path]::GetFullPath($ExcludedRoot).TrimEnd('\') + '\'
    $found = @()
    $files = @(Get-ChildItem -LiteralPath $Root -Filter "mod.info" -File -Recurse -ErrorAction SilentlyContinue)

    foreach ($file in $files) {
        $full = [System.IO.Path]::GetFullPath($file.FullName)
        if ($full.StartsWith($excluded, [System.StringComparison]::OrdinalIgnoreCase)) {
            continue
        }

        try {
            $text = [System.IO.File]::ReadAllText($full)
            if ($text -match '(?m)^id=Bandits2\s*$') {
                $found += $full
            }
        } catch {
            # Ignore unreadable unrelated mod metadata during duplicate scan.
        }
    }

    return $found
}

function Update-ServerIni([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Server ini not found: $Path. Pass -SkipServerIni only if you intentionally manage it elsewhere."
    }

    $lines = [System.IO.File]::ReadAllLines($Path)
    $foundWorkshop = $false
    $foundMods = $false
    $changed = $false

    for ($i = 0; $i -lt $lines.Length; $i++) {
        if ($lines[$i].StartsWith("WorkshopItems=", [System.StringComparison]::Ordinal)) {
            $foundWorkshop = $true
            $raw = $lines[$i].Substring("WorkshopItems=".Length)
            $items = @($raw.Split(';') | Where-Object { $_ -and $_ -ne $WorkshopId })
            $newLine = "WorkshopItems=" + [string]::Join(';', $items)
            if ($newLine -ne $lines[$i]) {
                $lines[$i] = $newLine
                $changed = $true
            }
        }

        if ($lines[$i].StartsWith("Mods=", [System.StringComparison]::Ordinal)) {
            $foundMods = $true
            $raw = $lines[$i].Substring("Mods=".Length)
            $mods = @($raw.Split(';') | Where-Object { $_ })
            if ($mods -notcontains $ModId) {
                throw "Server ini does not contain Mods=...;$ModId;... . Refusing to guess Bandits load order."
            }
        }
    }

    if (-not $foundWorkshop) {
        throw "WorkshopItems= line not found in $Path"
    }
    if (-not $foundMods) {
        throw "Mods= line not found in $Path"
    }

    if ($changed) {
        $backup = "$Path.lcc-local-bandits.bak"
        if (-not (Test-Path -LiteralPath $backup)) {
            Copy-Item -LiteralPath $Path -Destination $backup
            Write-Host "Created server ini backup: $backup"
        }
        [System.IO.File]::WriteAllLines($Path, $lines, [System.Text.UTF8Encoding]::new($false))
        Write-Host "Removed Workshop item $WorkshopId from: $Path"
    } else {
        Write-Host "Server ini already excludes Workshop item ${WorkshopId}: $Path"
    }
}

if (-not (Test-Path -LiteralPath $Source -PathType Container)) {
    throw "Repository Bandits source not found: $Source"
}
Assert-BanditsModInfo $SourceModInfo

$destinationParent = Split-Path -Parent $Destination
New-Item -ItemType Directory -Force -Path $destinationParent | Out-Null
New-Item -ItemType Directory -Force -Path $Destination | Out-Null

Write-Host "Mirroring repository Bandits into local dev mod:"
Write-Host "  source:      $Source"
Write-Host "  destination: $Destination"
if ($NoAttackPoC) {
    Write-Host "  variant:     clean repository upstream snapshot"
} else {
    Write-Host "  variant:     ready working copy ($AttackPoCMarker)"
}

& robocopy $Source $Destination /MIR /R:1 /W:1 /NFL /NDL /NJH /NJS /NP | Out-Host
$robocopyCode = $LASTEXITCODE
if ($robocopyCode -ge 8) {
    throw "robocopy failed with exit code $robocopyCode"
}

Assert-BanditsModInfo $DestinationModInfo
if (-not (Test-Path -LiteralPath $DestinationBanditUpdate -PathType Leaf)) {
    throw "Local BanditUpdate.lua not found after mirror: $DestinationBanditUpdate"
}

# The repository working copy is already materialized. Do not patch the destination
# again here; that previously allowed the local copy to drift from the audited repo state.
if (-not $NoAttackPoC) {
    $preparedText = [System.IO.File]::ReadAllText($DestinationBanditUpdate)
    if ($preparedText -notmatch [regex]::Escape("LCC_BANDITS_ATTACK_BRIDGE_POC = `"$AttackPoCMarker`"")) {
        throw "Ready Bandits working copy does not contain expected marker $AttackPoCMarker"
    }
}

if (-not $SkipServerIni) {
    Update-ServerIni $ServerIni
} else {
    Write-Warning "Server ini was not changed. Ensure WorkshopItems does NOT contain $WorkshopId and Mods still contains $ModId."
}

$otherCopies = @()
$otherCopies += Find-OtherBanditsCopies (Join-Path $ZomboidHome "mods") $Destination
$otherCopies += Find-OtherBanditsCopies (Join-Path $ZomboidHome "Workshop") $Destination
$otherCopies = @($otherCopies | Sort-Object -Unique)

if ($otherCopies.Count -gt 0) {
    Write-Warning "Other local copies with id=$ModId were found. Remove/rename them before testing:"
    $otherCopies | ForEach-Object { Write-Warning "  $_" }
    throw "Local Bandits source is ambiguous because another id=$ModId copy exists under Zomboid\mods or Zomboid\Workshop."
}

$marker = Join-Path $Destination ".lcc-local-bandits-dev"
$markerText = @(
    "source=$Source",
    "modId=$ModId",
    "workshopId=$WorkshopId",
    "requiredModFolders=$RequiredModFolders",
    "attackPoC=" + $(if ($NoAttackPoC) { "none" } else { $AttackPoCMarker }),
    "generated=" + [DateTime]::Now.ToString("o")
)
[System.IO.File]::WriteAllLines($marker, $markerText, [System.Text.UTF8Encoding]::new($false))

Write-Host ""
Write-Host "Local Bandits dev setup is ready."
Write-Host "Bandits2 path: $Destination"
Write-Host ""
Write-Host "IMPORTANT: launch BOTH client and dedicated server with:"
Write-Host "  -modfolders $RequiredModFolders"
Write-Host ""
if ($NoAttackPoC) {
    Write-Host "Clean upstream snapshot was copied; no LCC Attack PoC marker is expected."
} else {
    Write-Host "Runtime proof for the current Attack PoC:"
    Write-Host "  [LCC][BanditsAttackPoC][INIT] upstream-coordinate-pursuit-v2 active"
    Write-Host "If that line is absent, the game did not load the prepared BanditUpdate.lua."
}
