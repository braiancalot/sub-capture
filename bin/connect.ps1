param([string]$Address = "192.168.1.5")

$ErrorActionPreference = "Stop"

# Android picks the wireless debugging port from its ephemeral range on every toggle
$firstPort = 32768
$lastPort = 60999
$portsPerBatch = 2000

function Test-PhoneConnected {
    return [bool](adb devices | Select-String "\tdevice$")
}

function Find-OpenPorts([int]$batchStart) {
    $batchEnd = [Math]::Min($batchStart + $portsPerBatch - 1, $lastPort)
    $attempts = foreach ($port in $batchStart..$batchEnd) {
        $client = [Net.Sockets.TcpClient]::new()
        [pscustomobject]@{ Port = $port; Client = $client; Task = $client.ConnectAsync($Address, $port) }
    }
    try { [Threading.Tasks.Task]::WaitAll([Threading.Tasks.Task[]]$attempts.Task, 500) | Out-Null } catch {}
    $openPorts = $attempts | Where-Object { $_.Task.Status -eq "RanToCompletion" } | ForEach-Object Port
    $attempts.Client | ForEach-Object Dispose
    return $openPorts
}

if (Test-PhoneConnected) {
    "Phone already connected."
    return
}

adb disconnect | Out-Null
"Looking for the wireless debugging port at $Address..."
for ($batchStart = $firstPort; $batchStart -le $lastPort; $batchStart += $portsPerBatch) {
    foreach ($port in Find-OpenPorts $batchStart) {
        adb connect "${Address}:$port" | Out-Null
        if (Test-PhoneConnected) {
            "Connected to ${Address}:$port."
            return
        }
    }
}
throw "No wireless debugging port answered at $Address (ports $firstPort to $lastPort). Turn wireless debugging on, or pass -Address if the phone's IP changed."
