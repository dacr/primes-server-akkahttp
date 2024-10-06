# ![](images/logo-base-32.png) Primes server ![tests][tests-workflow] [![License][licenseImg]][licenseLink] [![][PrimesServerAkkaMvnImg]][PrimesServerAkkaMvnLnk]

It has been deployed on https://mapland.fr/primes


## Quick local start

Thanks to [scala-cli][scl],
this application is quite easy to start, just execute :
```
scala-cli -S 2.13.15 --dep fr.janalyse::primes-server-akkahttp:1.0.7 -e 'primes.Main.main(args)'
```


## Configuration

| Environment variable | Description                                       | default value           |
|----------------------|---------------------------------------------------|-------------------------|
| PRIMES_LISTEN_IP     | Listening network interface                       | "0.0.0.0"               |
| PRIMES_LISTEN_PORT   | Listening port                                    | 8080                    |
| PRIMES_PREFIX        | Add a prefix to all defined routes                | ""                      |
| PRIMES_URL           | How this service is known from outside            | "http://127.0.0.1:8080" |
| PRIMES_STORE_PATH    | Where data is stored                              | "/tmp/primes-data"      |
| PRIMES_MAX_COUNT     | How many primes to compute in background (BigInt) | 500000000               |
| PRIMES_MAX_LIMIT     | Stop primes background compute after this value   | 9223372036854775807     |

[cs]: https://get-coursier.io/
[scl]: https://scala-cli.virtuslab.org/

[deployed]:   https://mapland.fr/primes
[primes-lib]:  https://github.com/dacr/primes
[akka-http]:  https://doc.akka.io/docs/akka-http/current/index.html

[PrimesServerAkka]:       https://github.com/dacr/primes-server-akkahttp
[PrimesServerAkkaMvnImg]: https://img.shields.io/maven-central/v/fr.janalyse/primes-server-akkahttp_2.13.svg
[PrimesServerAkkaMvnLnk]: https://search.maven.org/#search%7Cga%7C1%7Cfr.janalyse.primes-server-akkahttp

[tests-workflow]: https://github.com/dacr/primes-server-akkahttp/actions/workflows/scala.yml/badge.svg

[licenseImg]: https://img.shields.io/github/license/dacr/primes-server-akkahttp.svg
[licenseLink]: LICENSE
