# Disruption Companion — Análisis funcional v0.2

**Versión:** 0.2 · **Fecha:** 2026-11-04 · **Track:** Backend / Integración (Java + Spring Boot)
**Base:** `Disruption Companion.pdf` (prevalece sobre este documento) + análisis v0.1 + diagnóstico del Analyst (`diagnostico-analyst-v0.1.md`) + ajustes del usuario.
**Estado:** **CERRADO el 2026-11-04**, listo para Architect (ver §13). **No es un PRD ni una arquitectura.** Las etiquetas `[PENDIENTE]` de secciones anteriores quedan superadas por §11 y §13; solo el registro de tiempo sigue abierto (lo registra el usuario).

**Leyenda de origen:** `[PDF p.N]` hecho del enunciado · `[DERIVADO]` cálculo a partir de datos del PDF · `[SUPUESTO]` interpretación nuestra · `[PENDIENTE]` decisión sin aprobar · `[APROBADO]` confirmado por el usuario.

---

## 1. Problema y resultado esperado

Un pasajero con un vuelo modificado debe entender qué ocurrió y cuál es su siguiente paso, sin recibir información no verificada ni que se ejecuten acciones de alto impacto sin controles [PDF p.1]. Objetivo: reducir incertidumbre y contactos evitables al call center, manteniendo exactitud, seguridad y trazabilidad [PDF p.1].

**Vertical slice [APROBADO como alcance general]:** servicio o función invocable del backend que combina reserva + eventos + políticas (+ opciones) y devuelve una **evaluación justificada y segura** del caso base, con manejo de errores y al menos una validación/test [PDF p.4]. Sin frontend, sin escrituras reales, sin sobrecomplicar.

## 2. Cambios respecto a v0.1

| # | Cambio | Origen |
|---|---|---|
| 1 | Conexión **en riesgo** (proyectada) se distingue de **perdida** (confirmada); R3 queda DESCONOCIDO en el caso base | Diagnóstico C1; aprobado en principio por el usuario |
| 2 | Toda condición de política se evalúa con tres valores: VERDADERO / FALSO / DESCONOCIDO | C1 [PENDIENTE detalle] |
| 3 | Se agrega un instante de referencia explícito ("ahora") para evaluar vigencia | C4; el usuario valida la vigencia de datos [PENDIENTE valor y regla] |
| 4 | Se reemplaza el enum D2 por dos ejes (evidencia + compuerta de acción) | C2 [PENDIENTE] |
| 5 | Corrección: las opciones **sí** traen `confidence` (HIGH/MEDIUM/LOW) | Usuario |
| 6 | O3 `REFUND` **no** se descarta por `refundable:false`; queda "no verificable" hasta consultar su política (`POL-REFUND-2026-01`) | Usuario |
| 7 | Menores: no se proponen alternativas que separen al grupo, como **supuesto pendiente de validar** (no bloqueo duro) | Usuario |
| 8 | IA/diseño complementario pasa a IN-scope en versión ligera (tabla de clasificación, consulta ambigua, mensaje al pasajero) | C3 [PENDIENTE confirmar] |
| 9 | Se recortan los criterios de aceptación a los de mayor valor (ver §8) | Usuario: priorizar |

## 3. Hechos confirmados por el PDF

