# Diagnóstico del Analyst (Mary) sobre el análisis funcional v0.1

**Fecha:** 2026-11-04 · **Insumos:** `Disruption Companion.pdf`, `disruption-companion-analisis-funcional-v0.1.md`, `Prompt de Analisis.md`
**Estado:** diagnóstico original, sin cambios. Las correcciones posteriores del usuario están en `analisis-funcional-v0.2.md` y en la bitácora.

> **Correcciones posteriores del usuario sobre este documento:**
> 1. Las opciones **sí** traen `confidence` (O1 HIGH, O2 MEDIUM, O3 LOW). El texto "falta la confidence de cada opción" de la sección 1 es un error del diagnóstico.
> 2. O3 (`REFUND`) no se descarta automáticamente por `refundable: false`: podría existir una política excepcional. Debe quedar como "no verificable / pendiente de política", no como contradicción (hallazgo A3).
> 3. Separar a un grupo con menor: se evitará proponer alternativas que lo separen, pero queda como **supuesto pendiente de validar**, no como bloqueo duro (hallazgo A5).

## Veredicto ejecutivo

El v0.1 es sólido: transcribe bien R1–R5, O1–O3 y timestamps, reconoce que el PDF prevalece, no inventa políticas y distingue `recorded_at` de `retrieved_at`. Tiene 4 hallazgos críticos y 8 altos. Alcance viable en 3 horas si se recorta.

## 1. Hechos, supuestos y pendientes

**Hechos del PDF:** R1–R5 y versión `POL-DELAY-2026-03` vigente desde 2026-03-01 (p. 3); DELAY de 160 min, nueva salida 17:10, OPS_CORE 10:48Z (p. 2), coherente con 14:30 + 160 min; `LEGACY_MIRROR` degradado y registrado 09:30Z, antes del DELAY (p. 2–3); conexión programada de 70 min (17:55 → 19:05); falta `fare_conditions.change_penalty_amount`; TTL de opciones 120 s desde 11:02:35Z (p. 3).

**Derivado por aritmética:** llegada proyectada de S1 si el retraso se propaga completo = 17:55 + 2h40 = 20:35, 90 min después de la salida de S2 (19:05). Es proyección, no confirmación.

**Supuestos a marcar:** horas sin zona horaria en un mismo marco de referencia; S1 aún no despegó; el retraso es de salida y se aplica al segmento.

**Pendientes reales:** enums, SLA de frescura, prioridad entre fuentes, definición de conexión perdida, "modificación relevante", política de separar grupos.

## 2. Hallazgos críticos

- **C1. Conexión en riesgo derivable.** El v0.1 la presenta como "sin datos" (§4 punto 2, AC-01). Con 20:35 contra 19:05 el déficit proyectado es de 90 min. Riesgo de dos errores opuestos: decir "indeterminado" cuando la evidencia es fuerte, o fijar `missed_connection=true` sin base. Recomendación: evaluar toda condición en VERDADERO / FALSO / DESCONOCIDO, distinguir "confirmado" de "proyectado". R3 queda DESCONOCIDO con riesgo alto proyectado. Definir quién fija `missed_connection` (no es campo de ninguna tool).
- **C2. Enum D2 mezcla dos dimensiones.** El caso base (R5 con aprobación humana + conexión incierta) no encaja limpiamente en ninguno de los tres estados. `CONFIRMED_RECOMMENDATION` sobreafirma ("opciones candidatas", p. 2); `INSUFFICIENT_DATA` y `REVIEW_REQUIRED` terminan en el mismo destino. Recomendación: dos ejes, suficiencia de evidencia y compuerta de acción.
- **C3. Falta lo que el PDF exige como tarea.** §5 ítems 2–3 (p. 4–5), tabla de clasificación (p. 5), dimensión "Razonamiento agentic y control" (p. 7). El v0.1 relega la IA a diseño futuro (D5) y el diseño complementario a "si hay tiempo". El PDF no penaliza no implementar la capa complementaria (p. 7) pero sí pide especificarla. Incluir en versión ligera: diseño de la consulta ambigua, tabla de clasificación, mensaje al pasajero.
- **C4. No existe un "ahora" de evaluación.** Las opciones expiran 11:04:35Z (p. 3); con reloj real ya vencieron. El resultado dependería del reloj y los tests no serían deterministas. Decidir un instante de referencia explícito y qué ocurre al vencer.

## 3. Hallazgos altos

