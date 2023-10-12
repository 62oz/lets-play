#!/bin/bash

# Build the Docker image
docker build -t lets-play .

# Remove the old container if it exists
docker rm -f lets-play-container || true

# Run a new container from the built image
docker run --name lets-play-container -p 443:443 -d lets-play