- Reserva `ABC123`: P1 María (ADULT), P2 Tomás (CHILD, 7). Segmentos S1 LA500 SCL→LIM 14:30–17:55; S2 LA2410 LIM→MEX 19:05–23:40; ambos 2026-09-15 [PDF p.2].
- Tarifa LIGHT, `changeable:true`, `refundable:false`. Contexto `PARTIAL`, falta `fare_conditions.change_penalty_amount`; `retrieved_at` 11:02:31Z [PDF p.2].
- Eventos (`retrieved_at` 11:02:33Z): E-1001 DELAY S1, 160 min, nueva salida 17:10, OPS_CORE, 10:48Z · E-1007 GATE_CHANGE S1 puerta 24, AIRPORT_FEED, 10:55Z · E-1012 STATUS ON_TIME S1, LEGACY_MIRROR, 09:30Z · E-2003 STATUS SCHEDULED S2, OPS_CORE, 10:48Z. `sources_degraded: [LEGACY_MIRROR]` [PDF p.2–3].
- Opciones (`retrieved_at` 11:02:35Z, `ttl_seconds` 120): O1 REBOOK LA2412 LIM→MEX 09-16 08:15, diferencia 0, 4 asientos, HIGH · O2 REBOOK LA2450 09-15 23:40, CLP 180.000, 1 asiento, MEDIUM · O3 REFUND, LOW, `POL-REFUND-2026-01` [PDF p.3].
- Política `POL-DELAY-2026-03`, vigente desde 2026-03-01, reemplaza `POL-DELAY-2025-11`; R1 `delay>=180`→REBOOK_NO_FEE; R2 `delay>=240 AND international`→MEAL_VOUCHER; R3 `missed_connection`→REBOOK_NO_FEE; R4 `compensation_requested`→CASH_COMPENSATION (aprobación humana); R5 `group_includes_minor`→PRIORITY_HANDLING (aprobación humana) [PDF p.3].
- Escrituras: `create_rebooking_request` 201 PENDING_CONFIRMATION / 409 duplicado / 503 resultado indeterminado; `create_human_case` 201 (cola DISRUPTION_L2, 45 min); `notify_passenger` 202 / 422 si falta `policy_ref` [PDF p.3–4].
- Restricciones: datos desactualizados o contradictorios; recomendación incorrecta = costo/reclamo/incumplimiento; cambiar reserva y compensar son alto impacto; mínimo privilegio; equipo pequeño; latencia, costo y dependencia de modelos; no hay solución única [PDF p.4].
- IA en runtime no obligatoria; resolver de forma determinística puede ser excelente decisión [PDF p.5].
- Límites: 3 horas efectivas **incluyendo análisis**, 48 h de plazo, README ≤2 pág., AI Usage Note ≤1 pág. [PDF p.1, 5, 6].

## 4. Derivados y supuestos

**Derivado:** llegada proyectada S1 = 17:55 + 160 min = 20:35; salida S2 = 19:05; déficit proyectado 90 min (antes de cualquier tiempo mínimo de conexión) [DERIVADO].

**Supuestos (no confirmados):**
- S-1: todas las horas comparten un marco horario (el PDF no lo especifica; solo `recorded_at`/`retrieved_at` llevan `Z`).
- S-2: S1 aún no despegó en el instante de evaluación.
- S-3: `delay_minutes` es retraso de salida y se propaga a la llegada.
- S-4: `GATE_CHANGE` es informativo, no una "modificación relevante".
- S-5: un evento de fuente degradada y más antiguo que un evento de fuente sana no prevalece.
- S-6: "menor" = `type == CHILD` (o edad < 18). Datos incoherentes → incertidumbre.
- S-7: `compensation_requested` es un input externo, falso por defecto.
- S-8: `international` se deriva de origen/destino (SCL, LIM, MEX son aeropuertos de países distintos); el PDF no lo entrega como dato.
- S-9: no se proponen alternativas que separen al grupo (decisión del usuario, **pendiente de validar con PM**).

## 5. Reglas aplicadas al caso base

| Regla | Valor | Justificación |
|---|---|---|
| R1 | FALSO | 160 < 180 (margen 20 min, volátil) |
| R2 | FALSO | 160 < 240 |
| R3 | DESCONOCIDO | riesgo proyectado alto, sin confirmación ni criterio de "perdida" definido |
| R4 | No aplica | sin solicitud de compensación |
| R5 | VERDADERO | P2 es CHILD; requiere aprobación humana; **no concedido ni ejecutado** |

Se separan tres niveles: condición cumplida → derecho concedido → acción ejecutada. R1 y R3 otorgan el mismo derecho: deduplicar si ambas aplicaran.

