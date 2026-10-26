"""Insert lorem ipsum posts into the simplenews news table.

Usage:
  python sql/insert-lorem.py
  python sql/insert-lorem.py --count 50 --category news

Connection defaults match a local Postgres (see application.properties).
Override with flags or PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD.

Requires: pip install psycopg2-binary
"""

import argparse
import os
import sys
from datetime import datetime, timedelta

try:
    import psycopg2
except ImportError:
    print("psycopg2 is required: pip install psycopg2-binary", file=sys.stderr)
    sys.exit(1)

PARAGRAPHS = [
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
    "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
    "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
    "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
    "Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit.",
    "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae.",
]


def html_body(index):
    count = 2 + (index % 4)
    parts = []
    for offset in range(count):
        parts.append("<p>{}</p>".format(PARAGRAPHS[(index + offset) % len(PARAGRAPHS)]))
    return "\n".join(parts)


def connect(args):
    return psycopg2.connect(
        host=args.host,
        port=args.port,
        dbname=args.dbname,
        user=args.user,
        password=args.password,
    )


def next_index(cur):
    cur.execute(
        """
        SELECT COALESCE(MAX(CAST(substring(key FROM 7) AS integer)), 0)
        FROM public.news
        WHERE key ~ '^lorem-[0-9]+$'
        """
    )
    return cur.fetchone()[0] + 1


def main():
    parser = argparse.ArgumentParser(description="Insert lorem ipsum news posts.")
    parser.add_argument("--count", type=int, default=20, help="number of posts to insert (default 20)")
    parser.add_argument("--category", default="news", help="category name (default news)")
    parser.add_argument("--host", default=os.environ.get("PGHOST", "localhost"))
    parser.add_argument("--port", type=int, default=int(os.environ.get("PGPORT", "5432")))
    parser.add_argument("--dbname", default=os.environ.get("PGDATABASE", "news"))
    parser.add_argument("--user", default=os.environ.get("PGUSER", "postgres"))
    parser.add_argument("--password", default=os.environ.get("PGPASSWORD", "postgres"))
    args = parser.parse_args()

    if args.count < 1:
        print("--count must be at least 1", file=sys.stderr)
        sys.exit(1)

    conn = connect(args)
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT id FROM public.category WHERE name = %s", (args.category,))
            row = cur.fetchone()
            if row is None:
                print("No category named {!r}".format(args.category), file=sys.stderr)
                sys.exit(1)
            category_id = row[0]

            cur.execute(
                """
                SELECT setval(
                    pg_get_serial_sequence('public.news', 'id'),
                    GREATEST(COALESCE((SELECT MAX(id) FROM public.news), 1), 1)
                )
                """
            )

            start = next_index(cur)
            now = datetime.now()
            for i in range(args.count):
                n = start + i
                created = now - timedelta(hours=args.count - i)
                cur.execute(
                    """
                    INSERT INTO public.news
                        (key, title, content, tags, category, creator, last_modified_by,
                         created, last_modified, visible)
                    VALUES
                        (%s, %s, %s, %s, %s, 'admin', 'admin', %s, %s, true)
                    """,
                    (
                        "lorem-{}".format(n),
                        "Lorem ipsum {}".format(n),
                        html_body(n),
                        ["lorem", "ipsum"],
                        category_id,
                        created,
                        created,
                    ),
                )
        conn.commit()
    finally:
        conn.close()

    print("Inserted {} posts into category {!r} (lorem-{} .. lorem-{}).".format(
        args.count, args.category, start, start + args.count - 1
    ))


if __name__ == "__main__":
    main()
