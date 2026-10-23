<?php
/**
 *
 * @package simplenews/discussion
 * @license GPL-2.0-only
 */

namespace simplenews\discussion\migrations;

class install_schema extends \phpbb\db\migration\migration
{
	public function effectively_installed()
	{
		return $this->db_tools->sql_table_exists($this->table_prefix . 'simplenews_topic');
	}

	public static function depends_on()
	{
		return array('\phpbb\db\migration\data\v330\v330');
	}

	public function update_schema()
	{
		return array(
			'add_tables' => array(
				$this->table_prefix . 'simplenews_topic' => array(
					'COLUMNS' => array(
						'news_id' => array('BINT', 0),
						'topic_id' => array('UINT', 0),
					),
					'PRIMARY_KEY' => 'news_id',
				),
			),
		);
	}

	public function revert_schema()
	{
		return array(
			'drop_tables' => array(
				$this->table_prefix . 'simplenews_topic',
			),
		);
	}
}
