param(
    [string]$SrgJar = "$env:USERPROFILE\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-srg.jar",
    [string]$OfficialJar = "$env:USERPROFILE\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10_mapped_official_1.20.1\forge-1.20.1-47.4.10_mapped_official_1.20.1.jar"
)

# Verifies the synthetic mixin targets hard-coded in build.gradle against the SRG jar.
#
# The Mixin annotation processor only emits refmap entries for members present in the
# official -> SRG table, which does not include synthetic lambdas, so those are listed by hand.
# A Minecraft upgrade renumbers the lambdas and the entry goes stale, at which point the mixin
# silently fails to apply in production only. Run this after changing the Minecraft version.

$ErrorActionPreference = 'Continue'
$javap = Join-Path (Split-Path -Parent $PSCommandPath) '..\tools\.javap-path' | ForEach-Object { $null }
$javap = 'C:\Program Files\Java\jdk-21.0.11\bin\javap.exe'

if (-not (Test-Path $SrgJar)) {
    "SRG jar not found: $SrgJar"
    "Look for forge-<version>-srg.jar under %USERPROFILE%\.gradle\caches\forge_gradle\minecraft_user_repo"
    exit 1
}

$class = 'net.minecraft.server.network.ServerGamePacketListenerImpl'
$expected = 'm_244887_'

"SRG jar : $SrgJar"
"checking $class for $expected`n"

$members = (& $javap -p -cp $SrgJar $class 2>&1 | Out-String)

# The SRG member is the renamed synthetic that takes the packet plus the optional last-seen value.
$pattern = 'private\s+void\s+' + [regex]::Escape($expected) + '\(net\.minecraft\.network\.protocol\.game\.ServerboundChatPacket,\s*java\.util\.Optional\)'
if ($members -match $pattern) {
    "OK  $expected(ServerboundChatPacket, Optional) exists in the SRG jar"
    "    the build.gradle syntheticTargets entry is correct"
    exit 0
}

"NOT FOUND: $expected with the expected signature"
"The synthetic target in build.gradle is stale. Re-derive it and update the table."
""
"SRG members currently declared:"
($members -split "`r?`n" | Select-String -Pattern 'lambda|m_\d+_\(' | Select-Object -First 15) | ForEach-Object { "   " + $_.Line.Trim() }
exit 1
