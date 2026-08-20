param(
    [string]$RepoRoot,
    [string]$TargetFile,
    [switch]$Revert
)

$ErrorActionPreference = "Stop"

$workingCopyRelativePath = "WorkshopPatches/Bandits-LCC-Dev/42.20/media/lua/client/BanditUpdate.lua"
$PoCMarker = "upstream-coordinate-pursuit-v2"

if ($TargetFile) {
    $targetPath = (Resolve-Path -LiteralPath $TargetFile).Path
} else {
    if (-not $RepoRoot) {
        $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
    }
    $targetPath = Join-Path $RepoRoot $workingCopyRelativePath
}

if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf)) {
    throw "BanditUpdate.lua not found: $targetPath"
}

$content = [System.IO.File]::ReadAllText($targetPath)
$newline = if ($content.Contains("`r`n")) { "`r`n" } else { "`n" }

$headerOriginalLines = @(
    'require "BanditZombie"',
    '',
    'local sum1 = 0'
)

$headerPocLines = @(
    'require "BanditZombie"',
    '',
    '-- [LCC POC] Local B42.20.3 reference implementation; this working copy is for controlled testing.',
    '-- v2 removes every active pathToCharacter(bandit) call from zombie -> Bandit pursuit.',
    'LCC_BANDITS_ATTACK_BRIDGE_POC = "upstream-coordinate-pursuit-v2"',
    'print("[LCC][BanditsAttackPoC][INIT] upstream-coordinate-pursuit-v2 active; character pursuit and vanilla target bridge disabled")',
    '',
    'local sum1 = 0'
)

$helperOriginalLines = @(
    '-- table of bandits being attacked by zombies',
    'local biteTab = {}',
    '',
    '-- manages zombie behavior towards bandits'
)

$helperPocLines = @(
    '-- table of bandits being attacked by zombies',
    'local biteTab = {}',
    '',
    '-- Coordinate-only pursuit for normal zombies chasing Bandits.',
    '-- Passing the Bandit IsoZombie to pathToCharacter() can create/retain a Java/network',
    '-- goal-character relationship. Keep the destination as x/y/z only so the custom',
    '-- Bite/BiteLow pipeline can operate without constructing a character target.',
    'local function PathZombieToBanditLocation(zombie, banditCached)',
    '    if not zombie or not banditCached then return end',
    '    if BanditUtils.IsController(zombie) then',
    '        zombie:pathToLocationF(banditCached.x, banditCached.y, banditCached.z)',
    '    end',
    'end',
    '',
    '-- manages zombie behavior towards bandits'
)

$farOriginalLines = @(
    '            if zombie:CanSee(bandit) then',
    '                zombie:pathToCharacter(bandit)',
    '            end'
)

$farPocLines = @(
    '            if zombie:CanSee(bandit) then',
    '                PathZombieToBanditLocation(zombie, banditCached)',
    '            end'
)

$bridgeOriginalLines = @(
    '                    if zombie and bandit then',
    '                        zombie:spotted(bandit, true)',
    '                        zombie:addAggro(bandit, 1)',
    '                        zombie:setTarget(bandit)',
    '                        zombie:setAttackedBy(bandit)',
    '                    ',
    '                        --[[',
    '                        zombie:spotted(bandit, true)',
    '                        zombie:setTarget(bandit)',
    '                        zombie:setAttackedBy(bandit)',
    '                        ]]',
    '                    end'
)

$bridgePocLines = @(
    '                    if zombie and bandit then',
    '                        -- [LCC POC v2] Keep the final approach coordinate-only as well.',
    '                        -- Do not pass the Bandit IsoZombie into character-pathing APIs.',
    '                        PathZombieToBanditLocation(zombie, banditCached)',
    '                    end'
)

$headerOriginal = [string]::Join($newline, $headerOriginalLines)
$headerPoc = [string]::Join($newline, $headerPocLines)
$helperOriginal = [string]::Join($newline, $helperOriginalLines)
$helperPoc = [string]::Join($newline, $helperPocLines)
$farOriginal = [string]::Join($newline, $farOriginalLines)
$farPoc = [string]::Join($newline, $farPocLines)
$bridgeOriginal = [string]::Join($newline, $bridgeOriginalLines)
$bridgePoc = [string]::Join($newline, $bridgePocLines)

function Count-Occurrences([string]$Text, [string]$Needle) {
    if ([string]::IsNullOrEmpty($Needle)) { return 0 }
    $count = 0
    $offset = 0
    while (($index = $Text.IndexOf($Needle, $offset, [System.StringComparison]::Ordinal)) -ge 0) {
        $count++
        $offset = $index + $Needle.Length
    }
    return $count
}

$originalCounts = @(
    (Count-Occurrences $content $headerOriginal),
    (Count-Occurrences $content $helperOriginal),
    (Count-Occurrences $content $farOriginal),
    (Count-Occurrences $content $bridgeOriginal)
)
$pocCounts = @(
    (Count-Occurrences $content $headerPoc),
    (Count-Occurrences $content $helperPoc),
    (Count-Occurrences $content $farPoc),
    (Count-Occurrences $content $bridgePoc)
)

$isOriginal = ($originalCounts | Where-Object { $_ -ne 1 }).Count -eq 0 -and ($pocCounts | Where-Object { $_ -ne 0 }).Count -eq 0
$isPoc = ($originalCounts | Where-Object { $_ -ne 0 }).Count -eq 0 -and ($pocCounts | Where-Object { $_ -ne 1 }).Count -eq 0

if ($Revert) {
    if ($isOriginal) {
        Write-Host "Bandits AttackState PoC is already reverted: $targetPath"
        exit 0
    }
    if (-not $isPoc) {
        throw "Refusing to revert: BanditUpdate.lua is neither the audited original nor complete $PoCMarker state."
    }

    $updated = $content.Replace($headerPoc, $headerOriginal)
    $updated = $updated.Replace($helperPoc, $helperOriginal)
    $updated = $updated.Replace($farPoc, $farOriginal)
    $updated = $updated.Replace($bridgePoc, $bridgeOriginal)
    [System.IO.File]::WriteAllText($targetPath, $updated, [System.Text.UTF8Encoding]::new($false))
    Write-Host "Reverted Bandits AttackState PoC $PoCMarker: $targetPath"
    exit 0
}

if ($isPoc) {
    Write-Host "Bandits AttackState PoC $PoCMarker is already applied: $targetPath"
    exit 0
}
if (-not $isOriginal) {
    throw "Refusing to apply: BanditUpdate.lua is neither the audited original nor complete $PoCMarker state. Upstream may have changed."
}

$updated = $content.Replace($headerOriginal, $headerPoc)
$updated = $updated.Replace($helperOriginal, $helperPoc)
$updated = $updated.Replace($farOriginal, $farPoc)
$updated = $updated.Replace($bridgeOriginal, $bridgePoc)
[System.IO.File]::WriteAllText($targetPath, $updated, [System.Text.UTF8Encoding]::new($false))

Write-Host "Applied Bandits AttackState upstream PoC $PoCMarker: $targetPath"
Write-Host "All active zombie -> Bandit pursuit in UpdateZombies now uses pathToLocationF(x,y,z)."
Write-Host "The vanilla spotted/addAggro/setTarget/setAttackedBy bridge remains disabled."
Write-Host "NPCCombatExperimental will run observation-only while marker $PoCMarker is active."
if (-not $TargetFile) {
    Write-Host "Working copy is ready for manual transfer from:"
    Write-Host "  $targetPath"
}
Write-Host "Use -Revert with the same arguments to restore the exact audited original blocks."
