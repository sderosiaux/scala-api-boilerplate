# scala-api-boilerplate [![Build Status](https://travis-ci.org/sderosiaux/scala-api-boilerplate.svg?branch=master)](https://travis-ci.org/sderosiaux/scala-api-boilerplate)

# What

```
> ~reStart
```

- Use SwaggerUI available at: http://localhost:8000/index.html?url=/openapi.yaml
- Use `application.conf` and `.env` for configuration

# To add

- Tracing (http4s server and client)
- Healthcheck
- Metrics (prometheus)

# To be done after fork

- Setup scala-steward
    - Add [mergify](https://mergify.io/) to merge its PR ([example](https://github.com/softwaremill/tapir/blob/master/.mergify.yml))

# To be done one day

- `zio-shield` commented out in `plugins.sbt`
- `zio-logging` commented out in `build.sbt` 
- Switch to `zio-config` when ready

# IntelliJ

- Install https://github.com/zio/zio-intellij
