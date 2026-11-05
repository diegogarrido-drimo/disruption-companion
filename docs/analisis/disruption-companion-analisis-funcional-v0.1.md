# Disruption Companion — Análisis funcional inicial

**Versión:** 0.1 — borrador para revisión crítica con Claude/BMAD  
**Track propuesto:** Backend / Integración  
**Fuente principal:** `Disruption Companion.pdf` (desafío técnico, Caso A, 7 páginas).  
**Propósito:** complementar el enunciado original con comprensión del negocio, incertidumbres, escenarios y criterios de aceptación *antes* de diseñar o implementar.  
**Estado:** hipótesis y decisiones propuestas pendientes de revisión; **no** es un PRD aprobado ni sustituye los contratos del PDF.

> **Instrucción de uso con IA:** adjuntar **este Markdown y el PDF original**. El PDF prevalece en ejemplos, interfaces y reglas; cualquier ampliación de este documento debe etiquetarse como propuesta o supuesto. No atribuir a LATAM ninguna política que no esté expresamente en el desafío.

## 1. Problema y resultado de negocio

Un pasajero con un vuelo modificado necesita comprender qué ocurrió, qué significa para su itinerario y qué puede hacer. La aerolínea busca reducir la incertidumbre y los contactos evitables al call center sin sacrificar **exactitud, seguridad, trazabilidad ni control de acciones de alto impacto** (PDF, p. 1).

El producto inicial debería permitir: (1) consultar contexto del viaje; (2) entender una modificación relevante y su implicancia; (3) recibir explicación/recomendación accionable basada en información verificada; (4) derivación humana o inicio de una acción controlada, según corresponda.

**Principio rector propuesto:** ante evidencia insuficiente, informar hechos confirmados y la incertidumbre; no asegurar derechos, conexiones perdidas, disponibilidad ni cambios ejecutados.

**Resultado verificable del vertical slice propuesto:** una función o API de evaluación que combine contexto de reserva, eventos operacionales y políticas para producir una *evaluación justificada y segura*, con tratamiento explícito de incertidumbre. No es necesario realizar rebooking real ni desarrollar frontend.

## 2. Hechos confirmados del enunciado vs. decisiones pendientes

### Confirmado por el PDF

- El ejercicio permite elegir Backend/Integración o Frontend/Experience; la capa complementaria puede documentarse con contrato, diseño o pseudocódigo (pp. 4–5).
- Backend requiere resolver el caso base combinando **reserva + eventos + políticas**, manejo de errores y al menos una validación/test (p. 4).
- Las interfaces de ejemplo pueden simularse, **respetando sus contratos**; no es necesario usar todas (pp. 1–4).
- Hay información incompleta, fuentes operacionales potencialmente contradictorias y degradadas, políticas versionadas y opciones candidatas que **no son decisiones finales** (pp. 2–4).
- Cambiar reserva o conceder compensación supone alto impacto; se exigen controles; los datos personales requieren mínimo privilegio (p. 4).
- El uso de IA en runtime **no es obligatorio**; la decisión de usarla o no debe explicarse (p. 5).

### No especificado: NO inferir como hecho

- Enumeraciones completas para `_meta.completeness`, `STATUS`, tipos de evento, valores de `confidence` y errores de cada integración.
- SLA o umbral exacto de frescura de los eventos; prioridad formal entre fuentes; si todas las zonas horarias son locales, UTC o mixtas.
- Reglas para calcular formalmente conexión perdida (hora real/proyectada de llegada, embarque/cierre, mínimo de conexión, etc.).
- Semántica precisa de `confidence` y criterios finales de aprobación de opciones.
- Política definitiva sobre dividir reservas/grupos, experiencia del pasajero, autenticación, comunicación, consentimiento y disponibilidad.
- Definición oficial de “modificación relevante”.
- Que “operación por equipo pequeño” equivalga a bajo volumen de tráfico: **no se puede concluir**.

## 3. Mapa de integraciones y condiciones de confianza

