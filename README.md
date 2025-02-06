# SyncTexNG RabbitMQ Server

> This is another go on SyncTex, this time connecting via RabbitMQ.

The goal of this application is to take LaTex documents, as individual 
file or as a more complex archive of multiple files, and compile it 
into a PDF document, while using purely RabbitMQ for communication.

This application does not make any assumptions regarding the LaTeX content.
Generating specific files from templates etc. is left to calling agents.

## Configuration

Configuration is done using environment variables:

* `PORT`: Port for the HTTP endpoint (default `8080`, only change when running locally!)

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
