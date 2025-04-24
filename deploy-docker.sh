#!/bin/bash

DOCKER_USERNAME="akinaksoy"
IMAGE_NAME="repsy-package-manager"
TAG="latest"


DOCKER_IMAGE="${DOCKER_USERNAME}/${IMAGE_NAME}:${TAG}"


echo "Building Docker image: ${DOCKER_IMAGE}..."
docker build -t ${DOCKER_IMAGE} .


if [ $? -ne 0 ]; then
    echo "Docker build failed. Aborting."
    exit 1
fi


echo "Logging in to Docker Hub..."
echo "Please enter your Docker Hub credentials when prompted."
docker login


if [ $? -ne 0 ]; then
    echo "Docker Hub login failed. Aborting."
    exit 1
fi

echo "Pushing image to Docker Hub: ${DOCKER_IMAGE}..."
docker push ${DOCKER_IMAGE}


if [ $? -ne 0 ]; then
    echo "Docker push failed. Aborting."
    exit 1
fi

echo "Docker image successfully pushed to Docker Hub: ${DOCKER_IMAGE}"