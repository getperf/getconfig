// �����d�l�V�[�g��`

evidence.source = './�T�[�o�`�F�b�N�V�[�g.xlsx'

// �������ʃt�@�C���o�͐�

// evidence.target='./build/�T�[�o�`�F�b�N�V�[�g_<date>.xlsx'
 evidence.target='./build/check_sheet.xlsx'

// �������ʃ��O�f�B���N�g��

evidence.staging_dir='./build/log'

// �����A���،��ʃf�B���N�g��

evidence.json_dir='./src/test/resources/json'

// ���񉻂��Ȃ��^�X�N

test.serialization.tasks = ['vCenter']

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resources/log/'

// �C���x���g���p�`�P�b�g�J�X�^���t�B�[���h��

ticket.custom_field.inventory = '�C���x���g��'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.timeout         = 30
test.timeout.Linux   = 30
test.timeout.Windows = 300
test.timeout.vCenter = 300

// �R�}���h�̎�̃f�o�b�O���[�h

test.debug         = false
test.debug.Linux   = false
test.debug.Windows = false
test.debug.vCenter = false

// DryRun �\�s���K���[�h

test.dry_run         = false
test.dry_run.Linux   = false
test.dry_run.Windows = false
test.dry_run.vCenter = false

// vCenter�ڑ����

account.vCenter.Account01.server   = '192.168.10.100'
account.vCenter.Account01.user     = 'test_user'
account.vCenter.Account01.password = 'P@ssword'

// Linux �ڑ����

account.Linux.Account01.user      = 'someuser'
account.Linux.Account01.password  = 'P@ssword'
account.Linux.Account01.work_dir  = '/tmp/gradle_test'

// Windows �ڑ����

account.Windows.Account01.user     = 'administrator'
account.Windows.Account01.password = 'P@ssword'

