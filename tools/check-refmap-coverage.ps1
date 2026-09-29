param(
    [string]$Jar = "",
    [string]$SrgJar = "$env:USERPROFILE\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-srg.jar"
)

# Reports mixin method targets that have no entry in the refmap.
#
# The Mixin annotation processor only emits refmap entries for members present in the
# official -> SRG table. Synthetic lambda methods are not in that table, so their targets resolve in a
# development environment and then fail on a production server with "Critical injection failure ...
# could not find any targets". Those have to be listed by hand in syntheticTargets in build.gradle.
#
# Run this after changing the Minecraft version or adding a mixin target.

$ErrorActionPreference = 'Continue'
$root = Join-Path (Split-Path -Parent $PSScriptRoot) 'src\main\java'
$javap = 'C:\Program Files\Java\jdk-21.0.11\bin\javap.exe'

if ([string]::IsNullOrWhiteSpace($Jar)) {
    $libs = Join-Path (Split-Path -Parent $PSScriptRoot) 'build\libs'
    $found = Get-ChildItem $libs -Filter '*.jar' -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|javadoc' } |
        Select-Object -First 1
    if ($found) { $Jar = $found.FullName }
}

if (-not $Jar -or -not (Test-Path $Jar)) {
    "Release jar not found; run .\gradlew.bat build first."
    exit 1
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($Jar)
try {
    $entry = $zip.Entries | Where-Object { $_.FullName -like '*.refmap.json' } | Select-Object -First 1
    if (-not $entry) { "No refmap in $Jar"; exit 1 }
    $reader = New-Object System.IO.StreamReader($entry.Open())
    $refmap = $reader.ReadToEnd() | ConvertFrom-Json
    $reader.Close()
} finally {
    $zip.Dispose()
}

$missing = @()

Get-ChildItem $root -Recurse -Filter *.java | ForEach-Object {
    $text = [System.IO.File]::ReadAllText($_.FullName)
    # src/main/java/eu/pb4/...Mixin.java -> eu/pb4/...Mixin, which is how the refmap keys classes
    $mixinName = ($_.FullName -replace '\\', '/') -replace '^.*/src/main/java/', '' -replace '\.java$', ''

    $targets = @()
    foreach ($m in [regex]::Matches($text, '@Mixin\s*\(\s*(?:value\s*=\s*)?\{?\s*([A-Za-z0-9_.]+)\.class')) { $targets += $m.Groups[1].Value }
    if ($targets.Count -eq 0) { return }

    $imports = @{}
    foreach ($m in [regex]::Matches($text, '(?m)^import\s+([A-Za-z0-9_.$]+);')) {
        $fqn = $m.Groups[1].Value
        $imports[($fqn -split '\.')[-1]] = $fqn
    }

    $section = $refmap.mappings.$mixinName

    foreach ($mm in [regex]::Matches($text, 'method\s*=\s*\{([^}]*)\}')) {
        foreach ($nm in [regex]::Matches($mm.Groups[1].Value, '"([^"]+)"')) {
            $decl = $nm.Groups[1].Value
            $bare = ($decl -split '\(')[0]
            if ($bare -eq '<init>') { continue }

            # The processor keys descriptor-qualified targets by the whole declaration, and bare
            # names by the name, so both spellings have to be considered covered.
            $keys = @()
            if ($section) { $keys = $section.PSObject.Properties.Name }
            if (($keys -contains $decl) -or ($keys -contains $bare)) { continue }

            $fqn = $null
            foreach ($t in $targets) {
                if ($t.Contains('.')) { $fqn = $t } elseif ($imports.ContainsKey($t)) { $fqn = $imports[$t] }
            }
            $missing += [pscustomobject]@{ Mixin = $mixinName; Method = $bare; Target = $fqn }
        }
    }
}

"mixin method targets without a refmap entry: $($missing.Count)"
if ($missing.Count -gt 0) {
    $missing | Format-Table -AutoSize
    ""
    "These resolve in dev and fail on a production server. Add each to syntheticTargets in"
    "build.gradle using the SRG name:"
    ""
    foreach ($t in ($missing.Target | Select-Object -Unique)) {
        if (-not $t -or -not (Test-Path $SrgJar)) { continue }
        "--- SRG members of $t ---"
        (& $javap -p -cp $SrgJar $t 2>&1 | Select-String -Pattern 'm_\d+_\(|lambda' | Select-Object -First 10) | ForEach-Object { "    " + $_.Line.Trim() }
    }
    exit 1
}

"All mixin method targets are covered by the refmap."
exit 0
