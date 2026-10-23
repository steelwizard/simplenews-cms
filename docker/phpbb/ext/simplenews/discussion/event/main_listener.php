<?php
/**
 *
 * @package simplenews/discussion
 * @license GPL-2.0-only
 */

namespace simplenews\discussion\event;

use Symfony\Component\EventDispatcher\EventSubscriberInterface;

class main_listener implements EventSubscriberInterface
{
	/** @var \phpbb\db\driver\driver_interface */
	protected $db;

	protected $map_table;
	protected $pending = array();
	protected $flushed = false;

	public function __construct(\phpbb\db\driver\driver_interface $db, $table_prefix)
	{
		$this->db = $db;
		$this->map_table = $table_prefix . 'simplenews_topic';
	}

	public static function getSubscribedEvents()
	{
		return array(
			'core.submit_post_end' => 'on_submit',
			'core.delete_posts_after' => 'on_delete',
			'core.approve_posts_after' => 'on_approve',
			'core.disapprove_posts_after' => 'on_disapprove',
			// phpBB closes the database during this call, so the webhook has to run here.
			'core.garbage_collection' => 'flush_notifications',
		);
	}

	public function on_submit($event)
	{
		$data = isset($event['data']) ? $event['data'] : array();
		if (is_array($data) && !empty($data['topic_id']))
		{
			$this->queue((int) $data['topic_id']);
		}
	}

	public function on_delete($event)
	{
		$this->queue_rows(isset($event['topic_ids']) ? $event['topic_ids'] : null);
	}

	public function on_approve($event)
	{
		$this->queue_rows(isset($event['post_info']) ? $event['post_info'] : null);
		$this->queue_rows(isset($event['topic_info']) ? $event['topic_info'] : null);
	}

	public function on_disapprove($event)
	{
		$this->queue_rows(isset($event['post_info']) ? $event['post_info'] : null);
		$this->queue_rows(isset($event['post_disapprove_list']) ? $event['post_disapprove_list'] : null);
		$this->queue_rows(isset($event['topic_information']) ? $event['topic_information'] : null);
	}

	public function flush_notifications()
	{
		if ($this->flushed)
		{
			return;
		}
		$this->flushed = true;
		foreach (array_keys($this->pending) as $topic_id)
		{
			$this->notify((int) $topic_id);
		}
	}

	protected function queue_rows($rows)
	{
		if (!is_array($rows))
		{
			return;
		}
		foreach ($rows as $row)
		{
			if (is_array($row))
			{
				if (isset($row['topic_id']))
				{
					$this->queue((int) $row['topic_id']);
				}
			}
			else
			{
				$this->queue((int) $row);
			}
		}
	}

	protected function queue($topic_id)
	{
		if ($topic_id > 0)
		{
			$this->pending[$topic_id] = true;
		}
	}

	protected function notify($topic_id)
	{
		$base = getenv('SIMPLENEWS_WEBHOOK_URL');
		$key = getenv('SIMPLENEWS_WEBHOOK_KEY');
		if (!is_string($base) || $base === '' || !is_string($key) || $key === '')
		{
			return;
		}
		if (!$this->is_tracked($topic_id))
		{
			return;
		}
		if (!function_exists('curl_init'))
		{
			return;
		}

		$url = rtrim($base, '/') . '/_private/phpbb/topics/' . (int) $topic_id . '/replies';
		$payload = json_encode(array('replyCount' => $this->reply_count($topic_id)));
		$ch = curl_init($url);
		curl_setopt_array($ch, array(
			CURLOPT_POST => true,
			CURLOPT_POSTFIELDS => $payload,
			CURLOPT_HTTPHEADER => array(
				'Content-Type: application/json',
				'Authorization: Bearer ' . $key,
			),
			CURLOPT_RETURNTRANSFER => true,
			CURLOPT_TIMEOUT => 5,
			CURLOPT_CONNECTTIMEOUT => 2,
		));
		curl_exec($ch);
		if (curl_errno($ch))
		{
			error_log('simplenews discussion webhook failed: ' . curl_error($ch));
		}
		curl_close($ch);
	}

	protected function is_tracked($topic_id)
	{
		$sql = 'SELECT news_id FROM ' . $this->map_table . ' WHERE topic_id = ' . (int) $topic_id;
		$result = $this->db->sql_query_limit($sql, 1);
		$news_id = $this->db->sql_fetchfield('news_id');
		$this->db->sql_freeresult($result);
		return $news_id !== false && $news_id !== null && $news_id !== '';
	}

	protected function reply_count($topic_id)
	{
		$sql = 'SELECT topic_posts_approved FROM ' . TOPICS_TABLE . ' WHERE topic_id = ' . (int) $topic_id;
		$result = $this->db->sql_query($sql);
		$approved = $this->db->sql_fetchfield('topic_posts_approved');
		$this->db->sql_freeresult($result);
		if ($approved === false || $approved === null || $approved === '')
		{
			return 0;
		}
		return max(0, ((int) $approved) - 1);
	}
}
