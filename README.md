# Jetty / Unix domain socket

Demonstration of Jetty / JNR slowness when using Unix domain sockets.

There are three components, each run as a separate container:

- Jetty server - serves up 200s on a Unix domain socket
- Envoy - proxies all request on a TCP port to the domain socket
- Load test - loadtest binary that will issue load against the Envoy over HTTP
  over a TCP socket

Build each of the containers:

```bash
# Server
$ docker build -t nicktrav/jetty-unix-socket-server -f Dockerfile.server .

# Envoy
$ docker build -t nicktrav/jetty-unix-socket-envoy -f Dockerfile.envoy .

# Load test
$ docker build -t nicktrav/jetty-unix-socket-loadtest -f Dockerfile.loadtest .
```

Create a Docker network for the test:

```bash
$ docker network create jetty-test
```

Run the server and Envoy containers:

```bash
# Server
$ docker run --rm -it \
  -v /tmp:/tmp \
  --network jetty-test \
  nicktrav/jetty-unix-socket-server

# Envoy
$ docker run --rm -it \
  --name envoy \
  -v /tmp:/tmp \
  --network jetty-test \
  nicktrav/jetty-unix-socket-envoy
```

Issue load against the Envoy, which will be proxied to the Jetty server:

```bash
# 500 qps
$ docker run --rm -it \
  --network jetty-test \
  nicktrav/jetty-unix-socket-loadtest 500
Requests      [total, rate]            500, 500.73
Duration      [total, attack, wait]    999.868736ms, 998.534392ms, 1.334344ms
Latencies     [mean, 50, 95, 99, max]  23.526471ms, 999.216µs, 194.216648ms, 239.675442ms, 275.039583ms
Bytes In      [total, mean]            0, 0.00
Bytes Out     [total, mean]            0, 0.00
Success       [ratio]                  89.20%
Status Codes  [code:count]             200:446  503:54
Error Set:
503 Service Unavailable
```

Observe the failure rate:

```
Success       [ratio]                  89.20%
```

Reboot the server with a larger socket accept queue size:

```bash
$ docker run --rm -it \
  -v /tmp:/tmp \
  --network jetty-test \
  nicktrav/jetty-unix-socket-server 65536
```

Run the load test again, and observe no failures:

```bash
$ docker run --rm -it \
  --network jetty-test \
  nicktrav/jetty-unix-socket-loadtest 500
Requests      [total, rate]            500, 500.90
Duration      [total, attack, wait]    999.106071ms, 998.197047ms, 909.024µs
Latencies     [mean, 50, 95, 99, max]  95.934883ms, 2.086756ms, 374.556165ms, 431.97388ms, 452.879507ms
Bytes In      [total, mean]            0, 0.00
Bytes Out     [total, mean]            0, 0.00
Success       [ratio]                  100.00%
Status Codes  [code:count]             200:500
Error Set:
```
