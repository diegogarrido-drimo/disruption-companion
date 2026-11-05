# Notas de construcción y evidencia — Disruption Companion

**Fecha:** 2026-11-04 · **Agente:** Dev de BMAD (Amelia) · **Código:** `disruption-companion/`

## Qué se construyó

| Paso del plan | Resultado |
|---|---|
| 0 Proyecto | Gradle 9.2.1 (wrapper), Java 21, Spring Boot **4.1.1** resolvió sin problemas: no se usó el plan B 4.0.8. Starters usados: `spring-boot-starter-web`, `spring-boot-starter-test` (ambos existen en 4.1.1) |
| 1 Modelos, puertos, fixtures | `domain` (records, `Tri`), 4 puertos en `application`, 4 JSON en `src/main/resources/fixtures/abc123/`, `adapter.fixture` sin campos personales |
| 2 Fuentes y conexión | `FlightStateResolver`, `ConnectionAnalyzer` |
| 3 Reglas | `RuleEvaluator` R1–R5 explícitas, derechos deduplicados |
| 4 Opciones | `OptionEvaluator` (orden de AD-7) |
| 5 Evaluador | `DisruptionEvaluator` + `DisruptionEvaluationService` |
| 6 (stretch) | Controller, `ProblemDetail`, 503 por dependencia caída (solo las dos filas bloqueantes de AD-8) |

## Evidencia

`./gradlew test --rerun-tasks`: **35 pruebas, 35 en verde, 0 fallos**. Sin `@SpringBootTest` (AD-11); el HTTP usa `MockMvcBuilders.standaloneSetup`.

| AC | Clase de test |
|---|---|
| AC-1 | `BaseCaseTest` |
| AC-2 | `DelayRuleTest` (179/180/200, variante sin menor, R2 239/240, condición alterada) |
| AC-3 | `ConflictingSourcesTest` |
| AC-4 | `GroupOptionsTest` |
| AC-5 | `OptionExpiryTest` (11:04:34Z vs 11:04:35Z) |
| AC-6 | `TraceabilityAndPrivacyTest` |
| AC-7 (parcial) | `DependencyFailureTest` |

Definición de «hecho» (plan): `bootRun` + `curl` con `at=2026-09-15T11:03:00Z` devolvió la salida de arquitectura §6 (con `non_null`, sin campos vacíos). `at` mal formado → 400; `bookingId` distinto de `ABC123` → 404 `BOOKING_NOT_FOUND`. En la respuesta: 0 coincidencias de nombres, emails, teléfono, documento o `GOLD`. En `src/main` fuera de `fixtures/`: 0 coincidencias.

## Desviaciones y decisiones del Dev

Aprobadas por el usuario el 2026-11-04 como supuestos de la primera versión (bitácora D-40 a D-45). Las limitaciones de cada una están en la bitácora.

| # | Qué | Por qué |
|---|---|---|
| 1 | Se implementó el controller (paso 6) aunque era stretch | La definición de «hecho» exige `bootRun` + `curl`; es una clase fina |
| 2 | Un `DELAY` solo de fuente degradada deja el segmento `UNCERTAIN` (`DEGRADED_DELAY_ONLY`) | AD-5 habla de DELAY de fuentes sanas; sin esta fila se habría dicho «sin retraso» ante un retraso conocido. Supuesto mío |
| 3 | `STATUS` con valor desconocido → `UNCERTAIN` | No inventar «a tiempo» |
| 4 | Opción sin `seats_available` o con `policy_ref` distinto → `NOT_VERIFIABLE` | AD-7 y casos borde de `03` §5 |
| 5 | R3 combina conexiones: `UNKNOWN` si alguna no es `NO_RISK`; `FALSE` si todas son `NO_RISK` | AD-2 no cubre el caso de varias conexiones ni de estado incierto |
| 6 | Política cuya `version` figura en `superseded_versions` → todas las reglas `UNKNOWN` | `03` §5; sin test dedicado |
| 7 | Un segmento posterior con retraso propio entra en el cálculo de la conexión | Generalización de AD-6, sin efecto en el caso base |
| 8 | `ConnectionAnalyzer` devuelve `UNDETERMINED` si algún segmento es incierto | No proyectar sin estado resuelto |

## Limitaciones conocidas

1. Solo existe el adaptador de fixtures del booking `ABC123`; cualquier otro `bookingId` devuelve 404 `BOOKING_NOT_FOUND` (D-46). Un adaptador real deberá lanzar `BookingNotFoundException`.
2. AD-8: las filas «política caída» y «opciones caídas» (200 con evaluación parcial) **no** están implementadas y quedan documentadas como pendientes por decisión del usuario (no se amplía AD-8); ningún adaptador lanza `DependencyUnavailableException` hoy, solo se prueba con un puerto simulado.
3. Sin tiempo mínimo de conexión, sin «conexión perdida» explícita, sin umbral de frescura (el PDF no los define); S-1 (un solo marco horario) y S-3 (retraso se propaga a llegada).
4. Retrasos que cruzan la medianoche no tienen test.
5. AC-8 y AC-9 tienen cobertura parcial por efecto de otras pruebas; AC-10 se cubre con `sameInputGivesSameOutput`. No son criterios formales de esta entrega.
6. Boot 4.1.1 no se verificó contra fuentes web (búsqueda bloqueada); solo se comprobó que resuelve y compila.
7. El escenario de 200 min es sintético: solo se prueba con `DelayRuleTest`, no por HTTP (los fixtures son fijos).
8. Tiempo efectivo consumido (P-14): lo registra el usuario.
