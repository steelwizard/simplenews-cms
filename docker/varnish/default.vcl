vcl 4.1;

backend default {
    .host = "web";
    .port = "8080";
    .connect_timeout = 5s;
    .first_byte_timeout = 600s;
    .between_bytes_timeout = 600s;
    .probe = {
        .url = "/index.html";
        .timeout = 2s;
        .interval = 5s;
        .window = 5;
        .threshold = 2;
    }
}

# No health probe: phpBB answers /forum with redirects, and a failed probe
# makes Varnish skip the backend entirely. A down forum fails only that request.
backend forum {
    .host = "forum";
    .port = "80";
    .connect_timeout = 2s;
    .first_byte_timeout = 30s;
    .between_bytes_timeout = 30s;
}

sub vcl_recv {
    # phpBB keeps its own session. Do not cache it and do not send it to the CMS.
    if (req.url ~ "^/forum($|[/?])") {
        set req.backend_hint = forum;
        return (pass);
    }

    if (req.method != "GET" && req.method != "HEAD") {
        return (pass);
    }

    # Admin, login, uploads and counted HTML pages are not shared between visitors.
    # Public HTML records a click on every view, so those requests stay on the app.
    if (req.url ~ "^/_private" ||
        req.url ~ "^/(login|logout|error|actuator)(/|$|\?)" ||
        req.url ~ "^/(media|download|clicks)(/|$|\?)") {
        return (pass);
    }

    if (req.url !~ "^/(css|js)/" &&
        req.url !~ "\.(css|js|mjs|gif|jpe?g|png|webp|svg|ico|woff2?|json)(\?|$)") {
        return (pass);
    }

    # A signed-in browser keeps its own copy. Everyone else shares one.
    if (req.http.Cookie ~ "(^|; )SESSION=") {
        return (pass);
    }
    if (req.http.Authorization) {
        return (pass);
    }
    unset req.http.Cookie;
    return (hash);
}

sub vcl_backend_response {
    if (beresp.status != 200 || beresp.http.Set-Cookie) {
        set beresp.ttl = 0s;
        set beresp.uncacheable = true;
        return (deliver);
    }

    # Spring Security sends no-store on every response. These paths are still public.
    unset beresp.http.Pragma;
    unset beresp.http.Expires;
    if (bereq.url ~ "\.json(\?|$)") {
        set beresp.ttl = 1m;
        set beresp.http.Cache-Control = "public, max-age=60";
    } else {
        set beresp.ttl = 10m;
        set beresp.http.Cache-Control = "public, max-age=600";
    }
    if (beresp.http.Content-Length ~ "^[0-9]{8,}$") {
        set beresp.ttl = 0s;
        set beresp.uncacheable = true;
        return (deliver);
    }
    set beresp.grace = 1m;
    return (deliver);
}

sub vcl_deliver {
    if (obj.hits > 0) {
        set resp.http.X-Cache = "HIT";
    } else {
        set resp.http.X-Cache = "MISS";
    }
}
