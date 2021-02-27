# ![](images/logo-base-32.png) Primes server [![][PrimesServerAkkaMvnImg]][PrimesServerAkkaMvnLnk]

## Quick local start

Thanks to [coursier][cs] from @alxarchambault,
this application is quite easy, just execute :
```
cs launch fr.janalyse::primes-server-akkahttp:1.0.0
```

## Configuration

| Environment variable | Description                                       | default value
| -------------------- | ------------------------------------------------- | -----------------
| PRIMES_LISTEN_IP     | Listening network interface                       | "0.0.0.0"
| PRIMES_LISTEN_PORT   | Listening port                                    | 8080
| PRIMES_PREFIX        | Add a prefix to all defined routes                | ""
| PRIMES_URL           | How this service is known from outside            | "http://127.0.0.1:8080"
| PRIMES_STORE_PATH    | Where data is stored                              | "/tmp/primes-data"
| PRIMES_MAX_COUNT     | How many primes to compute in background (BigInt) | 5000000

[cs]: https://get-coursier.io/

[deployed]:   https://mapland.fr/primes
[primes-lib]:  https://github.com/dacr/primes
[akka-http]:  https://doc.akka.io/docs/akka-http/current/index.html

[PrimesServerAkka]:       https://github.com/dacr/primes-server-akkahttp
[PrimesServerAkkaMvnImg]: https://img.shields.io/maven-central/v/fr.janalyse/primes-server-akkahttp_2.13.svg
[PrimesServerAkkaMvnLnk]: https://search.maven.org/#search%7Cga%7C1%7Cfr.janalyse.primes-server-akkahttp
