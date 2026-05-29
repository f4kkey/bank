FROM php:8.3-fpm-alpine

# Install PHP Redis extension
RUN apk add --no-cache $PHPIZE_DEPS \
    && pecl install redis \
    && docker-php-ext-enable redis \
    && apk del $PHPIZE_DEPS

# Copy application code
COPY . /var/www/payment
COPY zz-bank-fpm.conf /usr/local/etc/php-fpm.d/zz-bank-fpm.conf

WORKDIR /var/www/payment

# PHP-FPM listens on port 9000 by default
EXPOSE 9000
