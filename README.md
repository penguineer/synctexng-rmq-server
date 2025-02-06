# SyncTexNG RabbitMQ Server

> Yet another approach to SyncTex, leveraging RabbitMQ for communication.

This application processes LaTeX documents, either as individual files or complex archives, and compiles them into PDF
documents using RabbitMQ for communication.

It does not impose any constraints on the LaTeX content. Tasks such as generating specific files from templates are
left to the calling agents.

## Configuration

Configuration is done using environment variables:

* `PORT`: Port for the HTTP endpoint (default `8080`, only change when running locally!)
* `RMQ_HOST`: Host for RabbitMQ (default `localhost`)
* `RMQ_PORT`: Port for RabbitMQ (default `5672`)
* `RMQ_USER`: Username for RabbitMQ (default `guest`)
* `RMQ_PASSWORD`: Password for RabbitMQ (default `guest`)
* `RMQ_VHOST`: Virtual host for RabbitMQ (default `/`)
* `RMQ_QUEUE_TEX_REQUESTS`: Queue for TeX requests (default `tex-requests`)

## Run with Docker

With the configuration stored in a file `.env`, the service can be run as follows:

```bash
docker run --rm \
           -p 8080:8080 \
           --env-file .env \
           mrtux/synctexng-rmq-server:latest
```

Please make sure to pin the container to a specific version in a production environment.

## Development

Version numbers are determined with [jgitver](https://jgitver.github.io/).
If you encounter a project version `0` there is an issue with the jgitver generator.

## Maintainers

* Stefan Haun ([@penguineer](https://github.com/penguineer))

## Contributing

PRs are welcome!

If possible, please stick to the following guidelines:

* Keep PRs reasonably small and their scope limited to a feature or module within the code.
* If a large change is planned, it is best to open a feature request issue first, then link subsequent PRs to this
  issue, so that the PRs move the code towards the intended feature.

## License

[MIT](LICENSE.txt) © 2025 Stefan Haun and contributors
