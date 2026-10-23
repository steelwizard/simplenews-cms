<?php
/**
 *
 * @package simplenews/discussion
 * @license GPL-2.0-only
 */

namespace simplenews\discussion\controller;

class api
{
	/** @var \phpbb\request\request */
	protected $request;

	/** @var \phpbb\db\driver\driver_interface */
	protected $db;

	protected $root_path;
	protected $php_ext;
	protected $map_table;

	public function __construct(\phpbb\request\request $request, \phpbb\db\driver\driver_interface $db, $root_path, $php_ext, $table_prefix)
	{
		$this->request = $request;
		$this->db = $db;
		$this->root_path = $root_path;
		$this->php_ext = $php_ext;
		$this->map_table = $table_prefix . 'simplenews_topic';
	}

	public function create()
	{
		if (!$this->authorized())
		{
			return $this->json(array('error' => 'unauthorized'), 401);
		}
		$data = $this->body();
		if ($data === null)
		{
			return $this->json(array('error' => 'invalid json'), 400);
		}

		$news_id = isset($data['newsId']) ? (int) $data['newsId'] : 0;
		$title = isset($data['title']) ? trim((string) $data['title']) : '';
		$permalink = isset($data['permalink']) ? trim((string) $data['permalink']) : '';
		$forum_id = isset($data['forumId']) ? (int) $data['forumId'] : 0;
		if ($news_id < 1 || $title === '' || !$this->valid_permalink($permalink) || $forum_id < 1)
		{
			return $this->json(array('error' => 'newsId, title, permalink and forumId are required'), 400);
		}

		$existing = $this->topic_for_news($news_id);
		if ($existing)
		{
			return $this->json(array('topicId' => $existing), 200);
		}
		if (!$this->forum_exists($forum_id))
		{
			return $this->json(array('error' => 'forum not found'), 400);
		}
		$bot = $this->bot();
		if ($bot === null)
		{
			return $this->json(array('error' => 'bot user not found'), 503);
		}

		$this->load_posting();
		$this->act_as($bot);
		list($message, $uid, $bitfield, $checksum) = $this->message_for($permalink);
		$poll = array();
		$post = $this->post_data($forum_id, $title, $message, $uid, $bitfield, $checksum);
		$post['topic_id'] = 0;
		$post['topic_status'] = ITEM_UNLOCKED;
		submit_post('post', $title, $bot['username'], POST_NORMAL, $poll, $post);

		$topic_id = isset($post['topic_id']) ? (int) $post['topic_id'] : 0;
		if ($topic_id < 1)
		{
			return $this->json(array('error' => 'topic was not created'), 500);
		}

		$again = $this->topic_for_news($news_id);
		if ($again)
		{
			return $this->json(array('topicId' => $again), 200);
		}
		$sql = 'INSERT INTO ' . $this->map_table . ' ' . $this->db->sql_build_array('INSERT', array(
			'news_id' => $news_id,
			'topic_id' => $topic_id,
		));
		$this->db->sql_query($sql);
		return $this->json(array('topicId' => $topic_id), 201);
	}

	public function update($news_id)
	{
		if (!$this->authorized())
		{
			return $this->json(array('error' => 'unauthorized'), 401);
		}
		$data = $this->body();
		if ($data === null)
		{
			return $this->json(array('error' => 'invalid json'), 400);
		}
		$news_id = (int) $news_id;
		$topic_id = $this->topic_for_news($news_id);
		if (!$topic_id)
		{
			return $this->json(array('error' => 'not found'), 404);
		}

		$title = isset($data['title']) ? trim((string) $data['title']) : '';
		$permalink = isset($data['permalink']) ? trim((string) $data['permalink']) : '';
		$has_text = $title !== '' && $this->valid_permalink($permalink);
		$has_lock = array_key_exists('locked', $data);
		if (!$has_text && !$has_lock)
		{
			return $this->json(array('error' => 'nothing to update'), 400);
		}

		if ($has_text)
		{
			$bot = $this->bot();
			if ($bot === null)
			{
				return $this->json(array('error' => 'bot user not found'), 503);
			}
			$this->edit_opening_post($topic_id, $title, $permalink, $bot);
		}
		if ($has_lock)
		{
			$status = $data['locked'] ? ITEM_LOCKED : ITEM_UNLOCKED;
			$sql = 'UPDATE ' . TOPICS_TABLE . '
				SET topic_status = ' . (int) $status . '
				WHERE topic_id = ' . (int) $topic_id;
			$this->db->sql_query($sql);
		}
		return new \Symfony\Component\HttpFoundation\Response('', 204);
	}

