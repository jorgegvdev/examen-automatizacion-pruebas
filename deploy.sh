#!/bin/bash
set -e

IMAGEN="tareas-app"
CONTENEDOR="tareas-staging"
PUERTO=8080

echo "== 1. Build de la imagen candidata =="
docker build -t ${IMAGEN}:candidate .

echo "== 2. Deteniendo contenedor anterior (si existe) =="
docker stop ${CONTENEDOR} 2>/dev/null || true
docker rm ${CONTENEDOR} 2>/dev/null || true

echo "== 3. Desplegando la versión candidata al ambiente de prueba =="
docker run -d --name ${CONTENEDOR} -p ${PUERTO}:8080 ${IMAGEN}:candidate

echo "== 4. Esperando a que el servicio esté disponible =="
for i in {1..10}; do
    if curl -sf http://localhost:${PUERTO}/health > /dev/null; then
        echo "Servicio disponible."
        break
    fi
    sleep 1
done

echo "== 5. Ejecutando acceptance tests =="
if ./acceptance-test.sh; then
    echo "== Acceptance tests OK — promoviendo candidate a stable =="
    docker tag ${IMAGEN}:candidate ${IMAGEN}:stable
    echo "Despliegue exitoso. La versión candidata es ahora la versión estable."
else
    echo "== Acceptance tests FALLARON — iniciando rollback =="
    ./rollback.sh
    exit 1
fi
