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
* `WORK_DIR`: Working directory for TeX compilation; if empty, a temporary directory is created

## API

The service accepts ZIP archives containing LaTeX files in its root and compiles them into PDF documents. Up to eight
passes are performed to ensure that all references are resolved. After eight passes, compilation is aborted and the log
files are returned together with the PDF generated so far.

For LaTeX compilation, the file `main.tex` in the archive root is selected if it exists. If not, the first `.tex` file
encountered during archive processing is chosen.
Other files can be included as needed.

The response is sent to the `replyTo` queue specified in the request message and contains a ZIP archive with the
compiled PDF document, if compilation was successful, the log files generated during each pass, as well as a summary of
the compilation process in JSON format, stored in `META-INF/result.json`. If errors occur during processing, they can
be found in this result file.

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

If you miss a LaTeX package, which cannot be included in the request archive, or miss a language, please do not hesitate
to open an issue or PR.

## License

[MIT](LICENSE.txt) © 2025 Stefan Haun and contributors