**Salida esperada del caso base [PENDIENTE de aprobación por PM]:**
- Hechos: retraso de 160 min, nueva salida 17:10 (OPS_CORE 10:48Z); puerta 24 informativa.
- Descartado con motivo: `ON_TIME` de fuente degradada y anterior.
- Riesgo alto de conexión perdida (derivado, no confirmado).
- R1/R2 no aplican; R5 cumplida y pendiente de aprobación humana.
- O1: candidata viable para el grupo (4 asientos ≥ 2), costo no confirmable (falta penalidad), sujeta a vigencia. O2: no sirve para el grupo (1 asiento). O3: no verificable (política sin consultar).
- Recomendación de revisión humana; ninguna escritura ejecutada.

## 6. Reglas de seguridad y evidencia (propuestas)

1. No afirmar derechos, conexiones perdidas, disponibilidad ni cambios ejecutados sin evidencia verificada.
2. Cada conclusión cita versión de política, ID de regla, ID/fuente/`recorded_at` del evento y `retrieved_at` pertinente.
3. Opción vencida por TTL no se presenta como vigente; requiere revalidación [PENDIENTE: ¿se retira o se advierte?].
4. Campo faltante `change_penalty_amount`: ningún costo de cambio se afirma; O1 queda "costo por confirmar".
5. Política ausente, `policy_ref` distinto de la versión recuperada, o regla/condición desconocida → no confirmar el derecho.
6. Salidas y logs sin PII (email, teléfono, documento); solo IDs. Proyección mínima de datos hacia las tools y hacia cualquier modelo.
7. Escrituras (`create_rebooking_request`, `create_human_case`, `notify_passenger`) fuera de la implementación; solo diseño.

**Matriz de fallos [PENDIENTE]:**

| Dependencia | Efecto |
|---|---|
| `get_trip_context` | bloqueante, sin evaluación |
| `get_operational_events` | bloqueante para afirmar retraso |
| `get_policy_rules` | solo hechos, sin derechos |
| `get_eligible_options` | derechos sin opciones |

## 7. Casos borde (priorizados)

**Prioridad alta (cubrir en tests/diseño):** conexión en riesgo vs perdida; fuente degradada más reciente que la sana; frontera de R1 (179/180) y R2 (239/240); opción con menos asientos que pasajeros; opción vencida por TTL; ausencia de `change_penalty_amount`; PII en salida/logs; caída de dependencia crítica.

**Documentar sin implementar:** evento sin segmento o con tipo desconocido; segmento sin eventos (no equivale a "a tiempo"); `SCHEDULED` no confirma salida; inconsistencia interna `delay_minutes` vs `new_departure`; `policy_ref` de opción distinto de la política recuperada; versiones superadas; menor sin adulto, tipo y edad incoherentes, infante sin asiento; opciones polimórficas (campos ausentes); 409/503 de escritura (no reintentar ciegamente; el 503 es indeterminado); retraso que supera medianoche; O1 sale al día siguiente (sin derechos de alojamiento definidos en el PDF); 160 min puede cruzar a 180 si el retraso crece.

## 8. Criterios de aceptación

Implementar (★) y documentar el resto. Los escenarios sintéticos son nuestros, no del PDF.

