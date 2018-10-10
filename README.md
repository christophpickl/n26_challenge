# N26 Backend Senior Engineer Test

This is the enhanced solution to the one which was submitted to hackerrank.
"Enhanced" in such a way, that it finally supports the required constant execution time of `O(1)`,
whereas it had been a "close-to-constant" (linear) execution time so far.
Reason being, experienced has showed that non-functional requirements are sometimes somewhat theoretical,
and pragmatism and simplicity wins.

In order to verify the linear execution time, some "tricks" had to be introduced.

In order to get to constant execution time, complexity exploded by introducing multi-threading (thread pool size might be 
adjusted according to the real number of incoming requests, no JMX configured though, so restart of the server is necessary),
and therefor opened up the field for a lot of potential (and hard to discover) bugs!

Anyway, by using a blocking queue and a thread pool, the `POST /transactions` endpoint now executes in `O(1)` as required :)

## Design decisions

As stated above, for me personally simplicity is the key to success.
So I deliberately decided against introducing a lot of magic, as there was no need yet. Things like:

* **Profiles**: Use Spring profiles to configure the application differently (local setup)
* **Security**: Use Spring Security for authentication and authorization (statistics endpoint)
* **Logging**: Depending on the profile use console/file target, use a log annotation rather than do it manually, ...
* **API Documentation**: Use Swagger annotations and deploy together with application
* **CI/CD**: Configure Travis in order to execute tests on each push; deploy and run in some PaaS platform; dockernize the application
* **Monitoring**: Especially request frequency (memory/CPU) should be monitored, so to check whether the number of configured threads is enough
* **API Polishing**: Use custom exception response type and register a custom exception handler

Some of those topics already have been covered in another coding challenge: https://github.com/christophpickl/payconiq/