| Interfaz del PDF | Naturaleza | Uso propuesto | Riesgo / validación |
|---|---|---|---|
| `get_trip_context(booking_id)` | Lectura | Itinerario, pasajeros, condiciones tarifarias y metadatos | `completeness: PARTIAL`; `missing_fields`; verificar datos críticos según decisión; no asumir otros estados válidos |
| `get_operational_events(segment_ids)` | Lectura | Retrasos, puertas, estados por tramo | Distinguir `recorded_at` de `_meta.retrieved_at`; fuente, frescura, degradación, consistencia y tipo de evento |
| `get_eligible_options(trip_context)` | Lectura | Opciones *candidatas* de reubicación/reembolso | Vigencia (`ttl_seconds`), cupos para todo el grupo, costo, `confidence`, `policy_ref`; no tratar como beneficio aprobado |
| `get_policy_rules(scenario)` | Lectura | Reglas verificables y versionadas | Evaluar condiciones y vigencia; registrar versión/regla que justifica respuesta |
| `create_rebooking_request(option,idempotency_key)` | Escritura | Fuera de la implementación inicial; diseño de futuro flujo controlado | Alto impacto, autorización/confirmación; `409` duplicado, `503` resultado indeterminado: no reintentar ciegamente |
| `create_human_case(reason,evidence)` | Escritura | Posible escalamiento simulado / diseñado | Definir gatillantes, evidencia mínima, información personal estrictamente necesaria |
| `notify_passenger(message)` | Escritura | Fuera de alcance inicial; contrato diseñado | Solo mensajes validados; el ejemplo devuelve `422` si falta `policy_ref` |

**Sobre metadatos:** `retrieved_at` indica cuándo se recuperó el conjunto de datos, mientras `recorded_at` indica cuándo quedó registrado cada evento. No comparar estos timestamps como si fueran equivalentes. `scheduled_departure/arrival` son horarios programados, no estimaciones operacionales actuales.

## 4. Análisis del caso base entregado por el PDF

### Reserva e itinerario

- `booking_id: ABC123`: María (ADULT) y Tomás (CHILD, 7 años). Tratar identidad y contactos como datos ficticios del PDF; no propagarlos innecesariamente en API/logs.
- Segmento `S1`, LA500, SCL → LIM: salida programada **14:30**, llegada programada **17:55**.
- Segmento `S2`, LA2410, LIM → MEX: salida programada **19:05**.
- Conexión programada: **70 minutos** entre llegada S1 y salida S2. Es una resta de horarios de ejemplo, no certifica que cumpla un mínimo de conexión.
- `get_trip_context` recuperado `11:02:31Z`, `completeness: PARTIAL`, falta `fare_conditions.change_penalty_amount`. Ese dato puede ser relevante para conclusiones económicas, pero su ausencia no impide por sí sola detectar el retraso.

### Eventos operacionales

- `OPS_CORE`, registrado `10:48Z`: evento `DELAY` en S1, nueva salida **17:10**, retraso **160 minutos**.
- `AIRPORT_FEED`, registrado `10:55Z`: `GATE_CHANGE`, puerta 24.
- `LEGACY_MIRROR`, registrado `09:30Z`: `STATUS: ON_TIME` en S1; `LEGACY_MIRROR` aparece degradado en `_meta`.
- `OPS_CORE`, registrado `10:48Z`: `STATUS: SCHEDULED` en S2.
- Los eventos se recuperaron a `11:02:33Z`. Existe tensión aparente entre el estado anterior `ON_TIME` y el retraso posterior; la semántica de los eventos y las fuentes debe evaluarse antes de asumir incompatibilidad absoluta.

### Reglas visibles para `get_policy_rules("DELAY")`

| ID | Condición (según PDF) | Derecho / respuesta | Requiere aprobación humana |
|---|---|---|---|
| R1 | `delay_minutes >= 180` | `REBOOK_NO_FEE` | No |
| R2 | `delay_minutes >= 240 AND international` | `MEAL_VOUCHER` | No |
| R3 | `missed_connection == true` | `REBOOK_NO_FEE` | No |
| R4 | `compensation_requested == true` | `CASH_COMPENSATION` | Sí |
| R5 | `group_includes_minor == true` | `PRIORITY_HANDLING` | Sí |

**Lectura prudente del caso base:**

1. Existe evidencia de un retraso de 160 minutos. **No** cumple R1 (180 min) ni R2 (240 min).
2. No hay hora proyectada de llegada S1 ni datos completos para afirmar que `missed_connection == true`; por tanto, no podemos activar R3 *solo* porque la conexión parece amenazada.
3. La reserva incluye un menor y por ello **es pertinente evaluar R5**; el manejo prioritario requiere aprobación humana según el ejemplo. No afirmar que ya se ejecutó.
4. La alternativa O1 (4 asientos; CLP 0 diferencia; `HIGH`) es candidata; O2 (1 asiento; CLP 180.000; `MEDIUM`) no alcanza para el grupo de dos si el grupo debe mantenerse unido; O3 (`REFUND`; `LOW`) no acredita elegibilidad.
5. La respuesta inicial segura puede informar retraso verificado, advertir incertidumbre de conexión y sugerir verificación o revisión humana, **sin prometer reubicación gratuita**.