| ID | Criterio (Dado / Cuando / Entonces) | Prioridad |
|---|---|---|
| ★ AC-1 Caso base | Dado el PDF completo y un instante de referencia anterior a 11:04:35Z, cuando se evalúa, entonces R1/R2 FALSO, R3 DESCONOCIDO con riesgo alto, R5 VERDADERO sujeto a aprobación, O1 candidata, O2 no apta, O3 no verificable, sin escrituras. | Crítico |
| ★ AC-2 Regla cumplida (R1) | Con datos completos y vigentes: escenario sintético de 200 min → R1 VERDADERO sin aprobación humana y recomendación respaldada. Frontera: 179 → FALSO, 180 → VERDADERO. | Crítico |
| ★ AC-3 Fuente contradictoria | Con `ON_TIME` degradado más reciente que el DELAY, el estado es incierto, sin recomendación definitiva; el evento descartado se registra con motivo. | Crítico |
| ★ AC-4 Grupo | Con 2 pasajeros y opción de 1 asiento, esa opción no se propone. | Alto |
| ★ AC-5 TTL | Con instante ≥ 11:04:35Z las opciones se marcan vencidas. | Alto |
| ★ AC-6 Trazabilidad y PII | La respuesta incluye versión de política, IDs de regla/evento y marcas de tiempo; no contiene email, teléfono ni documento. | Alto |
| AC-7 Fallo de dependencia | Según matriz §6, error controlado y sin inventar datos. | Alto (documentar; implementar si hay tiempo) |
| AC-8 Política no verificable | `policy_ref` distinto o regla desconocida → no se confirma el derecho. | Medio |
| AC-9 Dato tarifario ausente | No se afirma costo de cambio. | Medio |
| AC-10 Determinismo | Mismas entradas y mismo "ahora" → mismo resultado. | Medio |

## 9. Alcance

**IN [PENDIENTE confirmar detalle]:** función/endpoint de evaluación del caso base; modelos alineados al PDF; mocks de las 4 lecturas; evaluación de reglas con tres valores; resolución de fuentes; vigencia TTL; salida con evidencia y sin PII; manejo de errores; 5–6 tests (★). Documentación: README ≤2 pág., AI Usage Note ≤1 pág., tabla de clasificación, diseño ligero de consulta ambigua y mensaje al pasajero.

**OUT:** escrituras reales, compensaciones, notificaciones, autenticación productiva, persistencia, streaming, infraestructura, UI, IA en runtime, parser genérico de condiciones.

**Viabilidad en 3 h:** adecuada con este recorte; las 3 h incluyen el análisis ya realizado, por lo que conviene registrar tiempo desde hoy. Estimación gruesa del Analyst (no del PDF): decisiones ≤30 min, construcción con tests ≈100 min, documentación ≈40 min, colchón 10 min.

## 10. Decisiones pendientes antes de Arquitectura

| ID | Decisión | Interlocutor |
|---|---|---|
| P-1 | Salida esperada del caso base (§5) y tratamiento de R3 DESCONOCIDO | PM |
| P-2 | Modelo de salida de dos ejes en vez del enum D2 | PM / Eng Lead |
| P-3 | Instante de referencia ("ahora") y qué hacer con opciones vencidas | Eng Lead / PM |
| P-4 | Regla de resolución de fuentes y umbral de frescura (S-5) | Eng Lead / Operaciones |
| P-5 | Política de grupo con menor (S-9) y definición de "menor" (S-6) | PM / Seguridad |
| P-6 | Tratamiento de O3 y consulta de `POL-REFUND-2026-01` | PM / owner políticas |
| P-7 | Aplicabilidad de política: escenario, versión, semántica de `delay_minutes` (S-3), regla desconocida | Eng Lead / owner políticas |
| P-8 | Matriz de fallos por dependencia | Eng Lead |
| P-9 | PII: lista de campos de salida, proyección hacia tools, supuesto de autenticación | Seguridad / Eng Lead |
| P-10 | Alcance IN/OUT final y tope de ★ (incluye diseños ligeros) | Usuario |
| P-11 | `create_human_case`: ¿se ejecuta (simulado) o solo se recomienda? | PM / Eng Lead |
| P-12 | Marco horario (S-1) y cómo se determina `international` (S-8) | Eng Lead |
| P-13 | Definición de "modificación relevante" y de "conexión perdida" | PM / Operaciones |
| P-14 | Registro de tiempo efectivo desde hoy | Usuario |

**Preguntas por interlocutor:** ver `diagnostico-analyst-v0.1.md` §7.

---

## 11. Resolución de decisiones pendientes (segunda ronda, 2026-11-04)

