<?php
/**
 * Creates the discussion bot if it is missing. Run from /opt/phpbb.
 */
define('IN_PHPBB', true);
$phpbb_root_path = '/opt/phpbb/';
$phpEx = 'php';
require $phpbb_root_path . 'includes/startup.' . $phpEx;
require $phpbb_root_path . 'phpbb/class_loader.' . $phpEx;

$loader = new \phpbb\class_loader('phpbb\\', $phpbb_root_path . 'phpbb/', $phpEx);
$loader->register();
$loader_ext = new \phpbb\class_loader('\\', $phpbb_root_path . 'ext/', $phpEx);
$loader_ext->register();

$config_file = new \phpbb\config_php_file($phpbb_root_path, $phpEx);
extract($config_file->get_all());
require $phpbb_root_path . 'includes/constants.' . $phpEx;
require $phpbb_root_path . 'includes/functions.' . $phpEx;
require $phpbb_root_path . 'includes/functions_user.' . $phpEx;
require $phpbb_root_path . 'includes/functions_compatibility.' . $phpEx;
require $phpbb_root_path . 'includes/utf/utf_tools.' . $phpEx;

$phpbb_container = (new \phpbb\di\container_builder($phpbb_root_path, $phpEx))
	->with_config($config_file)
	->get_container();
$phpbb_container->get('request')->enable_super_globals();
require $phpbb_root_path . 'includes/compatibility_globals.' . $phpEx;
register_compatibility_globals();

$config = $phpbb_container->get('config');
$db = $phpbb_container->get('dbal.conn');
$passwords = $phpbb_container->get('passwords.manager');
$user = $phpbb_container->get('user');
$user->data['user_id'] = ANONYMOUS;
$user->ip = '127.0.0.1';
$user->data['username'] = '';
$user->data['user_email'] = '';

$name = getenv('SIMPLENEWS_BOT_USERNAME');
if (!is_string($name) || $name === '') {
	$name = 'simplenews';
}
$email = getenv('SIMPLENEWS_BOT_EMAIL');
if (!is_string($email) || $email === '') {
	$email = 'simplenews@example.com';
}
$password = getenv('SIMPLENEWS_BOT_PASSWORD');
if (!is_string($password) || $password === '') {
	$password = 'simplenews';
}

$sql = 'SELECT user_id FROM ' . USERS_TABLE . " WHERE username_clean = '" . $db->sql_escape(utf8_clean_string($name)) . "'";
$result = $db->sql_query($sql);
$existing = (int) $db->sql_fetchfield('user_id');
$db->sql_freeresult($result);
if ($existing > 1) {
	echo "Forum user {$name} already exists.\n";
	exit(0);
}

$sql = 'SELECT group_id FROM ' . GROUPS_TABLE . " WHERE group_name = 'REGISTERED' AND group_type = " . GROUP_SPECIAL;
$result = $db->sql_query($sql);
$group_id = (int) $db->sql_fetchfield('group_id');
$db->sql_freeresult($result);
if ($group_id < 1) {
	fwrite(STDERR, "Registered group was not found.\n");
	exit(1);
}

$user_id = user_add(array(
	'username' => $name,
	'user_password' => $passwords->hash($password),
	'user_email' => $email,
	'group_id' => $group_id,
	'user_timezone' => $config['board_timezone'],
	'user_lang' => $config['default_lang'],
	'user_type' => USER_NORMAL,
	'user_regdate' => time(),
));

if (!$user_id) {
	fwrite(STDERR, "Could not create forum user {$name}.\n");
	exit(1);
}
echo "Created forum user {$name}.\n";