**Importante:** no confundir “diferencia de tarifa cero” con “derecho formal a reubicación sin costo”, ni `confidence` de una opción con verificación de toda la decisión.

## 5. Ambigüedades y responsables propuestos

| Pregunta | Propuesta provisional / cuestión a resolver | Interlocutor sugerido |
|---|---|---|
| ¿Qué cambio es relevante? | Priorizar retraso que afecte la conexión o cambie siguiente acción; el umbral es supuesto, no política confirmada | PM + Operaciones |
| ¿Qué comunicar al pasajero? | Solo hechos verificables y explicación de incertidumbre; no datos privados innecesarios ni derechos sin validar | PM + Product Designer + Seguridad |
| ¿Cuándo recomendar automáticamente? | Solo con entradas críticas suficientes, políticas vigentes, opciones viables y ausencia de bloqueos | PM + ETL |
| ¿Cuándo intervenir humanamente? | Incertidumbre decisiva, compensación, manejo prioritario por menor, discrepancias que impiden decidir o acción de alto impacto | PM + Operaciones |
| ¿Cómo resolver fuentes contradictorias? | Validar fuente, tipo de evento, `recorded_at`, degradación y frescura; si no resoluble, *fail-safe* | ETL + owner operacional |
| ¿Qué significa conexión perdida? | Falta definir criterios, fuente autoritativa, tiempos de conexión y estados confirmados | Operaciones + PM |
| ¿Cómo se manejan pasajeros juntos? | No recomendar opción con cupos inferiores al grupo sin política explícita de separación | PM + Operaciones |
| ¿Cuánto dura la vigencia de las opciones? | Respetar TTL de ejemplo y revalidar antes de una acción | ETL + owner inventario |
| ¿Cómo se calcula un beneficio con datos tarifarios parciales? | Separar evaluación operacional de condiciones económicas; no inventar montos | PM + owner políticas |
| ¿Qué registrar y qué nunca enviar a un modelo? | Minimización de PII, logs con IDs de correlación, políticas y códigos de razón; no documentos ni contactos crudos | ETL + Seguridad |

## 6. Decisiones propuestas para revisión — NO aprobadas aún

**D1. Alcance:** implementar solo evaluación de disrupción del caso base en Backend, con mocks y API/función invocable. Documentar experiencia de pasajero y llamadas de escritura como capa complementaria.

**D2. Estados de salida del backend (nombres *propuestos*, no del PDF):** `CONFIRMED_RECOMMENDATION`, `REVIEW_REQUIRED`, `INSUFFICIENT_DATA`. Debatir si hacen falta menos estados y si `REVIEW_REQUIRED` describe una derivación efectiva o solo sugerida.

**D3. Evidencia por decisión:** incluir `reason_codes`, referencia de regla/versión, fuente de evento, timestamp pertinente y campos faltantes relevantes; sin exponer PII.

**D4. Fallback seguro:** si no se puede probar una condición crítica, no inferirla ni ejecutar efectos secundarios; mostrar lo conocido y señalar pendiente o escalamiento.

**D5. IA:** desarrollo asistido por Claude/BMAD para análisis, diseño, pruebas y código. En runtime, dejar reglas de derechos y selección validada **determinísticas**; agente conversacional opcional como diseño futuro, sin autoridad para confirmar compensaciones o ejecutar cambios.

**D6. No sobrearquitectura:** aplicación modular Spring Boot (controlador, orquestación, evaluador de eventos, evaluador de políticas, gateways/mock adapters); no desplegar microservicios ni DB para un slice de 3 horas.

**D7. Seguridad:** datos ficticios del ejemplo solo como entradas de prueba; respuesta y logs minimizados; supuestos de autenticación/autorización pendientes para producto real.

## 7. Matriz de escenarios y criterios de aceptación preliminares

