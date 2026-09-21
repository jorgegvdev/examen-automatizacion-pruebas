# Examen — Automatización de Pruebas

**Autor:** Jorge Ignacio González Villagrán
**Repositorio:** [examen-automatizacion-pruebas](https://github.com/jorgegvdev/examen-automatizacion-pruebas)

## Objetivo

Implementar un flujo completo de automatización de pruebas y entrega continua sobre un proyecto Java: control de versiones con una estrategia de ramas formal, pruebas automatizadas de dos tipos (unitarias y de integración), un pipeline de integración continua, y un pipeline de despliegue a un ambiente de prueba con mecanismo de rollback.

## Stack técnico

- **Lenguaje:** Java 25 (LTS)
- **Gestor de dependencias:** Maven
- **Pruebas:** JUnit 5 (Jupiter) + Mockito 5.23.0
- **CI/CD:** GitHub Actions
- **Contenedores:** Docker (build multi-etapa)
- **Servidor HTTP:** `com.sun.net.httpserver` (JDK nativo, sin frameworks)

## Estrategia de ramas: Trunk-Based Development

Se optó por Trunk-Based Development en lugar de GitFlow por ser la estrategia más adecuada al tamaño y ciclo de vida del proyecto. `main` se mantiene siempre en un estado desplegable; cada cambio se desarrolla en una rama corta (`feature/...`), se valida mediante el pipeline de CI, y se integra a `main` vía Pull Request. No existen ramas de larga duración como `develop` o `release`.

## Estructura del proyecto

```
examen-automatizacion-pruebas/
├── .github/
│   └── workflows/
│       └── ci.yml                          # Pipeline de CI (build + test)
├── src/
│   ├── main/java/org/taller/
│   │   ├── Tarea.java                       # Entidad de dominio
│   │   ├── TareaRepository.java             # Interfaz del repositorio
│   │   ├── InMemoryTareaRepository.java     # Implementación en memoria
│   │   ├── TareaService.java                # Lógica de negocio
│   │   └── TareaHttpServer.java             # Servidor HTTP (Actividad 3)
│   └── test/java/org/taller/
│       ├── TareaServiceTest.java            # Pruebas unitarias (Mockito)
│       └── TareaServiceIntegrationTest.java # Pruebas de integración
├── Dockerfile                               # Build multi-etapa
├── deploy.sh                                # Despliegue + acceptance tests
├── acceptance-test.sh                       # Validación del contenedor desplegado
├── rollback.sh                              # Rollback a la última versión stable
├── .gitignore
├── pom.xml
└── README.md
```

## Actividad 1 — Repositorio y arquitectura del dominio

Se inicializó el repositorio con la estructura base del proyecto (dominio de tareas) en un único commit inicial sobre `main`, dado que se trataba del punto de partida sin historial previo que fragmentar.

Se separó explícitamente la interfaz del repositorio (`TareaRepository`) de su implementación (`InMemoryTareaRepository`). Esta decisión de diseño no es incidental: es la que permite, en la Actividad 2, pruebas unitarias con un repositorio simulado (Mockito) y pruebas de integración con el repositorio real, sin modificar `TareaService`.

**Nota sobre Selenium:** el enunciado lo menciona como ejemplo de dependencia de testing, no como requisito obligatorio. Al no existir una interfaz web navegable en este proyecto, no aplica; la cobertura de integración se resuelve validando la colaboración real entre `TareaService` y su repositorio.

## Actividad 2 — Pruebas automatizadas y pipeline de CI

### Pruebas unitarias (`TareaServiceTest`)
Aíslan `TareaService` mediante un mock de `TareaRepository` (Mockito), validando la lógica de negocio y el manejo de errores sin depender de una implementación real de persistencia.

### Pruebas de integración (`TareaServiceIntegrationTest`)
Usan `InMemoryTareaRepository` real, sin mocks, verificando que el servicio y el repositorio colaboran correctamente de punta a punta.

### Incidente resuelto: Mockito vs. Java 25
La versión inicial de Mockito (5.16.1) incluía una versión de Byte Buddy sin soporte para el bytecode de Java 25, causando fallos en las 4 pruebas unitarias con `MockitoException`. Se resolvió actualizando a Mockito 5.23.0. El fix se aplicó sobre la rama de feature correspondiente, sin tocar `main` directamente.

### Pipeline de CI (`.github/workflows/ci.yml`)
Con stages explícitamente separados:
- **Build:** `mvn -B compile`
- **Test:** `mvn -B test` (ejecuta ambas suites)
- Publicación del reporte de resultados vía `dorny/test-reporter`

## Actividad 3 — Despliegue a ambiente de prueba con rollback

### Servidor HTTP
`TareaHttpServer` expone `TareaService` mediante tres endpoints (`POST /tareas`, `GET /tareas`, `POST /tareas/{id}/completar`) más un endpoint de salud (`/health`).

### Imagen Docker
Build multi-etapa: la primera etapa compila el proyecto con Maven; la segunda copia únicamente el jar ejecutable sobre una imagen JRE liviana, sin herramientas de build en la imagen final.

### Despliegue con acceptance tests y rollback automático
`deploy.sh` construye la imagen candidata, la despliega como contenedor de staging, ejecuta `acceptance-test.sh` contra el contenedor real, y solo promueve la imagen a `stable` si todos los tests pasan. Si algún test falla, invoca `rollback.sh` automáticamente, que restaura el último contenedor `stable` conocido.

**El mecanismo se validó con un fallo real:** se introdujo un defecto intencional en el endpoint de completar tarea, se desplegó, el acceptance test lo detectó, y el rollback se disparó automáticamente — verificado además con peticiones HTTP directas al contenedor tras el rollback, confirmando que la lógica correcta quedó activa.

## Comandos utilizados (resumen)

```bash
# Actividad 1
git init
git add .
git commit -m "feat: agrega dominio de tareas con repositorio y servicio"
git push -u origin main

# Actividad 2
git checkout -b feature/pruebas-tareas
mvn clean test
git add . && git commit -m "test: agrega pruebas unitarias con Mockito y pruebas de integración"
git add . && git commit -m "ci: agrega pipeline con stages de build y pruebas automatizadas"
git add pom.xml && git commit -m "fix: actualiza Mockito a 5.23.0 para soportar Java 25"
git push -u origin feature/pruebas-tareas
# PR feature/pruebas-tareas → main, mergeado en GitHub

# Actividad 3
git checkout main && git pull origin main
git checkout -b feature/deploy-docker
chmod +x deploy.sh acceptance-test.sh rollback.sh
./deploy.sh
git add . && git commit -m "feat: agrega servidor HTTP, Dockerfile y scripts de despliegue con rollback"
git push -u origin feature/deploy-docker
# PR feature/deploy-docker → main, mergeado en GitHub
```
