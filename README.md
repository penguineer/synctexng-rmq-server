# SyncTexNG RabbitMQ Server

> This is another go on SyncTex, this time connecting via RabbitMQ.

The goal of this application is to take LaTex documents, as individual 
file or as a more complex archive of multiple files, and compile it 
into a PDF document, while using purely RabbitMQ for communication.

This application does not make any assumptions regarding the LaTeX content.
Generating specific files from templates etc. is left to calling agents.
