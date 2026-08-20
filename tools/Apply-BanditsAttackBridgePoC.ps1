param(
    [string]$RepoRoot,
    [string]$TargetFile,
    [switch]$Revert
)

$ErrorActionPreference = "Stop"

$workingCopyRelativePath = "WorkshopPatches/NPCCombatExperimental/DevUpstream/Bandits/42.20/media/lua/client/BanditUpdate.lua"

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
    '-- [LCC POC] Local B42.20.3 reference implementation; never ship this upstream file in the Workshop patch.',
    'LCC_BANDITS_ATTACK_BRIDGE_POC = "upstream-pursuit-v1"',
    'print("[LCC][BanditsAttackPoC][INIT] upstream-pursuit-v1 active; vanilla spotted/addAggro/setTarget/setAttackedBy bridge disabled")',
    '',
    'local sum1 = 0'
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
    '                        -- [LCC POC] Keep Bandits'' own pursuit / Bite pipeline without',
    '                        -- constructing a vanilla zombie -> Bandit combat relationship.',
    '                        zombie:pathToCharacter(bandit)',
    '                    end'
)

$headerOriginal = [string]::Join($newline, $headerOriginalLines)
$headerPoc = [string]::Join($newline, $headerPocLines)
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

$headerOriginalCount = Count-Occurrences $content $headerOriginal
$headerPocCount = Count-Occurrences $content $headerPoc
$bridgeOriginalCount = Count-Occurrences $content $bridgeOriginal
$bridgePocCount = Count-Occurrences $content $bridgePoc

$isOriginal = $headerOriginalCount -eq 1 -and $headerPocCount -eq 0 -and $bridgeOriginalCount -eq 1 -and $bridgePocCount -eq 0
$isPoc = $headerOriginalCount -eq 0 -and $headerPocCount -eq 1 -and $bridgeOriginalCount -eq 0 -and $bridgePocCount -eq 1

if ($Revert) {
    if ($isOriginal) {
        Write-Host "Bandits AttackState PoC is already reverted: $targetPath"
        exit 0
    }
    if (-not $isPoc) {
        throw "Refusing to revert: BanditUpdate.lua is neither the audited original nor the complete LCC PoC state."
    }

    $updated = $content.Replace($headerPoc, $headerOriginal).Replace($bridgePoc, $bridgeOriginal)
    [System.IO.File]::WriteAllText($targetPath, $updated, [System.Text.UTF8Encoding]::new($false))
    Write-Host "Reverted Bandits AttackState PoC: $targetPath"
    exit 0
}

if ($isPoc) {
    Write-Host "Bandits AttackState PoC is already applied: $targetPath"
    exit 0
}
if (-not $isOriginal) {
    throw "Refusing to apply: BanditUpdate.lua is neither the audited original nor the complete LCC PoC state. Upstream may have changed."
}

$updated = $content.Replace($headerOriginal, $headerPoc).Replace($bridgeOriginal, $bridgePoc)
[System.IO.File]::WriteAllText($targetPath, $updated, [System.Text.UTF8Encoding]::new($false))

Write-Host "Applied Bandits AttackState upstream PoC: $targetPath"
Write-Host "The PoC marker is active from BanditUpdate.lua load time."
Write-Host "The four-call vanilla bridge is replaced by pathToCharacter(bandit)."
Write-Host "NPCCombatExperimental will run observation-only while marker upstream-pursuit-v1 is active."
if (-not $TargetFile) {
    Write-Host "Working copy is ready for manual transfer from:"
    Write-Host "  $targetPath"
}
Write-Host "Use -Revert with the same arguments to restore the exact original header and bridge blocks."
