<?php

require_once "config.php";

$method = $_SERVER["REQUEST_METHOD"];
$uri = $_SERVER['REQUEST_URI'];
$path = parse_url($uri, PHP_URL_PATH);

$queryParams = $_GET;

$rawBody = file_get_contents("php://input");
$body = json_decode($rawBody, true);

if ($body === null) {
    $body = $_POST;
}

$headers = getallheaders();


$request = [
    "method" => $method,
    "path" => $path,
    "query" => $queryParams,
    "body" => $body,
    "headers" => $headers
];
error_log($uri);

echo call_java($request);