- **A1. R5 es la única regla cuya condición es verdadera.** Separar condición cumplida / derecho concedido / acción ejecutada. Falta cómo se elige el escenario "DELAY".
- **A2. Dato tarifario faltante subestimado.** AC-04 lo llama "no crítico", pero con `changeable: true` y sin penalidad el costo real de reubicar es desconocido. `fare_difference: 0` no incluye penalidad.
- **A3. O3 `REFUND`** con `refundable: false` y `policy_ref` `POL-REFUND-2026-01`, nunca consultada. *(Ajustado por el usuario: no descartar; marcar como no verificable.)*
- **A4. Fuentes contradictorias.** El `ON_TIME` viene de fuente degradada, 79 min más antiguo y de otro tipo de evento; no es contradicción real. Regla a validar con Engineering: evento más reciente de fuente no degradada por segmento y dimensión; registrar lo descartado con motivo. Si una fuente degradada es más reciente, conflicto sin resolver = incertidumbre. Sin SLA de frescura en el PDF. Discrepancias internas (`delay_minutes` vs `new_departure`) también son conflicto.
- **A5. Grupos y menores.** Separar a un menor de 7 años es riesgo de seguridad. *(Ajustado por el usuario: evitar proponer alternativas que separen, como supuesto pendiente de validar.)* Definir "menor" (`type=CHILD` o `age<18`) y casos de datos incoherentes.
- **A6. PII y autenticación.** `get_eligible_options(trip_context)` recibe contexto con email, teléfono y documento (p. 2–3), contra mínimo privilegio (p. 4). Proyección mínima hacia tools y modelos. `booking_id` es identificador débil.
- **A7. Aplicabilidad de política.** Versión vigente según fecha del evento o de compra; `policy_ref` distinto al recuperado; semántica de `delay_minutes` (salida vs llegada, segmento vs viaje); R1 y R3 otorgan el mismo derecho (deduplicar); regla desconocida = salida segura.
- **A8. Asimetría del error** (p. 4). 160 min está a 20 min del umbral R1; el mensaje debe advertir que puede cambiar.

## 4. Mejoras opcionales

Restricciones de latencia/costo/dependencia de modelos (p. 4); opciones polimórficas (O1 sin `currency`, O3 sin vuelo ni asientos); tipos/estados desconocidos → salida segura; segmento sin eventos ≠ a tiempo; `SCHEDULED` no confirma salida; `GATE_CHANGE` informativo (supuesto); mapear entregable 1; "ETL" se confunde con Extract-Transform-Load; O1 sale el día siguiente (sin derechos de alojamiento definidos).

## 5. Reglas aplicadas al caso base

| Regla | Valor | Comentario |
|---|---|---|
| R1 (≥180) | FALSO | 160 < 180, margen 20 min |
| R2 (≥240 e internacional) | FALSO | "international" no viene como dato |
| R3 (conexión perdida) | DESCONOCIDO | proyección 20:35 vs 19:05, sin confirmación |
| R4 (compensación solicitada) | No aplica | input externo, por defecto falso |
| R5 (menor) | VERDADERO | P2 CHILD 7 años; requiere aprobación, no concedido |

Salida propuesta: hechos (retraso 160 min, nueva salida 17:10); puerta 24 informativa; `ON_TIME` descartado; riesgo alto de conexión (derivado, no confirmado); R1/R2 no aplican; R5 cumplida y pendiente de aprobación; O1 candidata viable para 2 pasajeros sujeta a TTL y costo desconocido; O2 insuficiente (1 asiento); O3 no verificable; revisión humana recomendada; ninguna escritura ejecutada.

Matriz de fallos propuesta: `get_trip_context` bloqueante; `get_operational_events` bloqueante para afirmar retraso; `get_policy_rules` solo hechos sin derechos; `get_eligible_options` derechos sin opciones.

## 6. Alcance para 3 horas

Adecuado si se recorta; las 3 horas incluyen el análisis (p. 6). Riesgos de desborde: parser genérico de condiciones, implementar todos los AC, simular las escrituras, documentos obligatorios más diseños de C3. Estimación gruesa del Analyst (juicio, no del PDF): decisiones ≤30 min, construcción con tests ≈100 min, documentación ≈40 min, colchón 10 min.

## 7. Preguntas por interlocutor

- **PM:** comportamiento esperado del caso base; riesgo proyectado ¿se comunica o espera confirmación?; "modificación relevante"; ofrecer O1 antes de aprobación de R5; separar grupo con menor; opciones vencidas.
- **Engineering Team Lead:** quién define `missed_connection`; prioridad/frescura entre fuentes; zona horaria; identificación de la reserva en `create_rebooking_request`; `create_human_case` ¿ejecuta o recomienda?; autenticación.
- **Product Designer:** comunicar incertidumbre sin alarmar ni sobreprometer; qué mostrar del menor; texto mientras R5 espera aprobación.
- **Operaciones/Seguridad (vía PM):** fuente autoritativa de estado, tiempos mínimos de conexión, datos de menores.

## 8. Decisiones a aprobar antes de Arquitectura

Ver `analisis-funcional-v0.2.md` §10, que consolida esta lista con los ajustes del usuario.
