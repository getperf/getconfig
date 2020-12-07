// Excel specification sheet path

    evidence.source = './getconfig.xlsx'

// Excel inspection result file output path

    // evidence.target='./build/check_sheet.xlsx'
    evidence.target='./build/check_sheet_<date>.xlsx'

// Test command log directory

    // evidence.staging_dir='./build/log'

// Test result directory

    // evidence.json_dir='./src/test/resources/json'

// DryRun test command log directory

    // test.dry_run_staging_dir = './src/test/resources/log/'

// HUB server agent log directory

    // test.hub_inventory_dir = './src/test/resources/hub/inventory'

// CMDB database creation script

    // test.cmdb_create_script = './lib/template/create_db.sql'

// Test command timeout [sec]

    test.timeout.Default = 30
    test.timeout.Linux   = 30
    test.timeout.Windows = 300
    test.timeout.VMWare = 300

// Test command debug mode

    // test.debug.Default = false
    // test.debug.Linux   = false
    // test.debug.Windows = false
    // test.debug.VMWare = false

// DryRun test mode

    // test.dry_run.Default = false
    // test.dry_run.Linux   = false
    // test.dry_run.Windows = false
    // test.dry_run.VMWare = false

// Account information of the server to be inspected
// 
//    The following example is an example of setting a user ID called 'Account01'. 
//    Copy each text and change the part of Account01 to the user ID to be added.

// vCenter virtual machine management REST API connection
//
//   vSphere Web Services API account information

account.VMWare.Account01.server   = '192.168.0.200'  // IP or "https://{IP}/sdk"
account.VMWare.Account01.user     = 'test_user'
account.VMWare.Account01.password = 'P@ssw0rd'

// ESXi server management REST API connection
// 
//    Same as vCenter connection information above. For a single ESXi server, 
//    specify the IP of the ESXi server

account.VMHost.Account01.server   = '192.168.0.200'  // IP or "https://{IP}/sdk"
account.VMHost.Account01.user     = 'test_user'
account.VMHost.Account01.password = 'P@ssw0rd'

// Linux server SSH connection
//
//   Linux server to be inspected SSH connection account information

account.Linux.Account01.user      = 'zabbix'
account.Linux.Account01.password  = 'P@ssw0rd'

// Windows server WinRM connection
//
//   Windows server to be inspected WinRM connection account information
//   December 2020 Currently, only Basic authentication is supported. 
//   Basic authentication to the server Permission required

//    RefferenceÅF https://github.com/masterzen/winrm

account.Windows.Account01.user     = 'administrator'
account.Windows.Account01.password = 'P@ssw0rd'

// CiscoUCS IA server firmware connection 
//
//   Cisco UCS CIMC SSH connection account information

account.CiscoUCS.Account01.user     = 'root'
account.CiscoUCS.Account01.password = 'P@ssw0rd'

// HPE IA server firmware connection
//
//   HP iLO REST API connection account information

account.HPiLO.Account01.user     = 'root'
account.HPiLO.Account01.password = 'P@ssw0rd'

// Fujitsu IA server firmware connection
//
//   Primergy iRMC REST API connection account information

account.Primergy.Account01.user     = 'admin'
account.Primergy.Account01.password = 'P@ssw0rd'

// Solaris server SSH connection
//
//   Solaris server SSH connection account information

account.Solaris.Account01.user     = 'someuser'
account.Solaris.Account01.password = 'P@ssw0rd'

// Solaris server firmware connection
//
//   Solaris XSCF API SSH connection account information

account.XSCF.Account01.user     = 'admin'
account.XSCF.Account01.password = 'P@ssw0rd'

// NetAPP storage firmware connection
//
//   NetAPP ONTAP System Manager SSH connection account information

account.NetAPP.Account01.user     = 'admin'
account.NetAPP.Account01.password = 'P@ssw0rd'

// HitachiVSP storage firmware connection
//
//   Hitachi Command Suite REST API connection account information

account.HitachiVSP.Account01.user     = 'admin'
account.HitachiVSP.Account01.password = 'P@ssw0rd'

// Fujitsu Eternus storage firmware connection
//
//   Fujitsu  Eternus CLI SSH connection account information

account.Eternus.Account01.user     = 'admin'
account.Eternus.Account01.password = 'P@ssw0rd'

// Oracle database connection
//
//   Oracle JDBC connection account information

account.Oracle.Account01.user     = 'scott'
account.Oracle.Account01.password = 'tiger'

// Zabbix management account connection
//
//   Zabbix API JSON-RPC connection account information

account.Zabbix.Account01.server   = 'http://zabbix:8081/zabbix'
account.Zabbix.Account01.user     = 'Admin'
account.Zabbix.Account01.password = 'zabbix'
