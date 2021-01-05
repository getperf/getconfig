// Excel specification sheet path

    evidence.source = 'getconfig.xlsx'

// Excel inspection result file output path

    // evidence.target=Å@'check_sheet.xlsx'
    evidence.target = 'check_sheet_<date>.xlsx'

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
    test.timeout.VMWare  = 300

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
//  The following example is connection setting of user ID called 'Account01'. 
//  Copy each platform and change the part of Account01 to the specific user ID.

// vCenter virtual machine management REST API connection
//
//  vSphere Web Services API account

account.VMWare.Account01.server   = '192.168.0.200'  // IP or "https://{IP}/sdk"
account.VMWare.Account01.user     = 'test_user'
account.VMWare.Account01.password = 'P@ssw0rd'

// ESXi server management REST API connection
// 
//  Same as vCenter connection information above. For a single ESXi server, 
//  specify the IP of the ESXi server

account.VMHost.Account01.server   = '192.168.0.200'  // IP or "https://{IP}/sdk"
account.VMHost.Account01.user     = 'test_user'
account.VMHost.Account01.password = 'P@ssw0rd'

// Linux server SSH connection
//
//  Linux server to be inspected SSH connection account

account.Linux.Account01.user      = 'zabbix'
account.Linux.Account01.password  = 'P@ssw0rd'

// Windows server WinRM connection
//
//  Windows server to be inspected WinRM connection account
//  December 2020 Currently, only Basic authentication is supported. 
//  Basic authentication to the server Permission required
//
//    RefferenceÅF https://github.com/masterzen/winrm

account.Windows.Account01.user     = 'administrator'
account.Windows.Account01.password = 'P@ssw0rd'

// Cisco UCS IA server firmware connection 
//
//  Cisco CIMC SSH connection account

account.CiscoUCS.Account01.user     = 'root'
account.CiscoUCS.Account01.password = 'P@ssw0rd'

// HPE IA server firmware connection
//
//  HP iLO REST API connection account

account.HPiLO.Account01.user     = 'root'
account.HPiLO.Account01.password = 'P@ssw0rd'

// Fujitsu IA server firmware connection
//
//  Fujitsu iRMC REST API connection account

account.Primergy.Account01.user     = 'admin'
account.Primergy.Account01.password = 'P@ssw0rd'

// Solaris server SSH connection
//
//  Solaris server SSH connection account

account.Solaris.Account01.user     = 'someuser'
account.Solaris.Account01.password = 'P@ssw0rd'

// Solaris server firmware connection
//
//  Solaris XSCF API SSH connection account

account.XSCF.Account01.user     = 'admin'
account.XSCF.Account01.password = 'P@ssw0rd'

// NetApp storage firmware connection
//
//  NetApp ONTAP System Manager SSH connection account

account.NetApp.Account01.server   = '192.168.0.105'
account.NetApp.Account01.user     = 'admin'
account.NetApp.Account01.password = 'P@ssw0rd'

// HitachiVSP storage firmware connection
//
//  Hitachi Command Suite REST API connection account

account.HitachiVSP.Account01.server   = 'http://192.168.0.104'
account.HitachiVSP.Account01.user     = 'admin'
account.HitachiVSP.Account01.password = 'P@ssw0rd'

// Fujitsu Eternus storage firmware connection
//
//  Fujitsu  Eternus CLI SSH connection account

account.Eternus.Account01.user     = 'admin'
account.Eternus.Account01.password = 'P@ssw0rd'

// Oracle database connection
//
//  Oracle JDBC connection account

account.Oracle.Account01.user     = 'scott'
account.Oracle.Account01.password = 'tiger'

// Zabbix management account connection
//
//  Zabbix API JSON-RPC connection account

account.Zabbix.Account01.server   = 'http://zabbix:8081/zabbix'
account.Zabbix.Account01.user     = 'Admin'
account.Zabbix.Account01.password = 'zabbix'
