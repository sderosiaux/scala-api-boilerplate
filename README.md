# scala-tapir-http4s-zio [![Build Status](https://travis-ci.org/sderosiaux/scala-tapir-http4s-zio.svg?branch=master)](https://travis-ci.org/sderosiaux/scala-tapir-http4s-zio)

# What

The API is using `.env` to read and set environment variables (dev mode).

```
> ~reStart
```

Use SwaggerUI available at: http://localhost:8000/index.html?url=/openapi.yaml

# To be done

- Setup scala-steward
    - Add [mergify](https://mergify.io/) to merge its PR ([example](https://github.com/softwaremill/tapir/blob/master/.mergify.yml))
- `zio-shield` commented out in `plugins.sbt`
- `zio-logging` commented out in `build.sbt` 
- Switch to `zio-config` when ready

# IntelliJ

- Install https://github.com/zio/zio-intellij