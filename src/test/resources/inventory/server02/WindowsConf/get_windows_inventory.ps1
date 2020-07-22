# Windows invetory collecting script

Param(
    [string]$log_dir
)
$log_dir = Convert-Path $log_dir


echo TestId::system
$log_path = Join-Path $log_dir "system"
Invoke-Command  -ScriptBlock { 
			Get-WmiObject -Class Win32_ComputerSystem
			 } | Out-File $log_path -Encoding UTF8


echo TestId::os_conf
$log_path = Join-Path $log_dir "os_conf"
Invoke-Command  -ScriptBlock { 
			Get-ItemProperty "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion" | 
			Format-List
			 } | Out-File $log_path -Encoding UTF8


echo TestId::system
$log_path = Join-Path $log_dir "system"
Invoke-Command  -ScriptBlock { 
			Get-WmiObject -Class Win32_ComputerSystem
			 } | Out-File $log_path -Encoding UTF8


echo TestId::os_conf
$log_path = Join-Path $log_dir "os_conf"
Invoke-Command  -ScriptBlock { 
			Get-ItemProperty "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion" |
			Format-List
			 } | Out-File $log_path -Encoding UTF8


echo TestId::os
$log_path = Join-Path $log_dir "os"
Invoke-Command  -ScriptBlock { 
		Get-WmiObject Win32_OperatingSystem |
		Format-List Caption,CSDVersion,ProductType,OSArchitecture
		 } | Out-File $log_path -Encoding UTF8


echo TestId::cpu
$log_path = Join-Path $log_dir "cpu"
Invoke-Command  -ScriptBlock { 
		Get-WmiObject -Class Win32_Processor | Format-List DeviceID, Name, MaxClockSpeed, SocketDesignation, NumberOfCores, NumberOfLogicalProcessors
		 } | Out-File $log_path -Encoding UTF8


echo TestId::memory
$log_path = Join-Path $log_dir "memory"
Invoke-Command  -ScriptBlock { 
		Get-WmiObject Win32_OperatingSystem |
		select TotalVirtualMemorySize,TotalVisibleMemorySize,
		FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles
		 } | Out-File $log_path -Encoding UTF8


echo TestId::patch_lists
$log_path = Join-Path $log_dir "patch_lists"
Invoke-Command  -ScriptBlock { 
		wmic qfe
		 } | Out-File $log_path -Encoding UTF8


echo TestId::packages
$log_path = Join-Path $log_dir "packages"
Invoke-Command  -ScriptBlock { 
		Get-WmiObject Win32_Product |
		Select-Object Name, Vendor, Version |
		Format-List
		Get-ChildItem -Path(
		'HKLM:SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall',
		'HKCU:SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall') |
		% { Get-ItemProperty $_.PsPath | Select-Object DisplayName, Publisher, DisplayVersion } |
		Format-List
		 } | Out-File $log_path -Encoding UTF8


