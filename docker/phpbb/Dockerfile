FROM php:8.3-apache

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libpq-dev \
        unzip \
        ca-certificates \
    && docker-php-ext-install pgsql pdo_pgsql \
    && a2enmod rewrite \
    && rm -rf /var/lib/apt/lists/*

ARG PHPBB_VERSION=3.3.19
ARG PHPBB_SHA256=1530e5cabf7ecc9ecfaed3fa13da79f9e8a514623beafababc8800b5ce966de9

# /var/www/html is a volume in the PHP image, so the board lives beside it.
RUN curl -fsSL -o /tmp/phpbb.zip "https://download.phpbb.com/pub/release/3.3/${PHPBB_VERSION}/phpBB-${PHPBB_VERSION}.zip" \
    && echo "${PHPBB_SHA256}  /tmp/phpbb.zip" | sha256sum -c - \
    && unzip -q /tmp/phpbb.zip -d /tmp \
    && mkdir -p /opt/phpbb \
    && cp -a /tmp/phpBB3/. /opt/phpbb/ \
    && rm -rf /tmp/phpbb.zip /tmp/phpBB3 \
    && mkdir -p /opt/phpbb/ext/simplenews \
    && chown -R www-data:www-data /opt/phpbb

COPY ext/simplenews /opt/phpbb/ext/simplenews
COPY apache.conf /etc/apache2/conf-enabled/simplenews.conf
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
COPY create-bot.php /opt/phpbb/create-bot.php

RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
    && chmod +x /usr/local/bin/docker-entrypoint.sh \
    && chown -R www-data:www-data /opt/phpbb/ext

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["apache2-foreground"]
