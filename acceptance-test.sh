#!/bin/bash
BASE_URL="http://localhost:8080"

echo "Acceptance test 1: health check"
curl -sf ${BASE_URL}/health | grep -q '"status":"UP"' || { echo "FALLÓ health check"; exit 1; }

echo "Acceptance test 2: crear tarea"
RESPUESTA=$(curl -sf -X POST ${BASE_URL}/tareas \
    -H "Content-Type: application/json" \
    -d '{"id":"1","titulo":"Validar despliegue"}')
echo "$RESPUESTA" | grep -q '"titulo":"Validar despliegue"' || { echo "FALLÓ crear tarea"; exit 1; }

echo "Acceptance test 3: listar tareas"
curl -sf ${BASE_URL}/tareas | grep -q '"id":"1"' || { echo "FALLÓ listar tareas"; exit 1; }

echo "Acceptance test 4: completar tarea"
curl -sf -X POST ${BASE_URL}/tareas/1/completar | grep -q '"completada":true' || { echo "FALLÓ completar tarea"; exit 1; }

echo "Todos los acceptance tests pasaron."
exit 0