| Código | Escenario | Comportamiento esperado (propuesta) | Tipo |
|---|---|---|---|
| AC-01 | **Caso base**, retraso 160 minutos, conexión sin estado confirmado | Informar retraso, no reconocer R1/R2, no declarar R3 como cumplida; indicar riesgo/incertidumbre | Crítico |
| AC-02 | Escenario sintético, retraso válido de 200 minutos, política R1 vigente | Reconocer **elegibilidad según R1**, sin ejecutar cambio de reserva | Crítico |
| AC-03 | Fuentes con datos contradictorios/no resolubles | Respuesta de revisión o datos insuficientes; no recomendación definitiva | Crítico |
| AC-04 | Falta campo no crítico de contexto | Mantener evaluación de la parte que sí puede verificarse, sin inventar datos | Alto |
| AC-05 | Dos pasajeros y opción con un asiento | No proponer la opción como solución para ambos | Alto |
| AC-06 | Grupo incluye menor | Identificar R5 como candidata y requisito de aprobación humana | Alto |
| AC-07 | Timeout o error de lectura de dependencia crítica | Manejar error con salida controlada y trazabilidad; no inventar respuesta | Alto |
| AC-08 | Opción caducada por TTL | No presentarla como actualmente confirmada; requerir revalidación | Medio |
| AC-09 | Política ausente o versión no verificable | No confirmar derecho asociado; indicar revisión | Alto |
| AC-10 | Escenario sintético con conexión perdida **confirmada por datos suficientemente autoritativos** | Reconocer condición R3 sin inferirla del retraso; documentar criterio de confirmación | Alto, sujeto a definición |

**El caso base AC-01 es obligatorio; los escenarios sintéticos son pruebas diseñadas y no ejemplos del PDF.** Para la implementación seleccionar pocos casos con alto valor de evidencia; no prometer cobertura completa.

## 8. Preguntas para la revisión crítica con agentes

1. ¿Qué afirmaciones de este documento son interpretaciones presentadas por error como hechos?
2. ¿Es correcto tratar el retraso y la conexión incierta como dos hechos distintos? ¿Qué datos mínimos faltan?
3. ¿Cómo evitar que la lógica de frescura priorice incorrectamente un tipo de evento sobre otro?
4. ¿Qué condiciones deben cumplirse para recomendar una alternativa al grupo completo?
5. ¿Qué casos borde del propio PDF estamos omitiendo (R5 menor, TTL, 409/503, política versionada, PII)?
6. ¿Qué pruebas automáticas dan mayor valor dentro de un vertical slice de 3 horas?
7. ¿Qué parte es decisión determinística, cuál podría ser asistida por IA y cuál requiere supervisión humana?
8. ¿Qué conviene **no implementar** y por qué?

## 9. Alcance preliminar de la entrega y criterios de cierre del análisis

**Incluido (propuesto):** endpoint o función invocable, modelos alineados con el PDF, mocks, reglas para escenario base, resultados con explicación/evidencia, error/fallback y tests críticos.

**Excluido (propuesto):** cambios reales de reserva, compensación monetaria, envío real de notificaciones, autenticación de producción, streaming de eventos, persistencia real, infraestructura, UI funcional, agente de IA en runtime.

**Complementario:** contrato de respuesta para frontend + descripción de experiencia; solo al finalizar, Markdown UX para Google Stitch si hay tiempo y no desplaza entregables obligatorios.

**Antes de planificar implementación:** aprobar (a) alcance IN/OUT; (b) hechos vs. supuestos; (c) reglas y fuente de verdad; (d) comportamiento del caso base; (e) criterio de revisión humana; (f) 3–5 tests prioritarios. Registrar decisiones pendientes y evitar ampliar alcance por recomendaciones automáticas de agentes.

## 10. Restricción de evaluación y transparencia de IA

El desafío fija **48 horas de plazo** desde recepción y **máximo 3 horas de trabajo efectivo**, incluyendo análisis, implementación, validación y documentación; seguimiento de tiempo recomendado. Entregables: vertical slice, README de ingeniería (máx. 2 páginas), AI Usage & Decision Note (máx. 1 página). Presentación de 10 minutos y discusión técnica posterior (PDF, pp. 1, 5–7).

Registrar en AI Usage Note: herramienta/agente, petición, salida, decisión propia (aceptada/modificada/rechazada), validación y qué datos nunca se compartirían con el modelo.

---

**Nota para Claude/BMAD:** esta versión es insumo de *análisis funcional*, **no** una orden de implementación. Primero criticar, clasificar supuestos y proponer correcciones mínimas; después solicitar aprobación humana antes de emitir plan técnico o código.
