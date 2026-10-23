#!/bin/sh
set -eu

: "${PHPBB_DBHOST:=data}"
: "${PHPBB_DBPORT:=5432}"
: "${PHPBB_DBUSER:=postgres}"
: "${PHPBB_DBPASS:=postgres}"
: "${PHPBB_DBNAME:=phpbb}"
: "${PHPBB_ADMIN_NAME:=admin}"
: "${PHPBB_ADMIN_PASSWORD:=adminadmin}"
: "${PHPBB_ADMIN_EMAIL:=admin@example.com}"

cd /opt/phpbb

echo "Waiting for PostgreSQL..."
ready=0
i=0
while [ "$i" -lt 30 ]; do
    if php -r 'try { new PDO("pgsql:host=".getenv("PHPBB_DBHOST").";port=".getenv("PHPBB_DBPORT").";dbname=postgres", getenv("PHPBB_DBUSER"), getenv("PHPBB_DBPASS")); } catch (Throwable $e) { fwrite(STDERR, $e->getMessage()."\n"); exit(1); }'; then
        ready=1
        break
    fi
    i=$((i + 1))
    sleep 2
done
if [ "$ready" -ne 1 ]; then
    echo "PostgreSQL did not become ready. The forum will retry; the site itself does not need it." >&2
    exit 1
fi

php -r '$name = getenv("PHPBB_DBNAME"); if (!preg_match("/^[A-Za-z0-9_]+$/", $name)) { fwrite(STDERR, "Invalid database name\n"); exit(1); } $pdo = new PDO("pgsql:host=".getenv("PHPBB_DBHOST").";port=".getenv("PHPBB_DBPORT").";dbname=postgres", getenv("PHPBB_DBUSER"), getenv("PHPBB_DBPASS")); $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION); $check = $pdo->prepare("SELECT 1 FROM pg_database WHERE datname = ?"); $check->execute([$name]); if (!$check->fetchColumn()) { $pdo->exec("CREATE DATABASE ".$name); }'

mkdir -p /var/phpbb-config

# A failed install can leave an empty config.php. That must not count as done.
if ! grep -q dbhost /var/phpbb-config/config.php 2>/dev/null; then
    rm -f /var/phpbb-config/config.php /opt/phpbb/config.php
    cat > /opt/phpbb/install/install-config.yml << EOF
installer:
    admin:
        name: "${PHPBB_ADMIN_NAME}"
        password: "${PHPBB_ADMIN_PASSWORD}"
        email: "${PHPBB_ADMIN_EMAIL}"
    board:
        lang: en
        name: Article discussion
        description: Comments for articles
    database:
        dbms: postgres
        dbhost: "${PHPBB_DBHOST}"
        dbport: ${PHPBB_DBPORT}
        dbuser: "${PHPBB_DBUSER}"
        dbpasswd: "${PHPBB_DBPASS}"
        dbname: "${PHPBB_DBNAME}"
        table_prefix: phpbb_
    email:
        enabled: false
        smtp_delivery: false
        smtp_host: ~
        smtp_port: ~
        smtp_auth: ~
        smtp_user: ~
        smtp_pass: ~
    server:
        cookie_secure: false
        server_protocol: http://
        force_server_vars: false
        server_name: localhost
        server_port: 8080
        script_path: /forum
    extensions: []
EOF
    php /opt/phpbb/install/phpbbcli.php install /opt/phpbb/install/install-config.yml
    cp /opt/phpbb/config.php /var/phpbb-config/config.php
fi

cp /var/phpbb-config/config.php /opt/phpbb/config.php
rm -rf /opt/phpbb/install
sed -i 's|^RewriteBase /$|RewriteBase /forum|' /opt/phpbb/.htaccess || true

php /opt/phpbb/bin/phpbbcli.php extension:enable simplenews/discussion --no-interaction || true
php /opt/phpbb/bin/phpbbcli.php db:migrate --no-interaction || true
php /opt/phpbb/create-bot.php || echo "Could not create the forum bot user." >&2

chown www-data:www-data /opt/phpbb/config.php
chown -R www-data:www-data /opt/phpbb/cache /opt/phpbb/store /opt/phpbb/files /opt/phpbb/images
chmod 640 /opt/phpbb/config.php

exec docker-php-entrypoint "$@"
