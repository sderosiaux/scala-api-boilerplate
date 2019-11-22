# What

The API is using `.env` to read and set environment variables (dev mode).

```
> ~reStart
```

Use SwaggerUI available at: http://localhost:8000/index.html?url=/openapi.yaml

# Watch for

- Setup scala-steward
    - Add [mergify](https://mergify.io/) to merge its PR ([example](https://github.com/softwaremill/tapir/blob/master/.mergify.yml))
- `zio-shield` commented in `plugins.sbt`
- Switch to `zio-config` when ready

# IntelliJ

- Install https://github.com/zio/zio-intellij