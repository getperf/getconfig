// �����d�l�V�[�g��`

evidence.source = './getconfig.xlsx'

// �������ʃt�@�C���o�͐�

// evidence.target='./build/�T�[�o�`�F�b�N�V�[�g_<date>.xlsx'
evidence.target='./build/�T�[�o�`�F�b�N�V�[�g_<date>.xlsx'

// �������ʃ��O�f�B���N�g��

evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��

evidence.json_dir='./src/test/resources/json'

// ���񉻂��Ȃ��^�X�N

test.serialization.tasks = ['VMWare', 'VMHost']

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// HUB�T�[�o�G�[�W�F���g���O�ۑ��f�B���N�g��

test.hub_inventory_dir = './src/test/resources/hub/inventory'

// �C���x���g���p�`�P�b�g�J�X�^���t�B�[���h��

ticket.custom_field.inventory = '�C���x���g��'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.timeout.Default = 30
test.timeout.Linux   = 30
test.timeout.Windows = 300
test.timeout.VMWare = 300

// �R�}���h�̎�̃f�o�b�O���[�h

test.debug.Default = false
test.debug.Linux   = false
test.debug.Windows = false
test.debug.VMWare = false

// DryRun �\�s���K���[�h

test.dry_run.Default = false
test.dry_run.Linux   = false
test.dry_run.Windows = false
test.dry_run.VMWare = false

// vCenter�ڑ����

account.VMWare.Account01.server   = '192.168.0.200'
account.VMWare.Account01.user     = 'test_user'
account.VMWare.Account01.password = 'P@ssword'

// VMHost �ڑ����

account.VMHost.Account01.server   = '192.168.0.200'
account.VMHost.Account01.user     = 'test_user'
account.VMHost.Account01.password = 'P@ssword'

// Linux �ڑ����

account.Linux.Account01.user      = 'someuser'
account.Linux.Account01.password  = 'P@ssword'
account.Linux.Account01.work_dir  = '/tmp/gradle_test'

// Windows �ڑ����

account.Windows.Account01.user     = 'administrator'
account.Windows.Account01.password = 'P@ssw0rd'

// CiscoUCS �ڑ����

account.CiscoUCS.Account01.user     = 'root'
account.CiscoUCS.Account01.password = 'P@ssw0rd'

// HPiLO �ڑ����

account.HPiLO.Account01.user     = 'root'
account.HPiLO.Account01.password = 'P@ssw0rd'

// Primergy �ڑ����

account.Primergy.Account01.user     = 'admin'
account.Primergy.Account01.password = 'P@ssw0rd'

// Solaris �ڑ����

account.Solaris.Account01.user     = 'someuser'
account.Solaris.Account01.password = 'P@ssw0rd'