	protected function edit_opening_post($topic_id, $title, $permalink, array $bot)
	{
		$this->load_posting();
		$sql = 'SELECT t.forum_id, t.topic_first_post_id, t.topic_last_post_id,
				t.topic_posts_approved, t.topic_posts_unapproved, t.topic_posts_softdeleted,
				t.topic_attachment, t.icon_id, p.poster_id, p.post_edit_locked
			FROM ' . TOPICS_TABLE . ' t
			JOIN ' . POSTS_TABLE . ' p ON p.post_id = t.topic_first_post_id
			WHERE t.topic_id = ' . (int) $topic_id;
		$result = $this->db->sql_query($sql);
		$row = $this->db->sql_fetchrow($result);
		$this->db->sql_freeresult($result);
		if (!$row)
		{
			return;
		}

		$this->act_as($bot);
		list($message, $uid, $bitfield, $checksum) = $this->message_for($permalink);
		$poll = array();
		$post = $this->post_data((int) $row['forum_id'], $title, $message, $uid, $bitfield, $checksum);
		$post['topic_id'] = (int) $topic_id;
		$post['post_id'] = (int) $row['topic_first_post_id'];
		$post['poster_id'] = (int) $row['poster_id'];
		$post['icon_id'] = (int) $row['icon_id'];
		$post['post_edit_locked'] = (int) $row['post_edit_locked'];
		$post['post_edit_reason'] = '';
		$post['post_edit_user'] = (int) $bot['user_id'];
		$post['topic_first_post_id'] = (int) $row['topic_first_post_id'];
		$post['topic_last_post_id'] = (int) $row['topic_last_post_id'];
		$post['topic_posts_approved'] = (int) $row['topic_posts_approved'];
		$post['topic_posts_unapproved'] = (int) $row['topic_posts_unapproved'];
		$post['topic_posts_softdeleted'] = (int) $row['topic_posts_softdeleted'];
		$post['topic_attachment'] = (int) $row['topic_attachment'];
		submit_post('edit', $title, $bot['username'], POST_NORMAL, $poll, $post);
	}

	protected function post_data($forum_id, $title, $message, $uid, $bitfield, $checksum)
	{
		return array(
			'forum_id' => (int) $forum_id,
			'icon_id' => 0,
			'enable_bbcode' => 1,
			'enable_smilies' => 1,
			'enable_urls' => 1,
			'enable_sig' => 1,
			'enable_indexing' => 1,
			'message' => $message,
			'message_md5' => $checksum,
			'attachment_data' => array(),
			'bbcode_bitfield' => $bitfield,
			'bbcode_uid' => $uid,
			'post_edit_locked' => 0,
			'topic_title' => $title,
			'notify_set' => false,
			'notify' => false,
			'post_time' => time(),
			'force_approved_state' => ITEM_APPROVED,
		);
	}

	protected function message_for($permalink)
	{
		$text = "Discuss this article:\n" . $permalink;
		$uid = $bitfield = '';
		$flags = 0;
		generate_text_for_storage($text, $uid, $bitfield, $flags, true, true, true);
		return array($text, $uid, $bitfield, md5($text));
	}

	protected function load_posting()
	{
		if (!function_exists('submit_post'))
		{
			include $this->root_path . 'includes/functions_posting.' . $this->php_ext;
		}
		if (!function_exists('generate_text_for_storage'))
		{
			include $this->root_path . 'includes/functions_content.' . $this->php_ext;
		}
	}

	protected function act_as(array $bot)
	{
		global $user;
		$user->data['user_id'] = (int) $bot['user_id'];
		$user->data['username'] = $bot['username'];
		$user->data['user_colour'] = !empty($bot['user_colour']) ? $bot['user_colour'] : '';
		$user->data['is_registered'] = 1;
		if (empty($user->ip))
		{
			$user->ip = '127.0.0.1';
		}
	}

	protected function bot()
	{
		$name = getenv('SIMPLENEWS_BOT_USERNAME');
		if (!is_string($name) || $name === '')
		{
			$name = 'simplenews';
		}
		$sql = 'SELECT user_id, username, user_colour
			FROM ' . USERS_TABLE . "
			WHERE username_clean = '" . $this->db->sql_escape(utf8_clean_string($name)) . "'";
		$result = $this->db->sql_query($sql);
		$row = $this->db->sql_fetchrow($result);
		$this->db->sql_freeresult($result);
		if (!$row || (int) $row['user_id'] <= ANONYMOUS)
		{
			return null;
		}
		return $row;
	}

	protected function forum_exists($forum_id)
	{
		$sql = 'SELECT forum_id
			FROM ' . FORUMS_TABLE . '
			WHERE forum_id = ' . (int) $forum_id . '
				AND forum_type = ' . FORUM_POST;
		$result = $this->db->sql_query_limit($sql, 1);
		$id = $this->db->sql_fetchfield('forum_id');
		$this->db->sql_freeresult($result);
		return (bool) $id;
	}

	protected function topic_for_news($news_id)
	{
		$sql = 'SELECT topic_id FROM ' . $this->map_table . ' WHERE news_id = ' . (int) $news_id;
		$result = $this->db->sql_query_limit($sql, 1);
		$topic_id = $this->db->sql_fetchfield('topic_id');
		$this->db->sql_freeresult($result);
		return $topic_id ? (int) $topic_id : 0;
	}

	protected function authorized()
	{
		$expected = getenv('SIMPLENEWS_API_KEY');
		if (!is_string($expected) || $expected === '')
		{
			return false;
		}
		$header = $this->request->header('Authorization', '');
		if (!is_string($header) || stripos($header, 'Bearer ') !== 0)
		{
			$header = isset($_SERVER['HTTP_AUTHORIZATION']) ? $_SERVER['HTTP_AUTHORIZATION'] : '';
		}
		if (!is_string($header) || stripos($header, 'Bearer ') !== 0)
		{
			return false;
		}
		return hash_equals($expected, substr($header, 7));
	}

	protected function valid_permalink($permalink)
	{
		return (bool) preg_match('#^https?://#i', $permalink);
	}

	protected function body()
	{
		$raw = file_get_contents('php://input');
		if ($raw === false || $raw === '')
		{
			return null;
		}
		$data = json_decode($raw, true);
		return is_array($data) ? $data : null;
	}

	protected function json(array $data, $status)
	{
		return new \Symfony\Component\HttpFoundation\JsonResponse($data, $status);
	}
}
