# Traspaso al agente Dev de BMAD (Amelia)

**Fecha:** 2026-11-04 · **De:** Architect (Winston) · **Estado del diseño:** aprobado por el usuario el 2026-11-04.
**Alcance de esta entrega:** solo implementación de AC-1 a AC-6 y documentación obligatoria. No cambiar decisiones de arquitectura; si algo no cuadra, detenerse y consultar.

## Leer en este orden

1. `docs/analisis/Disruption Companion.pdf` — prevalece sobre todo lo demás.
2. `docs/diseno/01-arquitectura-propuesta.md` — AD-1 a AD-11, contrato de salida (§6), aprobación (§10).
3. `docs/diseno/02-plan-implementacion.md` — pasos 0 a 8, tests por AC, definición de «hecho», recortes.
4. `docs/analisis/analisis-funcional-v0.2.md` §3, §5, §8, §13 — hechos, reglas, criterios.
5. `docs/diseno/03-diseno-complementario.md` — solo diseño; no implementar nada de allí.

## Qué construir

Un servicio Spring Boot, un solo módulo Gradle, paquete base `com.latam.disruption` con las capas `domain`, `application`, `adapter.fixture`, `api` (árbol en arquitectura §6). Evaluador puro, lógica de tres valores, R1 a R5 explícitas, fixtures JSON del PDF tras 4 puertos de lectura.

## Decisiones cerradas (no reabrir)

| Tema | Decisión |
|---|---|
| Salida | `recommendationSupported` + `humanInterventionRequired` + motivos. R1 y R5 verdaderas ⇒ ambos `true` |
| Fuentes | un `ON_TIME` más reciente que el `DELAY` ⇒ vuelo incierto, aunque la fuente sea degradada; sin recomendación |
| Opciones vencidas | se listan como `REQUIRES_REVALIDATION` (vencida si `at >= retrievedAt + ttl`) |
| AC-2 | casos 179 / 180 / 200, más la variante de 200 min **sin menor** (`humanInterventionRequired=false`) |
| Instantes fijos | caso base `2026-09-15T11:03:00Z`; vencimiento `2026-09-15T11:04:35Z` |
| Escrituras | ninguna en código; solo diseño |
| Boot | 4.1.x (4.1.1). Si falla, 4.0.8 y registrarlo en la bitácora |

## Orden y prioridad

Pasos 0 a 5 del plan primero (proyecto, modelos, fuentes y conexión, reglas, opciones, evaluador). Después documentación obligatoria (paso 7) y bitácora (paso 8). El controller con manejo de fallos y AC-7 (paso 6) son **stretch**: solo si queda tiempo.

## Cosas que verificar, no asumir

| Qué | Cómo |
|---|---|
| Starters y API de test de Boot 4.1.x | paso 0: proyecto con un test vacío antes de escribir lógica. `@SpringBootTest` queda fuera (AD-11) |
| Versión de Boot | **no verificada en web** (búsqueda bloqueada). BOM 4.1.1 leído de la caché Gradle local: Spring Framework 7.0.9, Jackson 3.1.5 (`tools.jackson`), JUnit 6.0.3 |
| Frontera del TTL | 11:02:35Z + 120 s = 11:04:35Z; vencida **desde** ese instante |
| Cifras del caso base | llegada proyectada S1 = 20:35; salida S2 = 19:05; déficit 90 min |
| Ausencia de PII | test de AC-6 busca los emails, teléfono, documento y nombres del PDF en el JSON de salida |

## Límites duros

- Sin parser de condiciones, sin motor de reglas, sin persistencia, sin autenticación, sin IA en runtime.
- El dominio no importa `org.springframework.*` ni usa `Instant.now()`.
- Ningún dato personal en modelos de dominio, respuestas ni logs; solo IDs.
- Cada supuesto usado (S-1, S-3, S-8…) se documenta en el README, en «supuestos».

## Entregables al terminar

1. Código y tests AC-1 a AC-6 en verde (`./gradlew test`).
2. README de ingeniería ≤2 págs. y AI Usage & Decision Note ≤1 pág. (guardar en `docs/documentacion/`).
3. Bitácora de IA actualizada con la skill `ai-usage-log` (una entrada por etapa, sin inventar datos).
4. Notas de construcción y evidencia de tests en `docs/implementacion/`.
5. Lista de limitaciones conocidas.

## Pendiente del usuario

P-14: registrar el tiempo efectivo consumido. El PDF limita a 3 h incluyendo el análisis; el plan suma ~140 min de construcción y documentación.

## Cómo invocar

Pedir al agente Dev (`bmad-agent-dev`, Amelia) que implemente según este traspaso, o usar `bmad-build` con los documentos 01 y 02 como entrada.
