$ErrorActionPreference = 'Continue'
$jar = "$env:USERPROFILE\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10_mapped_official_1.20.1\forge-1.20.1-47.4.10_mapped_official_1.20.1.jar"
$javap = "C:\Program Files\Java\jdk-21.0.11\bin\javap.exe"
$root = "C:\Users\Alex\Downloads\MODDING\StyledChatForge\src\main\java"

$cache = @{}
$problems = @()
$checked = 0
$skipped = @()

function Get-Members([string]$fqn) {
    if ($cache.ContainsKey($fqn)) { return $cache[$fqn] }
    $out = (& $javap -p -cp $jar $fqn 2>&1 | Out-String)
    if ($out -match 'class not found' -or $out -match 'Error:') {
        # nested class: swap the last package separator for a binary-name separator
        $last = $fqn.LastIndexOf('.')
        if ($last -gt 0) {
            $nested = $fqn.Substring(0, $last) + '$' + $fqn.Substring($last + 1)
            $out = (& $javap -p -cp $jar $nested 2>&1 | Out-String)
            $fqn = $nested
        }
    }
    $cache[$fqn] = $out
    return $out
}

Get-ChildItem $root -Recurse -Filter "*Mixin.java" | ForEach-Object {
    $file = $_.Name
    $text = [System.IO.File]::ReadAllText($_.FullName)

    # simple name -> FQN from imports
    $imports = @{}
    foreach ($m in [regex]::Matches($text, '(?m)^import\s+([A-Za-z0-9_.$]+);')) {
        $fqn = $m.Groups[1].Value
        if ($fqn.EndsWith('.*')) { continue }
        $imports[($fqn -split '\.')[-1]] = $fqn
    }

    $targets = @()
    foreach ($m in [regex]::Matches($text, '@Mixin\s*\(\s*(?:value\s*=\s*)?\{?\s*([A-Za-z0-9_.]+)\.class')) { $targets += $m.Groups[1].Value }
    foreach ($m in [regex]::Matches($text, '@Mixin\s*\(\s*([A-Za-z0-9_.]+)\.class')) { $targets += $m.Groups[1].Value }
    $targets = $targets | Select-Object -Unique
    if ($targets.Count -eq 0) { return }

    $resolved = @()
    foreach ($t in $targets) {
        if ($t.Contains('.')) { $resolved += $t }
        elseif ($imports.ContainsKey($t)) { $resolved += $imports[$t] }
        else { $resolved += $t; $skipped += "$file : unresolved simple name '$t'" }
    }

    foreach ($mm in [regex]::Matches($text, 'method\s*=\s*\{([^}]*)\}')) {
        foreach ($nm in [regex]::Matches($mm.Groups[1].Value, '"([^"]+)"')) {
            $decl = $nm.Groups[1].Value
            $bare = ($decl -split '\(')[0]
            $hasDesc = $decl.Contains('(')
            if ($bare -eq '<init>') { continue }

            foreach ($t in $resolved) {
                $members = Get-Members $t
                if ($members -match 'class not found|Error:') { $skipped += "$file : could not load $t"; continue }
                $checked++
                if ($members -notmatch "\s$([regex]::Escape($bare))\(") {
                    $problems += [pscustomobject]@{ File = $file; Target = $t; Decl = $decl }
                }
            }
        }
    }
}

"checked method targets: $checked"
"PROBLEMS: $($problems.Count)"
$problems | Format-Table -AutoSize
if ($skipped.Count -gt 0) { "SKIPPED:"; $skipped | Select-Object -Unique }