**Criterios aprobados por el usuario [APROBADO]:**
- C-1: si falta información crítica o hay contradicciones no resolubles, no se entrega recomendación definitiva.
- C-2: las opciones vencidas requieren revalidación.
- C-3: no se separan automáticamente grupos con menores.
- C-4: O3 queda pendiente de verificar; ni descartada ni ofrecida como beneficio confirmado.
- C-5: las escrituras quedan fuera de la implementación; solo se diseña su funcionamiento.
- C-6: no exponer datos personales innecesarios en respuestas ni logs.
- C-7: pruebas con instante de referencia fijo, reproducibles con los datos del PDF.
- C-8: lo que depende de PM se resuelve con supuestos explícitos y una lista de validación previa a producción.
- C-9: casos borde priorizados por valor demostrativo, sin implementarlos todos.

**Estado de P-1 a P-14.** "Cerrada con supuesto" significa que se puede avanzar a arquitectura y que queda en la lista de validación de la sección 12. Las propuestas de la IA que el usuario no ha confirmado se marcan como tales.

| ID | Estado | Resolución |
|---|---|---|
| P-1 Salida del caso base | Cerrada con supuesto | Por C-1: R3 queda DESCONOCIDO (es información crítica), así que **no hay recomendación definitiva**. Se informa lo verificado, el riesgo proyectado, R5 pendiente de aprobación y O1 como candidata sujeta a revalidación. |
| P-2 Modelo de salida | **Abierta (pequeña)** | La necesidad funcional está cerrada: debe distinguir "recomendación definitiva" de "no definitiva" y mostrar si hay aprobación humana pendiente. La forma concreta la propone el Architect. Propuesta de la IA, no aprobada: un único resultado (definitiva / no definitiva) más una lista de motivos, sin dos ejes formales. |
| P-3 Instante y vencidos | Cerrada con supuesto | Instante fijo (C-7) y revalidación obligatoria (C-2). Valor propuesto, no aprobado: `2026-09-15T11:03:00Z`, posterior a todos los `retrieved_at` y anterior al vencimiento (11:04:35Z). Un segundo instante posterior al vencimiento cubre el caso de opciones vencidas. |
| P-4 Fuentes y frescura | Cerrada con supuesto | Se aplica S-5. No se define umbral de frescura porque el PDF no lo da; se ordena por `recorded_at` y por estado degradado. Si no se puede resolver, aplica C-1. |
| P-5 Grupo con menor | Cerrada con supuesto | C-3 y S-6 (menor = `CHILD`). Validar con PM antes de producción. |
| P-6 O3 | Cerrada | C-4. |
| P-7 Aplicabilidad de política | Cerrada con supuesto | Se usa la versión recuperada vigente; S-3 para `delay_minutes`; una regla o `policy_ref` desconocido no confirma el derecho (consecuencia de "sin respaldo de política"). |
| P-8 Matriz de fallos | Cerrada por derivación | La matriz de la sección 6 es consecuencia de C-1. El usuario no la aprobó fila por fila, así que **confirmar al leerla**. |
| P-9 Datos personales | Cerrada con supuesto | C-6. La lista exacta de campos de salida la fija el Architect. Autenticación fuera de alcance; se documenta como validación previa a producción. |
| P-10 Alcance IN/OUT | **Abierta (pequeña)** | OUT confirmado (C-5). Falta que el usuario confirme los 6 criterios ★ de la sección 8 como los que se implementan. |
| P-11 Caso humano | Cerrada | Por C-5, `create_human_case` no se ejecuta; la salida solo recomienda la revisión humana y su diseño queda documentado. |
| P-12 Marco horario e `international` | Cerrada con supuesto | S-1 y S-8. Impacto bajo en el caso base: R2 no aplica con 160 min aunque sea internacional. |
| P-13 "Relevante" y "conexión perdida" | Cerrada con supuesto | Propuesta, no aprobada: "en riesgo" si la llegada proyectada supera la salida del siguiente segmento (sin tiempo mínimo de conexión, que el PDF no define). "Perdida" solo se tomaría de un dato explícito, y el PDF no trae ninguno; por eso R3 queda DESCONOCIDO. Retraso relevante = cambia el cumplimiento de una regla o afecta una conexión; `GATE_CHANGE` es informativo (S-4). |
| P-14 Registro de tiempo | **Abierta (acción del usuario)** | Tiempo consumido: No registrado. |

