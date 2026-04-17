<?php

define('JAVA_HOST', '127.0.0.1');
define('JAVA_PORT', 9000);

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
