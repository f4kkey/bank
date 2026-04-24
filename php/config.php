<?php

define('JAVA_HOST', '127.0.0.1');
define('JAVA_PORT', 12345);

$redis = new Redis();
$redis->connect('127.0.0.1', 6379); 

function rate_limit($redis, $limit = 100, $window = 60)
{
    $ip = $_SERVER['REMOTE_ADDR'];
    $key = "rate_limit:" . $ip;

    $current = $redis->incr($key);

    if ($current == 1) {
        $redis->expire($key, $window);
    }

    if ($current > $limit) {
        return false;
    }

    return true;
}

function call_java($data)
{
    $socket = fsockopen(JAVA_HOST, JAVA_PORT, $errno, $errstr, 5);

    if (!$socket) {
        return json_encode([
            "status" => "ERROR",
            "message" => "cannot connect to java bank server"
        ]);
    }
    fwrite($socket, json_encode($data) . "\n");
    $response = fgets($socket);
    fclose($socket);

    return $response;
}
