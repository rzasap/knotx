# Server

Server is essentially a "heart" (a main [Verticle](http://vertx.io/docs/vertx-core/java/#_verticles)) of Knot.x.
It creates HTTP Server, listening for browser requests, and is responsible for coordination of 
communication between [[Repository Connectors|RepositoryConnectors]], [[Splitter|Splitter]] and all deployed [[Knots|Knot]].

## How does it work?
Once the HTTP request from the browser comes to the Knot.x, it goes to the **Server** verticle.
Server performs following actions when receives HTTP request:

- Verifies if request **method** is configured in `routing` (see config below), and sends 
**Method Not Allowed** response if not matches
- Search for the **repository** address in `repositories` configuration, by matching the 
requested path with the regexp from config, and sends **Not Found** response if none is matched.
- Calls the matching **repository** address with the original request
- Calls the **splitter** address with the template got from **repository**
- Builds [[KnotContext|Knot]] communication model (that consists of original request, response from 
repository & split HTML fragments)
- Calls **[[Knots|Knot]]** according to the [routing](#routing) configuration, with the **KnotContext**
- Once the last Knot returns processed **KnotContext**, server creates HTTP Response based on data from the KnotContext
- Filters the response headers according to the `allowed.response.headers` configuration and returns to the browser.

The diagram below depicts flow of data coordinated by the **Server** based on the hypothetical 
configuration of routing (as described in next section).
[[assets/knotx-server.png|alt=Knot.x Server How it Works flow diagram]]

### Routing
Routing specifies how the system should behave for different [Knots|Knot] responses. The request flow at 
the diagram above is reflected in a `routing` JSON node in the configuration section below. This routing 
defines that all requests for HTML pages must be processed first by Knot listening on address 
`first.knot.eventbus.address`. Then based on its response there are two next steps: `go-second` and
`go-alt`:
- If returned transition is `go-second`, Server will call next `second.knot.eventbus.address`.
- If returned transition is `go-alt`, Server will call next `alternate.knot.eventbus.address`.

For the route with `go-second` transition there is one more strep after `second.knot.eventbus.address` - 
for `go-third` transition Server will call `third.knots.eventbus.address` at the end.
For the route with `go-alt` transition Server will call `alternate.knot.eventbus.address` only.
In both cases the response will be returned to the client.

For more details please see [[Routing|Routing]] and [[Communication Flow|CommunicationFlow]] sections.

## How to configure?
Server is deployed using Vert.x service factory as a separate [verticle](http://vertx.io/docs/apidocs/io/vertx/core/Verticle.html) and it's shipped with default configuration.

Default configuration shipped with the verticle as `io.knotx.KnotxServer.json` file available in classpath.
```json
{
  "main": "io.knotx.server.KnotxServerVerticle",
  "options": {
    "config": {
      "httpPort": 8092,
      "displayExceptionDetails": true,
      "allowedResponseHeaders": [
        "Access-Control-Allow-Origin",
        "Allow",
        "Cache-Control",
        "Content-Disposition",
        "Content-Encoding",
        "Content-Language",
        "Content-Location",
        "Content-MD5",
        "Content-Range",
        "Content-Type",
        "Content-Length",
        "Content-Security-Policy",
        "Date",
        "ETag",
        "Expires",
        "Last-Modified",
        "Location",
        "Pragma",
        "Proxy-Authenticate",
        "Server",
        "Set-Cookie",
        "Status",
        "Vary",
        "Via",
        "X-Frame-Options",
        "X-XSS-Protection",
        "X-Content-Type-Options",
        "X-UA-Compatible",
        "X-Request-ID"
      ],
      "repositories": [
        {
          "path": "/content/local/.*",
          "address": "knotx.core.repository.filesystem"
        },
        {
          "path": "/content/.*",
          "address": "knotx.core.repository.http"
        }
      ],
      "splitter": {
        "address": "knotx.core.splitter"
      },
      "routing": {
        "GET": [
          {
            "path": "/content/.*",
            "address": "knotx.knot.service",
            "onTransition": {
              "next": {
                "address": "knotx.knot.handlebars"
              }
            }
          }
        ]
      }
    }
  }
}
```
In short, by default, server does:
- Listens on port 8092
- Displays exception details on error pages (for development purposes)
- Returns certain headers in Http Response to the client (as shown above)
- Communicates with two types of repositories: HTTP and Filesystem
- Uses core [Splitter|Splitter]
- Each GET request under `/content` path routed through [Service Knot|ServiceKnot] and then [Handlebars rendering engine|HandlebarsKnot]

Detailed description of each configuration option that's available is described in next section.

## Server options
Main server options available.

| Name                        | Type                                | Mandatory | Description  |
|-------:                     |:-------:                            |:-------:  |-------|
| `httpPort`                  | `Number (int)`                      | &#10004;       | HTTP Port on which Knot.x will listen for browser requests |
| `displayExceptionDetails`   | `Boolean`                           |                | (Debuging only) Displays exception stacktrace on error page. **False** if not set.|
| `allowedResponseHeaders`    | `Array of String`                   |                | Array of HTTP headers that are allowed to be send in response. **No** response headers are allowed if not set. |
| `repositories`              | `Array of RepositoryEntry`          | &#10004;       | Array of repositories configurations |
| `splitter`                  | `SplitterEntry`                     | &#10004;       | **Splitter** communication options |
| `routing`                   | `Object of Method to RoutingEntry`  | &#10004;       | Set of HTTP method based routing entries, describing communication between **Knots**<br/>`"routing": {"GET": {}, "POST": {}}` |

### RepositoryEntry options

| Name  | Type  | Mandatory | Description  |
|-------:|:-------:|:-------:  |-------|
| `path`      | `String`  | &#10004;       | Regular expression of the HTTP Request path |
| `address`   | `String`  | &#10004;       | Event bus address of the **Repository Connector** modules, that should deliver content for the requested path matching the regexp in `path` |

### SplitterEntry options

| Name  | Type  | Mandatory | Description  |
|-------:|:-------:|:-------:  |-------|
| `address`  | `String`  | &#10004;       | Sets the event bus address of the **Splitter** verticle |

### RoutingEntry options
| Name  | Type  | Mandatory | Description  |
|-------:|:-------:|:-------:  |-------|
| `path`           | `String`                               | &#10004;       | Regular expression of HTTP Request path |
| `address`        | `String`                               | &#10004;       | Event bus address of the **Knot** verticle, that should process the message, for the requested path matching the regexp in `path` |
| `onTransition`   | `Object of Strings to TransitionEntry` |        | Describes routing to addresses of other Knots based on the transition trigger returned from current Knot.<br/> `"onTransition": { "go-a": {}, "go-b": {} }` |

### KnotRouteEntry options
| Name  | Type  | Mandatory | Description  |
|-------:|:-------:|:-------:  |-------|
| `address`      | `String`         | &#10004;       | Event bus address of the **Knot** verticle |
| `onTransition` | `KnotRouteEntry` |        | Describes routing to addresses of other Knots based on the transition trigger returned from current Knot.<br/>`"onTransition": { "go-d": {}, "go-e": {} }` |