**Realmente abiertas: 3.** P-2 (forma de la salida, delegada al Architect), P-10 (confirmar la lista ★) y P-14 (tiempo).

**Una tensión a vigilar.** C-1 y la salida del caso base implican que, con los datos del PDF, el backend nunca emite una recomendación definitiva. Eso es coherente con el PDF (opciones "candidatas", conexión no confirmable). Para demostrar la rama "definitiva" en un test habría que usar un escenario sintético, por ejemplo retraso ≥180 con datos completos y vigentes. Está pendiente de confirmar que quieres ese test.

## 12. Validar antes de producción (no bloquea el slice)

1. PM: criterio de "modificación relevante", "conexión perdida" y comunicación del riesgo proyectado.
2. PM y Seguridad: política de grupos con menores y definición de "menor".
3. Operaciones: fuente autoritativa del estado de vuelo, prioridad entre fuentes, SLA de frescura, tiempo mínimo de conexión.
4. Owner de políticas: política de reembolso (`POL-REFUND-2026-01`), semántica de `delay_minutes`, política exacta de versiones.
5. Eng Lead: marco horario, identificación de `international`, autenticación y autorización, semántica del 503 en reubicación.
6. Product Designer: texto al pasajero para incertidumbre y para espera de aprobación.

---

## 13. Cierre del análisis funcional (2026-11-04)

**Confirmado por el usuario [APROBADO]:**
- **Salida (P-2):** un resultado simple con sus motivos que distingue (a) si la recomendación está respaldada y (b) si requiere intervención humana. Sin estados adicionales. La forma concreta la diseña el Architect.
- **Alcance (P-10):** se implementan AC-1 a AC-6 de la sección 8.
- **Instantes fijos:** `2026-09-15T11:03:00Z` para el caso base y un instante posterior al vencimiento (≥ 11:04:35Z) para opciones vencidas.
- **Matriz de fallos (P-8):** si falla una dependencia, respuesta controlada sin inventar información.
- **Conexión:** se mantiene la diferencia entre proyectada y confirmada.
- **Escenario sintético de 200 minutos:** se incluye para demostrar las dos ramas, "información insuficiente" (caso base) y "regla cumplida" (R1).

**Cómo encaja el escenario de 200 min con los 6 criterios:** lo incorporé dentro de AC-2 (con los casos de frontera 179/180), de modo que sigan siendo 6 criterios. Es mi interpretación de tu confirmación; si prefieres que sea un séptimo criterio, se cambia sin afectar el resto.

**Criterios de aceptación a implementar:**

| ID | Qué demuestra |
|---|---|
| AC-1 | Caso base: sin recomendación respaldada, R3 desconocida, R5 con aprobación humana pendiente |
| AC-2 | R1 cumplida con datos completos (200 min) y frontera 179/180 |
| AC-3 | Fuente degradada más reciente que la sana: incertidumbre, sin recomendación |
| AC-4 | Opción con menos asientos que pasajeros no se propone |
| AC-5 | Opciones vencidas requieren revalidación (segundo instante fijo) |
| AC-6 | Evidencia trazable (política, reglas, eventos, tiempos) y sin datos personales |

**Fuera de implementación y solo documentado:** AC-7 a AC-10, casos borde de la sección 7 (alta prioridad restante y los de documentar), escrituras (solo diseño, §6 regla 7), diseños ligeros (consulta ambigua, mensaje al pasajero, tabla de clasificación).

**Pendiente solo del usuario:** registro del tiempo efectivo consumido (P-14).

**Riesgos que el Architect debe respetar:** no sobreingeniería; la evaluación de condiciones es acotada a R1–R5, sin parser genérico; el instante de referencia es parámetro de entrada; sin persistencia ni escrituras.
