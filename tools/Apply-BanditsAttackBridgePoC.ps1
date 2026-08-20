param(
    [string]$RepoRoot,
    [switch]$Revert
)

$ErrorActionPreference = "Stop"

if (-not $RepoRoot) {
    $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

$relativePath = "3268487204/mods/Bandits/42.20/media/lua/client/BanditUpdate.lua"
$targetPath = Join-Path $RepoRoot $relativePath

if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf)) {
    throw "BanditUpdate.lua not found: $targetPath"
}

$content = [System.IO.File]::ReadAllText($targetPath)
$newline = if ($content.Contains("`r`n")) { "`r`n" } else { "`n" }

$originalLines = @(
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

$pocLines = @(
    '                    if zombie and bandit then',
    '                        -- [LCC POC] B42.20.3: keep Bandits'' own pursuit / Bite pipeline',
    '                        -- without constructing a vanilla zombie -> Bandit combat target.',
    '                        -- The Bandit is still an IsoZombie, so vanilla AttackState must not',
    '                        -- be allowed to treat it as the player-like target used by this mod.',
    '                        if not LCC_BANDITS_ATTACK_BRIDGE_POC then',
    '                            LCC_BANDITS_ATTACK_BRIDGE_POC = "upstream-pursuit-v1"',
    '                            print("[LCC][BanditsAttackPoC][INIT] upstream-pursuit-v1 active; vanilla spotted/addAggro/setTarget/setAttackedBy bridge disabled")',
    '                        end',
    '',
    '                        zombie:pathToCharacter(bandit)',
    '                    end'
)

$original = [string]::Join($newline, $originalLines)
$poc = [string]::Join($newline, $pocLines)

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

$originalCount = Count-Occurrences $content $original
$pocCount = Count-Occurrences $content $poc

if ($Revert) {
    if ($originalCount -eq 1 -and $pocCount -eq 0) {
        Write-Host "Bandits AttackState PoC is already reverted."
        exit 0
    }
    if ($pocCount -ne 1 -or $originalCount -ne 0) {
        throw "Refusing to revert: expected exactly one LCC PoC block and no original bridge block. original=$originalCount poc=$pocCount"
    }

    $updated = $content.Replace($poc, $original)
    [System.IO.File]::WriteAllText($targetPath, $updated, [System.Text.UTF8Encoding]::new($false))
    Write-Host "Reverted Bandits AttackState PoC in $relativePath"
    exit 0
}

if ($pocCount -eq 1 -and $originalCount -eq 0) {
    Write-Host "Bandits AttackState PoC is already applied."
    exit 0
}
if ($originalCount -ne 1 -or $pocCount -ne 0) {
    throw "Refusing to apply: upstream block does not match the audited B42.20 snapshot. original=$originalCount poc=$pocCount"
}

$updated = $content.Replace($original, $poc)
[System.IO.File]::WriteAllText($targetPath, $updated, [System.Text.UTF8Encoding]::new($false))

Write-Host "Applied Bandits AttackState upstream PoC to $relativePath"
Write-Host "The four-call vanilla bridge is replaced by pathToCharacter(bandit)."
Write-Host "NPCCombatExperimental will detect marker upstream-pursuit-v1 and disable its v3 setTarget(nil) intervention for an uncontaminated test."
Write-Host "Use -Revert to restore the exact original bridge block."
