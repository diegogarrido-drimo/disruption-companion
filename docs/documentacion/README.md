# Disruption Companion — README de ingeniería

**Track: Backend/Integración** (Java 21, Spring Boot 4.1.1, Gradle). El valor del caso está en decidir con exactitud qué corresponde al pasajero y en poder justificarlo. Eso se demuestra y se prueba mejor en lógica de backend que en pantalla.

## Problema y resultado esperado
Ante un cambio en el vuelo, el pasajero debe saber qué pasó y qué sigue, sin información no verificada y sin acciones de alto impacto sin control. El servicio recibe una reserva y un instante de referencia, lee las 4 consultas del PDF, evalúa las reglas R1–R5 y devuelve una evaluación con motivos y evidencia. Solo lee: `actionsExecuted` es siempre `[]`.

## Cómo revisarlo
```bash
cd disruption-companion
./gradlew test --rerun-tasks     # 35 pruebas
./gradlew bootRun
curl "localhost:8080/api/v1/bookings/ABC123/disruption-evaluation?at=2026-09-15T11:03:00Z"
```
Sin `at` usa el reloj del sistema, y con él las opciones del PDF ya están vencidas. Un `bookingId` distinto de `ABC123` devuelve 404 `BOOKING_NOT_FOUND`.

## Alcance
- **Implementado:** AC-1 a AC-6 (`docs/analisis/analisis-funcional-v0.2.md` §13), endpoint de lectura, 404 por reserva inexistente y 503 por dependencia caída (probado con un puerto simulado, porque el adaptador de fixtures nunca falla).
- **Solo diseño** (`docs/diseno/03-diseno-complementario.md`): consulta ambigua, mensaje al pasajero, escrituras.
- **No existe:** persistencia, frontend, autenticación, IA en runtime ni integraciones reales. Los 4 puertos leen fixtures JSON del PDF, solo de la reserva `ABC123`.

**Caso base** (`at=2026-09-15T11:03:00Z`): `recommendationSupported=false` y `humanInterventionRequired=true`. R1 y R2 son falsas (160 < 180). R3 es desconocida: la conexión está en riesgo (llegada proyectada 20:35, salida 19:05, déficit 90 min) pero el PDF no dice que se haya perdido. R5 es verdadera, con aprobación humana pendiente. O1 es candidata con costo por confirmar, O2 no sirve para el grupo (1 asiento) y O3 no es verificable. E-1012 se descarta por degradado y más antiguo.

## Supuestos
S-1 un solo marco horario · S-3 el retraso de salida se propaga a la llegada · S-4 `GATE_CHANGE` es informativo · S-5 (ampliado por AD-5/D-35) un `ON_TIME` más antiguo que el `DELAY` se descarta con motivo, sea de fuente sana o degradada; uno más reciente, de cualquiera de las dos, deja el segmento incierto y sin recomendación · S-6 menor = `CHILD` o edad < 18 · S-7 `compensation_requested` es entrada, falsa por defecto · S-8 «internacional» se deriva de una tabla de aeropuertos (`application.yml`) · S-9 no se separa al grupo.
Supuestos del desarrollo (D-40 a D-45 de la bitácora): un retraso visto solo en fuente degradada o un estado desconocido dejan el segmento incierto; R3 con varias conexiones; versión de política superada → reglas desconocidas; opción sin asientos informados → no verificable. No tienen todos prueba dedicada (`docs/implementacion/notas-construccion.md`).

## Decisiones y alternativas descartadas
| Decisión | Descartado y por qué |
|---|---|
| Núcleo puro; el instante entra como parámetro | Hexagonal completo: más archivos que valor. Un reloj dentro del dominio rompería la reproducibilidad |
| R1–R5 como código explícito, lógica de tres valores | Parser de condiciones o motor de reglas: 5 reglas no lo justifican. Un `false` por defecto ocultaría datos faltantes |
| Condición distinta de la conocida o política superada → regla `UNKNOWN`; `policy_ref` ajeno → opción `NOT_VERIFIABLE` | Confiar en el texto recuperado |
| Dos booleanos más motivos | Enum de estados: se pidió una salida simple |
| Contradicción de fuentes → vuelo incierto, aunque el evento sea degradado | Dejar que el evento más reciente decida solo |
| Pruebas sin `@SpringBootTest` | Más lento, y la API de test de Boot 4 no estaba verificada |
| Fixtures tras puertos | Datos fijos dentro del servicio; WireMock |

## Qué quedó determinístico, AI-assisted, agentic o human
| Paso | Clasificación | Riesgo | Control |
|---|---|---|---|
| Lectura, resolución de fuentes, R1–R5, filtrado de opciones | determinístico | regla demasiado simple; política que cambia | `UNKNOWN` ante duda; pruebas de frontera |
| Aprobar R4/R5, rebooking, compensación | human | acción sin control | no se ejecuta nada |
| Consulta ambigua (solo diseño) | agentic | inventar un derecho | solo lectura; validador contra la evaluación |
| Mensaje al pasajero (solo diseño) | AI-assisted | afirmar algo no validado | plantilla con `policy_ref` |
| Análisis, diseño y código del proyecto | AI-assisted | error factual de la IA | revisión humana, pruebas, bitácora |

No hay IA en runtime: la exactitud es el objetivo y un modelo añadiría costo, latencia y riesgo de inventar derechos [PDF p.5].

## Riesgos y próximos pasos
- Supuestos sin validar con negocio: S-1, S-3, la regla de grupos, tiempo mínimo de conexión, umbral de frescura (`analisis-funcional-v0.2.md` §12).
- Con los datos del PDF el caso base nunca llega a una recomendación definitiva. Es coherente con el enunciado, y la rama «respaldada» solo se demuestra con escenarios sintéticos.
- Código y pruebas salen de la misma IA, así que un malentendido compartido no se detectaría solo con las pruebas.
- Boot 4.1.1 se comprobó solo contra la caché local (la búsqueda web estaba bloqueada). Si da problemas, el plan B es 4.0.8.
- Próximos: adaptadores reales, filas de fallo «200 parcial» de AD-8 (política u opciones caídas) y validación con Operaciones y PM.

## Cómo se validó
- **Pruebas:** 35 con instante fijo. Cubren R1 en 179/180/200, el escenario de 200 min con y sin menor, la frontera del TTL (11:04:34Z y 11:04:35Z), fuente contradictoria, grupo, ausencia de datos personales en el JSON y errores HTTP (`DependencyFailureTest`).
- **Ejecución real:** `bootRun` más `curl` del caso base, del TTL, de `at` inválido y de `bookingId` inexistente. Sin coincidencias de datos personales en la respuesta ni en `src/main`.
- **Manual (autor):** con `curl` comprobó el caso base, los errores 400 y 404, la vigencia de las opciones y que la respuesta no expone datos personales. No consta que haya ejecutado por su cuenta la suite de pruebas ni el escenario de 200 min.
- **Límite:** el escenario de 200 min es sintético y solo se prueba por tests, no por HTTP. Limitaciones completas en `docs/implementacion/notas-construccion.md`.
