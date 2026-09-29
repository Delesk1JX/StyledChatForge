param(
    [Parameter(Mandatory = $true)][string]$Root,
    [Parameter(Mandatory = $true)][string]$From,
    [Parameter(Mandatory = $true)][string]$To
)

# Rewrites the fully qualified package prefix everywhere it appears as a Java identifier.
# Only `package` and `import` lines are touched, so a stray occurrence inside a string literal or
# a comment cannot be silently mangled; anything else is reported instead.

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$changed = 0
$decls = 0
$imports = 0
$unexpected = @()

Get-ChildItem $Root -Recurse -Filter *.java | ForEach-Object {
    $path = $_.FullName
    $text = [System.IO.File]::ReadAllText($path)
    $out = New-Object System.Text.StringBuilder
    $hit = $false

    foreach ($line in ($text -split "`r?`n")) {
        if ($line -match ('^\s*(package|import)\s+(static\s+)?' + [regex]::Escape($From) + '([.;])')) {
            $new = $line -replace ('(?<=package\s)' + [regex]::Escape($From)), $To
            $new = $new -replace ('(?<=import\s(?:static\s)?)' + [regex]::Escape($From)), $To
            if ($line -match '^\s*package\s') { $decls++ } else { $imports++ }
            [void]$out.AppendLine($new)
            $hit = $true
        } else {
            if ($line.Contains($From) -and $line -notmatch '^\s*\*' -and $line -notmatch '^\s*//') {
                $unexpected += "$($_.Name):$($out.ToString().Split("`n").Count)  $($line.Trim())"
            }
            [void]$out.AppendLine($line)
        }
    }

    if ($hit) {
        $newText = $out.ToString().TrimEnd("`r", "`n") + "`r`n"
        [System.IO.File]::WriteAllText($path, $newText, $utf8NoBom)
        $script:changed++
    }
}

"files rewritten      : $changed"
"package declarations : $decls"
"import statements    : $imports"
"non-declaration hits : $($unexpected.Count)"
$unexpected | ForEach-Object { "   ! $_" }
