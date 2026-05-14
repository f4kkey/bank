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

$signature = $headers['X-Internal-Signature'] ?? null;
$timestamp = $headers['X-Internal-Timestamp'] ?? null;
$kongApiKey = $headers['X-Kong-Api-Key'] ?? null;
$isInternalCall = ($signature !== null && $timestamp !== null);
$isKongCall = false;

if ($isInternalCall) {
    $secret = getenv('INTERNAL_API_SECRET');
    if (!$secret) {
        http_response_code(500);
        echo json_encode(["status" => "ERROR", "message" => "Server misconfigured"]);
        exit;
    }

    // Validate timestamp (5 minute window)
    $now = round(microtime(true) * 1000);
    if (abs($now - (int)$timestamp) > 300000) {
        http_response_code(403);
        echo json_encode(["status" => "ERROR", "message" => "Request expired"]);
        exit;
    }

    // Validate HMAC signature
    $payload = $timestamp . ":" . $rawBody;
    $expectedSignature = base64_encode(hash_hmac('sha256', $payload, $secret, true));
    if (!hash_equals($expectedSignature, $signature)) {
        http_response_code(403);
        echo json_encode(["status" => "ERROR", "message" => "Invalid signature"]);
        exit;
    }

    error_log("[HMAC] Validated inter-service request: $method $uri");
}

if (!$isInternalCall) {
    $expectedKongKey = getenv('KONG_API_KEY');
    if ($expectedKongKey && $kongApiKey === $expectedKongKey) {
        $isKongCall = true;
    }
}

// Reject unauthenticated requests
if (!$isInternalCall && !$isKongCall) {
    http_response_code(403);
    error_log("[AUTH] Denied unauthenticated request: $method $uri");
    echo json_encode(["status" => "ERROR", "message" => "Forbidden"]);
    exit;
}

// Tag auth source so Java can enforce per-route access control
$headers['X-Internal-Source'] = $isInternalCall ? 'HMAC' : 'KONG';

$request = [
    "method" => $method,
    "path" => $path,
    "query" => $queryParams,
    "body" => $body,
    "headers" => $headers
];
error_log($uri);

// Rate limit only external requests (not inter-service)
if (!$isInternalCall && !rate_limit($redis, 100, 60)) {
    http_response_code(429);
    echo json_encode([
        "status" => "ERROR",
        "message" => "Too many requests"
    ]);
    exit;
}

$response = json_decode(call_java($request), true);

http_response_code($response['code'] ?? 500);

unset($response['code']);

header('Content-Type: application/json');

echo json_encode($response);
