#!/bin/bash
set -e

IMAGEN="tareas-app"
CONTENEDOR="tareas-staging"
PUERTO=8080

echo "== Rollback: deteniendo versión fallida =="
docker stop ${CONTENEDOR} 2>/dev/null || true
docker rm ${CONTENEDOR} 2>/dev/null || true

if docker image inspect ${IMAGEN}:stable > /dev/null 2>&1; then
    echo "== Restaurando última versión estable conocida =="
    docker run -d --name ${CONTENEDOR} -p ${PUERTO}:8080 ${IMAGEN}:stable
    echo "Rollback completado. El ambiente de prueba corre la última versión estable."
else
    echo "No existe una versión stable previa — no hay a qué hacer rollback."
    exit 1
fi
