# Plan de implementación acotado — Disruption Companion

**Estado:** aprobado por el usuario (2026-11-04) · **Fecha:** 2026-11-04 · **Autor:** agente Architect de BMAD (Winston)
**Depende de:** `01-arquitectura-propuesta.md` (los AD-n citados están allí).
**Tope:** el PDF fija 3 horas efectivas *incluyendo el análisis ya hecho*. El tiempo consumido hasta hoy **no está registrado** (P-14, lo registra el usuario). Las duraciones de abajo son estimaciones mías, no del PDF.

## Prioridad aprobada

Backend funcional, sus pruebas principales (AC-1 a AC-6) y la documentación obligatoria (README ≤2 pág., nota de IA ≤1 pág.). El controller con manejo de fallos y AC-7 son stretch. Spring Boot 4.1.x; si el paso 0 genera problemas, cambiar a 4.0.8 y registrarlo en la bitácora.

## Principios

- Test primero por AC; cada paso termina con sus tests en verde.
- Nada fuera de AC-1…AC-6 hasta que los seis pasen. AC-7 es stretch.
- Si el tiempo se acaba, se recorta de abajo hacia arriba (ver «Recortes»).

## Pasos

| # | Paso | Entregable | AC | Est. |
|---|---|---|---|---|
| 0 | Crear proyecto Gradle con Boot 4.1.x, Java 21, JUnit/AssertJ. Comprobar que `./gradlew test` corre con un test vacío y qué starters existen en Boot 4 | proyecto que compila | — | 10 min |
| 1 | Modelos `record` de dominio, `Tri`, puertos, fixtures JSON del PDF y adaptador (sin campos PII) | modelos + fixtures + adaptador | AC-6 (parte PII) | 20 min |
| 2 | `FlightStateResolver` + `ConnectionAnalyzer` | estado por segmento, descartes con motivo, conexión `AT_RISK` | AC-3, AC-1 | 20 min |
| 3 | `RuleEvaluator` R1–R5 tri-valor + derechos y dedupe | reglas con frontera 179/180 | AC-2, AC-1 | 20 min |
| 4 | `OptionEvaluator` (grupo, TTL, REFUND, `policy_ref`, costo por confirmar) | estados de opción | AC-4, AC-5 | 15 min |
| 5 | `DisruptionEvaluator` + servicio: combina todo, calcula los dos booleanos y los motivos | `Evaluation` completa | AC-1, AC-6 | 15 min |
| 6 | Controller + `ProblemDetail` + manejo de fallos (AD-8) | endpoint en marcha | AC-7 (stretch) | 10 min |
| 7 | Documentación: README ≤2 pág., nota de IA ≤1 pág., clasificación | entregables 2 y 3 del PDF | — | 25 min |
| 8 | Bitácora de IA al día | `docs/documentacion/bitacora-ia.md` | — | 5 min |

Total estimado ≈ 140 min de construcción y documentación, sin contar lo ya consumido en análisis y diseño. **Con el presupuesto de 3 h es ajustado**: confirma cuánto tiempo llevas antes de empezar.

## Tests (uno o más por AC)

Todos con instante fijo explícito; datos del PDF salvo donde se indica «sintético».

| AC | Test | Aserción clave |
|---|---|---|
| AC-1 | caso base, `at=2026-09-15T11:03:00Z` | R1 F, R2 F, R3 UNKNOWN, R4 F, R5 T `PENDING_APPROVAL`; conexión `AT_RISK` déficit 90; O1 `CANDIDATE`, O2 `NOT_SUITABLE_GROUP`, O3 `NOT_VERIFIABLE`; `recommendationSupported=false`; `humanInterventionRequired=true`; `actionsExecuted` vacío |
| AC-2 | parametrizado 179 / 180 / 200 (sintético) | 179 → R1 F; 180 y 200 → R1 T `CONFIRMED`; variante 200 sin menor → `supported=true`, `humanInterventionRequired=false`; variante 200 con menor → ambos `true` (aprobado) |
| AC-3 | sintético: `ON_TIME` de `LEGACY_MIRROR` posterior al `DELAY` | segmento `UNCERTAIN`; `supported=false`; el evento figura en `discardedEvents` con motivo. Control: el caso base descarta E-1012 con `DEGRADED_AND_OLDER` |
| AC-4 | dos pasajeros, O2 con 1 asiento | O2 `NOT_SUITABLE_GROUP`; con un solo pasajero pasa a `CANDIDATE` |
| AC-5 | `at=11:04:34Z` vs `11:04:35Z` | antes: O1 `CANDIDATE`; desde: `REQUIRES_REVALIDATION` |
| AC-6 | caso base serializado a JSON | contiene `POL-DELAY-2026-03`, IDs de regla y de evento, las 4 marcas `retrievedAt`; **no contiene** `maria.silva@example.com`, `+56 9 8765 4321`, `12.345.678-9` ni los nombres |
| AC-7 (stretch) | puerto de eventos lanza excepción | 503 `ProblemDetail`, sin cuerpo de evaluación |

Cuidado con el borde del TTL: 11:02:35Z + 120 s = **11:04:35Z**, y la opción está vencida **desde** ese instante (`at >= validUntil`).

## Definición de «hecho»

1. `./gradlew test` en verde con los tests AC-1…AC-6.
2. `./gradlew bootRun` + `curl` al caso base devuelve la salida de §6 de la arquitectura.
3. `grep` de los datos personales del PDF en `src/main` y en la respuesta: sin coincidencias (salvo los fixtures de entrada, que los traen y el adaptador descarta).
4. README, nota de IA y bitácora actualizados; limitaciones conocidas listadas.

## Recortes si falta tiempo (de último a primero)

1. AC-7 y el controller con manejo de fallos (paso 6): dejar solo el servicio invocable por test.
2. Pasos 7–8 se acortan, no se omiten: son entregables del PDF.
3. **No se recorta** el orden de AC-1 → AC-3: son los críticos.

## Riesgos de la ejecución

| Riesgo | Mitigación |
|---|---|
| Boot 4.1.x sin verificar en web; los starters cambiaron respecto de 3.x | paso 0 con test vacío antes de escribir lógica; plan B Boot 4.0.8 (también en tu caché) |
| Interpretación del AD-4 distinta a la del PM | está en el punto 1 de aprobación; el cambio afecta una función |
| Tiempo ya consumido desconocido | registrar P-14 antes de empezar |
| Asumir como hecho lo que es supuesto (S-1, S-3) | cada supuesto va al README en «supuestos» |

## Handoff

Tras aprobar este plan: agente Dev de BMAD (`bmad-agent-dev`) o `bmad-build` con `01-arquitectura-propuesta.md` y este plan como entrada.
