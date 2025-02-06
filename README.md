# SyncTexNG RabbitMQ Server

> This is another go on SyncTex, this time connecting via RabbitMQ.

The goal of this application is to take LaTex documents, as individual 
file or as a more complex archive of multiple files, and compile it 
into a PDF document, while using purely RabbitMQ for communication.

This application does not make any assumptions regarding the LaTeX content.
Generating specific files from templates etc. is left to calling agents.

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
