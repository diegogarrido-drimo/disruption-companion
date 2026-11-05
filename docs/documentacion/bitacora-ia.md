# Bitácora de uso de IA — Disruption Companion

**Mantenida con la skill** `.claude/skills/ai-usage-log`. Alimenta el AI Usage & Decision Note (≤1 pág.) y la tabla de clasificación del README.
**Regla:** lo que no consta se marca `No registrado`; nada se inventa. Última actualización: 2026-11-04 (I-16).

## 1. Registro de interacciones

### I-01 · fecha No registrada · Análisis funcional inicial · Autor (humano)
- **Petición:** análisis propio del enunciado `Disruption Companion.pdf`.
- **Salida:** primer borrador del análisis funcional. Contenido intermedio: No registrado.
- **Decisión del usuario:** punto de partida. Fue revisado con ChatGPT (I-02).
- **Validación:** No registrada.
- **Clasificación:** human.
- **Riesgo / control:** sesgo de interpretación; mitigado con revisión posterior (I-03).
- **Artefactos:** No registrado.

### I-02 · fecha No registrada · Revisión y refinamiento · ChatGPT
- **Petición:** No registrada (el usuario indica que el análisis fue "revisado y refinado con ChatGPT").
- **Salida de la IA:** versión resultante `disruption-companion-analisis-funcional-v0.1.md` (2026-11-04 20:16 según el archivo). Cambios exactos introducidos por ChatGPT: No registrado.
- **Decisión del usuario:** usó el resultado como v0.1 y lo sometió a revisión crítica (I-03). Qué aceptó o descartó de ChatGPT: No registrado.
- **Validación:** I-03.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** el v0.1 presentó algunas interpretaciones como hechos; se controla con el contraste contra el PDF en I-03.
- **Artefactos:** `docs/analisis/disruption-companion-analisis-funcional-v0.1.md`.

### I-03 · 2026-11-04 · Revisión crítica · Claude, agente Analyst de BMAD (Mary)
- **Petición:** `docs/analisis/01-prompt-analyst.md` — contrastar v0.1 con el PDF, separar hechos/supuestos/pendientes, casos borde, criterios de aceptación, preguntas por rol, viabilidad en 3 h; sin código ni plan.
- **Salida de la IA:** diagnóstico con 4 hallazgos críticos, 8 altos, reglas aplicadas al caso base, 10 criterios de aceptación, preguntas por interlocutor y 12 decisiones a aprobar. Guardado en `_bmad-output/planning-artifacts/diagnostico-analyst-v0.1.md`.
- **Decisión del usuario:** **ACEPTADA con modificaciones.**
  - Aceptó el principio de distinguir conexión en riesgo de conexión perdida, validar vigencia de los datos y verificar la información antes de recomendar.
  - Corrigió un error de la IA: las opciones sí traen `confidence`.
  - Modificó A3: no descartar el reembolso solo por `refundable:false` (puede haber política excepcional).
  - Modificó A5: evitar alternativas que separen al grupo, pero como supuesto pendiente de validar, no bloqueo duro.
  - Pidió acotar el alcance a un vertical slice funcional del backend, sin sobrecomplicar.
- **Validación:** contraste manual del usuario contra el PDF (el usuario "revisó los puntos"; detalle No registrado). La IA verificó cifras contra el PDF: 17:10 = 14:30 + 160 min, llegada proyectada 20:35, TTL vence 11:04:35Z.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** error factual de la IA (`confidence`) detectado por el usuario; la IA además propone estimaciones de tiempo que son juicio suyo, no del PDF. Control: revisión humana antes de Architect.
- **Artefactos:** diagnóstico, `docs/analisis/01-prompt-analyst.md`.

### I-04 · 2026-11-04 · Consolidación y estructura documental · Claude
- **Petición:** crear skill de bitácora, guardar diagnóstico, preparar `analisis-funcional-v0.2.md`, ordenar `docs/` por fases y crear esta bitácora; sin código ni arquitectura.
- **Salida de la IA:** skill `ai-usage-log`, diagnóstico guardado, v0.2, estructura `docs/`, bitácora.
- **Decisión del usuario:** pendiente de revisión humana.
- **Validación:** pendiente (revisión del usuario de v0.2).
- **Clasificación:** AI-assisted.
- **Riesgo / control:** la IA pudo trasladar incorrectamente los ajustes del usuario; v0.2 marca cada ítem con su origen y deja `[PENDIENTE]` lo no aprobado.
- **Artefactos:** `.claude/skills/ai-usage-log/SKILL.md`, `_bmad-output/planning-artifacts/analisis-funcional-v0.2.md`, `docs/`.

### I-05 · 2026-11-04 · Cierre de decisiones pendientes · Claude, agente Analyst (Mary)
- **Petición:** el usuario fijó criterios para P-1 a P-14 y pidió clasificar qué queda resuelto y qué sigue abierto, sin agregar complejidad ni implementar.
- **Salida de la IA:** sección 11 y 12 de `analisis-funcional-v0.2.md`: estado de cada pendiente, 3 abiertas y lista de validación previa a producción.
- **Decisión del usuario:** criterios C-1 a C-9 APROBADOS por el usuario. Las clasificaciones y propuestas de la IA (valor del instante, definición de "en riesgo", forma de la salida) están pendientes de su revisión.
- **Validación:** pendiente (revisión humana de la sección 11).
- **Clasificación:** AI-assisted.
- **Riesgo / control:** la IA pudo dar por cerrado algo con un supuesto demasiado cómodo; por eso cada cierre dice "con supuesto" y va a la lista de la sección 12. Se señaló una tensión: con los datos del PDF nunca habrá recomendación definitiva.
- **Artefactos:** `_bmad-output/planning-artifacts/analisis-funcional-v0.2.md` (secciones 11–12).

### I-06 · 2026-11-04 · Cierre del análisis funcional · Claude, agente Analyst (Mary)
- **Petición:** el usuario confirmó P-2, P-10, instantes fijos, matriz de fallos y el escenario sintético de 200 min, dejó el tiempo pendiente y pidió cerrar el análisis.
- **Salida de la IA:** v0.2 cerrado (§13) con los 6 criterios a implementar y los límites para el Architect.
- **Decisión del usuario:** ACEPTADA (confirmaciones explícitas en la conversación). Un punto de interpretación de la IA queda por confirmar: el escenario de 200 min se fusionó dentro de AC-2 para mantener 6 criterios.
- **Validación:** revisión humana del usuario de v0.2 (indicó "vamos bien"); contraste de la sección 13 con su confirmación.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** que la IA haya fusionado criterios contra la intención del usuario; se explicó y es reversible.
- **Artefactos:** `_bmad-output/planning-artifacts/analisis-funcional-v0.2.md` (§13).

### I-07 · 2026-11-04 · Diseño de arquitectura y plan · Claude, agente Architect de BMAD (Winston)
- **Petición:** diseñar una solución backend Java + Spring Boot, simple y sin sobreingeniería, con propuesta de arquitectura y plan de implementación acotado en `docs/diseno/`; sin código hasta aprobar.
- **Salida de la IA:** `01-arquitectura-propuesta.md` (paradigma núcleo funcional puro, AD-1 a AD-11, contrato de salida, matriz de fallos, mapa AC→módulo), `02-plan-implementacion.md` (9 pasos, tests por AC, recortes) y `03-diseno-complementario.md` (tabla de clasificación, consulta ambigua, mensaje al pasajero, escrituras, casos borde). Registro de decisiones en `_bmad-output/planning-artifacts/architecture/architecture-Challenge - Latam-2026-11-04/.memlog.md`.
- **Decisión del usuario:** el usuario eligió el modo Fast path, los tres documentos y Spring Boot 4.1.x. **El diseño en sí está PENDIENTE de revisión y aprobación.** La IA dejó 5 puntos por confirmar (AD-4 con R1 y R5 verdaderas, ampliación de AD-5, opción vencida listada y no retirada, variante de 200 min sin menor, versión de Boot).
- **Validación:** la IA verificó en el entorno local Java 21.0.9, Gradle 9.2.1 y, en la caché de Gradle, el BOM de Boot 4.1.1 (Spring Framework 7.0.9, Jackson 3.1.5, JUnit 6.0.3, AssertJ 3.27.7). **No pudo verificar en la web la versión vigente de Boot** (búsqueda bloqueada por una política de la organización). La revisión de la IA con subagentes (gate de revisores del skill) **no se ejecutó**. Los cálculos de TTL (11:04:35Z) y de la conexión (déficit 90 min) se tomaron del análisis v0.2.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** que la IA haya trasladado mal una decisión del análisis o convertido un supuesto en regla (marcados `[SUPUESTO]`); que `policy`/starters de Boot 4 difieran de lo esperado. Control: aprobación humana antes de implementar y paso 0 del plan con test vacío.
- **Artefactos:** `docs/diseno/01-arquitectura-propuesta.md`, `docs/diseno/02-plan-implementacion.md`, `docs/diseno/03-diseno-complementario.md`.

### I-08 · 2026-11-04 · Aprobación de arquitectura y traspaso a Dev · Claude, agente Architect de BMAD (Winston)
- **Petición:** aplicar solo los ajustes aprobados por el usuario, actualizar la bitácora y dejar preparado el traspaso al agente Dev.
- **Salida de la IA:** ajustes en `01`, `02` y `03` (estado aprobado, resolución de los 5 puntos, dos supuestos de escrituras) y nuevo `04-traspaso-dev.md`. Consecuencia que la IA derivó de la clave de idempotencia: tras un 503 solo se reintenta con la misma clave, nunca con una nueva.
- **Decisión del usuario:** **ACEPTADA.** El usuario aprobó la arquitectura y resolvió los 5 puntos de I-07 (ver D-31 a D-33, D-35 a D-39). Aportó los dos supuestos de escrituras y la prioridad de ejecución.
- **Validación:** revisión humana del usuario de `docs/diseno/` ("me parece bien"). Los ajustes se contrastaron contra su mensaje. La versión de Boot sigue sin verificar en web; la gate de revisores con subagentes sigue sin ejecutarse.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** que la IA haya generalizado de más: "reintentos seguros" del caso humano es supuesto del usuario, pero el PDF no dice si `create_human_case` deduplica (anotado para validar con el Eng Lead). Control: ambos quedan marcados como supuestos en `03` §4.
- **Artefactos:** `docs/diseno/01-arquitectura-propuesta.md`, `02-plan-implementacion.md`, `03-diseno-complementario.md`, `04-traspaso-dev.md`.

### I-09 · 2026-11-04 · Implementación del backend · Claude, agente Dev de BMAD (Amelia)
- **Petición:** implementar el backend (Java 21, Spring Boot, Gradle) según `04-traspaso-dev.md`: caso base, AC-1 a AC-6, pruebas y documentación obligatoria; sin escrituras, persistencia, frontend ni integraciones reales; compilar, probar y corregir.
- **Salida de la IA:** proyecto `disruption-companion/` (dominio puro, 4 puertos, fixtures del PDF, adaptador sin PII, servicio, controller con 503), 34 pruebas, `docs/documentacion/README.md`, `docs/documentacion/ai-usage-decision-note.md`, `docs/implementacion/notas-construccion.md`.
- **Decisión del usuario:** **PENDIENTE de revisión.** La IA tomó 8 decisiones menores no previstas en el diseño (ver D-40 a D-45 y `notas-construccion.md`); ninguna está confirmada por el usuario.
- **Validación:** `./gradlew test --rerun-tasks`: 34 en verde, 0 fallos. `bootRun` + `curl` con `at=2026-09-15T11:03:00Z` devolvió el caso base esperado; `at` inválido → 400; 0 coincidencias de datos personales en la respuesta y en `src/main` fuera de fixtures. Boot 4.1.1 resolvió sin recurrir a 4.0.8 (no verificado en web). Errores durante la construcción: un flag de Jackson 2 que no existe en Jackson 3 (corregido); `sed -i` de macOS y un glob de zsh en comandos de shell (corregidos). La gate de revisores con subagentes **no se ejecutó**. Tiempo consumido: No registrado (P-14).
- **Clasificación:** AI-assisted.
- **Riesgo / control:** que las decisiones menores del Dev contradigan la intención del usuario; todas figuran en `notas-construccion.md` y son reversibles. Los tests los escribió la misma IA que el código: un malentendido compartido no se detectaría; control: cifras contrastadas con el PDF y revisión humana pendiente.
- **Artefactos:** `disruption-companion/`, `docs/documentacion/README.md`, `docs/documentacion/ai-usage-decision-note.md`, `docs/implementacion/notas-construccion.md`.

### I-10 · 2026-11-04 · Ajuste de bookingId y cierre de supuestos · Claude, agente Dev de BMAD (Amelia)
- **Petición:** un `bookingId` distinto de `ABC123` no debe devolver datos de esa reserva sino una respuesta controlada; registrar D-40 a D-45 como supuestos con limitaciones; sin ampliar AD-8 ni agregar funcionalidades; guía para que el usuario valide el backend.
- **Salida de la IA:** `BookingNotFoundException`, el adaptador la lanza para otro id, `ApiExceptionHandler` responde 404 `ProblemDetail` (`code=BOOKING_NOT_FOUND`, solo el id); un test nuevo; bitácora y notas actualizadas; guía de validación.
- **Decisión del usuario:** **ACEPTADA.** D-40 a D-45 aprobadas como supuestos de la primera versión (con limitaciones). Pidió explícitamente no ampliar AD-8 ni agregar componentes.
- **Validación:** 35 pruebas en verde (`./gradlew test --rerun-tasks`). Con `bootRun` + `curl`: `XYZ999` → 404; `ABC123` sin cambios (R1 F, R2 F, R3 UNKNOWN, R4 F, R5 T; O1 `CANDIDATE`, O2 `NOT_SUITABLE_GROUP`, O3 `NOT_VERIFIABLE`); `at=11:04:34Z` → O1 `CANDIDATE`, `at=11:04:35Z` → `REQUIRES_REVALIDATION`; `at=ayer` → 400; 0 datos personales. Validación manual del usuario: pendiente.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** el 404 solo distingue por id en el adaptador de fixtures; un adaptador real deberá lanzar la misma excepción. El escenario de 200 min es sintético y solo se prueba por tests, no por `curl`.
- **Artefactos:** `disruption-companion/src/main/java/com/latam/disruption/application/BookingNotFoundException.java`, `adapter/fixture/FixtureAdapter.java`, `api/ApiExceptionHandler.java`, `DependencyFailureTest.java`.

### I-11 · 2026-11-04 · Revisión de la documentación final · Claude, agente Dev de BMAD (Amelia)
- **Petición:** tras su validación manual del backend, revisar README de ingeniería y AI Usage & Decision Note contra el PDF, el análisis, la arquitectura, las pruebas y esta bitácora; claros, concretos y dentro de los límites de páginas; la nota con el proceso real (análisis inicial, ChatGPT, Analyst, Architect, Dev) sin atribuirle validaciones que no hizo; sin presentación ni funcionalidades nuevas.
- **Salida de la IA:** `docs/documentacion/README.md` y `ai-usage-decision-note.md` reescritos. Corrigió una imprecisión del README anterior (decía que un `policy_ref` ajeno «no confirma derecho»; en el código vuelve `NOT_VERIFIABLE` a la opción, y los derechos los bloquean la condición distinta o la política superada). La nota pasa de lista general a una tabla por etapa. Se añadieron los riesgos que faltaban (caso base sin recomendación definitiva con los datos del PDF; código y pruebas de la misma IA).
- **Decisión del usuario:** estaba pendiente al registrar esta entrada; el usuario aprobó ambos documentos después (ver I-12).
- **Validación:** contraste con el código y las pruebas (35, ver I-10), con el análisis v0.2 §13 y con esta bitácora. Páginas medidas renderizando a PDF con Chrome y un estilo compacto propio (10 pt, márgenes 16 mm): README 2 páginas, nota 1. El resultado depende del formato; en otro estilo puede cambiar. El usuario indicó que completó su validación manual; el detalle de sus resultados **No registrado** y así consta en ambos documentos.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** que la IA describa como hecho algo que el usuario no verificó; los documentos dicen explícitamente qué validó la IA y qué el usuario. La nota sustituye el qué se aceptó/modificó por una síntesis de la IA a partir de la bitácora; el usuario debe confirmar que refleja su proceso.
- **Artefactos:** `docs/documentacion/README.md`, `docs/documentacion/ai-usage-decision-note.md`.

### I-12 · 2026-11-04 · Cierre de documentación y portada del repositorio · Claude, agente Dev de BMAD (Amelia)
- **Petición:** README en la raíz como portada con enlaces (sin duplicar); registrar la validación manual del usuario; alinear S-5 con el manejo de fuentes contradictorias aprobado; mantener 2 páginas (README de ingeniería) y 1 (nota); sin cambios de código ni documentación adicional.
- **Salida de la IA:** `README.md` en la raíz; en el README de ingeniería, S-5 reescrito (descarta el `ON_TIME` más antiguo con motivo; el más reciente, sano o degradado, deja el segmento incierto) y validación manual del autor; en la nota, la fila de Dev actualizada y la de documentación final con la revisión del usuario. No se tocó `analisis-funcional-v0.2.md` (cerrado; D-35 ya registra que AD-5 amplía S-5).
- **Decisión del usuario:** **ACEPTADA.** Aprobó el README de ingeniería y la nota de IA como documentos finales (2026-11-04). Aportó el detalle de su validación manual.
- **Validación:** **declarada por el usuario, no reproducida por la IA:** con `curl` comprobó el caso base, los errores 400 y 404, la vigencia de las opciones y que la respuesta no expone datos personales. No confirmó haber ejecutado por su cuenta la suite de pruebas ni el escenario de 200 min, y los documentos no lo afirman. La IA verificó que los enlaces de la portada apuntan a archivos existentes y recomprobó las páginas (ver abajo).
- **Clasificación:** AI-assisted.
- **Riesgo / control:** que la portada contradiga los documentos que enlaza; por eso solo resume y remite. Límite de páginas medido con un estilo propio, no con el formato de entrega.
- **Artefactos:** `README.md`, `docs/documentacion/README.md`, `docs/documentacion/ai-usage-decision-note.md`.

### I-13 · 2026-11-04 · Tiempos iniciales, cierre de documentación y presentación técnica · Claude, agente Dev de BMAD (Amelia)
- **Petición:** registrar los tiempos iniciales (30 min de lectura y análisis personal, 10 min de organización e iteración con ChatGPT) sin duplicarlos con la sesión de Claude que empieza a las 20:15; dejar el README raíz; verificar los límites de README de ingeniería (2 pág.) y nota de IA (1 pág.); preparar un HTML sencillo para los 10 min de presentación, con el orden indicado, sin Google Stitch ni complejidad visual.
- **Salida de la IA:** sección 5 de esta bitácora reescrita con una tabla de tiempos; `docs/presentacion/presentacion-tecnica.html` (un archivo, CSS integrado, sin recursos externos, 7 secciones en el orden pedido, diagrama de flujo, línea de tiempo del pasajero y tablas de reglas y opciones); enlace a la presentación desde `README.md` y `docs/README.md`; frase de tiempo ajustada en la nota de IA. Sin cambios de código.
- **Decisión del usuario:** tiempos **ACEPTADOS como estimación del usuario** (30 + 10 min; inicio con Claude a las 20:15). La presentación: estaba pendiente al registrar esta entrada; el usuario la aprobó después, con dos ajustes (ver I-14).
- **Validación:** el HTML se renderizó en Chrome sin interfaz y se revisó una captura. Encontró y corrigió dos defectos de la línea de tiempo (barras montadas sobre el título y una barra de S1 mal escalada). Las cifras (160 min, 20:35, 19:05, déficit 90, TTL 11:04:35Z, 35 pruebas) se tomaron de los documentos y de la salida real de las pruebas. No se probó en otros navegadores. Límites de páginas: README de ingeniería 2 páginas y nota de IA 1 página, medidos renderizando a PDF con Chrome y un estilo compacto propio (10 pt, márgenes 16 mm); no equivale al formato final de entrega. Pruebas: 35 en verde en la última ejecución.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** el total de tiempo es una **cota superior**: se calculó con el reloj del sistema (21:50 al registrar) menos las 20:15 y no descuenta pausas; el tiempo efectivo real lo confirma el usuario. La presentación resume documentos ya aprobados; si alguno cambia, hay que reflejarlo en el HTML.
- **Artefactos:** `docs/presentacion/presentacion-tecnica.html`, `README.md`, `docs/README.md`, `docs/documentacion/bitacora-ia.md` §5, `docs/documentacion/ai-usage-decision-note.md`.

### I-14 · 2026-11-04 · Ajustes finales de la presentación · Claude, agente Dev de BMAD (Amelia)
- **Petición:** tras revisar la presentación, repartir los tiempos de las secciones para que sumen 10 min (sumaban 11) y diferenciar en la línea de tiempo del pasajero los horarios programados, reportados y proyectados; sin contenido nuevo ni cambio de diseño.
- **Salida de la IA:** en `docs/presentacion/presentacion-tecnica.html`, tiempos 1 / 1,5 / 1,5 / 2 / 1,5 / 1,5 / 1 min (total 10); en la línea de tiempo, marcador cuadrado gris «Programado», círculo verde «Reportado» y círculo punteado rojo «Proyectado», con etiqueta en cada uno y una leyenda de una línea. Se mantienen las mismas horas y cifras. Sin cambios de código ni de otros documentos.
- **Decisión del usuario:** **ACEPTADA.** La presentación queda aprobada por el usuario (2026-11-04).
- **Validación:** suma de tiempos comprobada por script (10,0); render en Chrome sin interfaz y revisión de una captura. No se reprobó en otros navegadores. Los 10 min son una guía de reparto: no se cronometró una exposición real.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** que la distinción dependa solo del color; por eso cada tipo también usa forma, etiqueta y leyenda.
- **Artefactos:** `docs/presentacion/presentacion-tecnica.html`.

### I-15 · 2026-11-04 · Autoría en la presentación · Claude, agente Dev de BMAD (Amelia)
- **Petición:** agregar el nombre del autor bajo el título de la presentación, con la referencia al desafío Software Engineer Senior, sin tocar el resto.
- **Salida de la IA:** una línea en `docs/presentacion/presentacion-tecnica.html` bajo el título: «Diego Garrido Delgado · Desafío técnico Software Engineer Senior». Nada más cambió.
- **Decisión del usuario:** **ACEPTADA** (pedido explícito del usuario, incluido el texto del nombre).
- **Validación:** revisión del diff de la línea insertada; no se volvió a renderizar en un navegador.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** ninguno relevante; cambio de una línea de texto.
- **Artefactos:** `docs/presentacion/presentacion-tecnica.html`.

### I-16 · 2026-11-04 · Preparación de la entrega: Git DRIMO, ignorados, puerto y Dockerfile · Claude, agente Dev de BMAD (Amelia)
- **Petición:** requisito de DRIMO: repositorio GitHub público y URL pública de la API. Configurar Git con la identidad de DRIMO sin afectar la de LATAM, `.gitignore` raíz que excluya el PDF de uso interno, BMAD y temporales, primer commit, Dockerfile Java 21 y puerto por `PORT` para Render; sin cambiar la lógica del backend ni enviar el POST de entrega.
- **Salida de la IA:** `git init -b main` con `user.name = Diego Garrido Delgado` y `user.email = diego.garrido@drimo.com` solo en `.git/config`; `.gitignore` raíz; `disruption-companion/Dockerfile` (build con `./gradlew bootJar -x test` y ejecución con JRE 21) y `.dockerignore`; `server.port: ${PORT:8080}` en `application.yml`; notas en `README.md` y `docs/README.md` sobre el PDF no publicado. Sin cambios en `src/main/java` ni en las pruebas.
- **Decisión del usuario:** **ACEPTADA.** Datos de la cuenta (usuario `diegogarrido-drimo`, repositorio `disruption-companion`, público); Render como plataforma; llave SSH exclusiva para DRIMO, generada por el usuario con passphrase. Dos decisiones del usuario: excluir el PDF y las carpetas de BMAD; no borrar nada local.
- **Validación:** `./gradlew clean build`: compila y 35 pruebas en verde tras los cambios. JAR ejecutable: con `PORT=18082` arranca en 18082 y el caso base responde HTTP 200; sin `PORT`, arranca en 8080 y responde HTTP 200. `git add` revisado antes del commit: sin PDF, `build/`, `.gradle`, `.DS_Store`, `_bmad*`, `.claude`, llaves, `latam.com` ni rutas locales; el único correo es el ficticio del enunciado. Configuración global de Git comprobada con `shasum` antes y después: sin cambios. **No validado:** el Dockerfile no se ejecutó (no hay Docker en esta máquina); se comprobará en el despliegue de Render.
- **Clasificación:** AI-assisted.
- **Riesgo / control:** que el Dockerfile falle en Render por no haberse probado localmente; que el contenido derivado del enunciado interno (fixtures y resúmenes) siga siendo sensible aunque el PDF no se publique: decisión del usuario, a confirmar con DRIMO. Los datos de pasajeros son ficticios.
- **Artefactos:** `.gitignore`, `disruption-companion/Dockerfile`, `disruption-companion/.dockerignore`, `disruption-companion/src/main/resources/application.yml`, `README.md`, `docs/README.md`.

## 2. Decisiones

| ID | Decisión | Estado | Fecha | Origen | Nota |
|---|---|---|---|---|---|
| D-01 | Track Backend/Integración, Java + Spring Boot | APROBADA | No registrada | I-01 | Indicado en el prompt del usuario; justificación formal del track pendiente para el README |
| D-02 | Alcance: vertical slice funcional del backend, acotado, sin sobrecomplicar | APROBADA | 2026-11-04 | I-03/I-04 | Detalle IN/OUT sigue pendiente (P-10) |
| D-03 | Distinguir conexión en riesgo de conexión perdida | APROBADA (principio) | 2026-11-04 | I-03 | Criterio exacto pendiente (P-1, P-13) |
| D-04 | Considerar la vigencia de los datos (TTL, frescura) | APROBADA (principio) | 2026-11-04 | I-03 | Regla concreta pendiente (P-3, P-4) |
| D-05 | Validar información antes de dar recomendaciones | APROBADA (principio) | 2026-11-04 | I-03 | |
| D-06 | Las opciones sí tienen `confidence` | APROBADA (corrección de hecho) | 2026-11-04 | I-03 | Verificable en PDF p.3 |
| D-07 | Reembolso (O3) no se descarta automáticamente por `refundable:false` | APROBADA | 2026-11-04 | I-03 | Tratamiento exacto pendiente (P-6) |
| D-08 | No proponer alternativas que separen al grupo con menor | APROBADA como supuesto | 2026-11-04 | I-03 | Pendiente de validar con PM (P-5) |
| D-09 | Esta etapa no genera código ni arquitectura | APROBADA | 2026-11-04 | I-04 | |
| D-10 | Documentación ordenada en `docs/{analisis,diseno,implementacion,documentacion}` | APROBADA | 2026-11-04 | I-04 | |
| D-11 | Bitácora de IA mantenida con skill `ai-usage-log` | APROBADA | 2026-11-04 | I-04 | |
| D-12 | Sin información crítica o con contradicciones no resolubles, no hay recomendación definitiva | APROBADA | 2026-11-04 | I-05 | C-1 |
| D-13 | Opciones vencidas requieren revalidación | APROBADA | 2026-11-04 | I-05 | C-2 |
| D-14 | No separar automáticamente grupos con menores | APROBADA | 2026-11-04 | I-05 | C-3; sustituye el estado "supuesto" de D-08 en la primera versión; validación con PM sigue en la lista previa a producción |
| D-15 | O3 pendiente de verificar; ni descartada ni ofrecida como beneficio | APROBADA | 2026-11-04 | I-05 | C-4; cierra el tratamiento de D-07 |
| D-16 | Escrituras fuera de la implementación; solo diseño | APROBADA | 2026-11-04 | I-05 | C-5 |
| D-17 | No exponer datos personales innecesarios en respuestas ni logs | APROBADA | 2026-11-04 | I-05 | C-6 |
| D-18 | Pruebas con instante de referencia fijo, reproducibles con los datos del PDF | APROBADA | 2026-11-04 | I-05 | C-7; el valor concreto es propuesta |
| D-19 | Definiciones de PM resueltas con supuestos explícitos y lista de validación previa a producción | APROBADA | 2026-11-04 | I-05 | C-8 |
| D-20 | Casos borde priorizados; no se implementan todos | APROBADA | 2026-11-04 | I-05 | C-9 |
| D-21 | Resultado simple con motivos, que distingue recomendación respaldada e intervención humana; sin estados adicionales | APROBADA | 2026-11-04 | I-06 | Cierra P-2; la forma concreta la define el Architect |
| D-22 | Se implementan AC-1 a AC-6 | APROBADA | 2026-11-04 | I-06 | Cierra P-10 |
| D-23 | Instante fijo `2026-09-15T11:03:00Z` para el caso base y otro posterior al vencimiento para opciones vencidas | APROBADA | 2026-11-04 | I-06 | Cierra P-3 |
| D-24 | Falla de dependencia: respuesta controlada sin inventar información (matriz §6) | APROBADA | 2026-11-04 | I-06 | Cierra P-8 |
| D-25 | Se mantiene la diferencia entre conexión proyectada y confirmada | APROBADA | 2026-11-04 | I-06 | Cierra P-13 en su parte de conexión |
| D-26 | Se incluye el escenario sintético de 200 min (dentro de AC-2) | APROBADA | 2026-11-04 | I-06 | Ubicación dentro de AC-2 es interpretación de la IA |
| D-27 | Análisis funcional v0.2 cerrado; siguiente etapa: Architect de BMAD | APROBADA | 2026-11-04 | I-06 | |
| D-28 | Modo Fast path para el diseño de arquitectura (propuesta con supuestos marcados que el usuario corrige) | APROBADA | 2026-11-04 | I-07 | Elegido por el usuario |
| D-29 | Entregables de diseño: propuesta de arquitectura, plan de implementación acotado y diseño complementario ligero, en `docs/diseno/` | APROBADA | 2026-11-04 | I-07 | Elegido por el usuario |
| D-30 | Spring Boot 4.1.x (4.1.1 en caché local) | APROBADA, versión SIN VERIFICAR en web | 2026-11-04 | I-07 | Elección del usuario; alternativa 4.0.8. Búsqueda web bloqueada |
| D-31 | Paradigma núcleo funcional puro + puertos mínimos, un módulo Gradle, tests sin `@SpringBootTest` | APROBADA | 2026-11-04 | I-07/I-08 | AD-1, AD-11; aprobada con la arquitectura |
| D-32 | Contrato de salida con `recommendationSupported` y `humanInterventionRequired` (R1 y R5 verdaderas ⇒ ambos `true`) | APROBADA | 2026-11-04 | I-07/I-08 | AD-4; el usuario confirmó ambos valores en `true` |
| D-33 | Opción vencida se lista como `REQUIRES_REVALIDATION`, no se retira | APROBADA | 2026-11-04 | I-07/I-08 | AD-7; cierra la duda del análisis §6 regla 3 |
| D-34 | Arquitectura aprobada; Dev implementa con prioridad en backend funcional, pruebas principales y documentación obligatoria | APROBADA | 2026-11-04 | I-08 | Controller y AC-7 son stretch |
| D-35 | Un evento contradictorio más reciente que el DELAY deja el vuelo incierto, aunque la fuente sea degradada | APROBADA | 2026-11-04 | I-08 | AD-5; amplía S-5 |
| D-36 | AC-2 incluye el escenario de 200 min sin menor para probar `humanInterventionRequired=false` | APROBADA | 2026-11-04 | I-08 | Complementa D-26 |
| D-37 | Spring Boot 4.1.x; si hay problemas, volver a 4.0.8 | APROBADA | 2026-11-04 | I-08 | Reafirma D-30; versión sigue sin verificar en web |
| D-38 | Supuesto: crear un caso humano admite reintentos seguros sin confirmación | APROBADA como supuesto | 2026-11-04 | I-08 | Solo diseño (C-5). El PDF no dice si la tool deduplica: validar con el Eng Lead |
| D-39 | Un reintento de la misma solicitud de rebooking mantiene la misma `idempotency_key` | APROBADA | 2026-11-04 | I-08 | Solo diseño. Corolario de la IA: tras un 503 no se reintenta con clave nueva |
| D-40 | Un `DELAY` visto solo en fuente degradada deja el segmento `UNCERTAIN` (`DEGRADED_DELAY_ONLY`) | APROBADA como supuesto (primera versión) | 2026-11-04 | I-09 | Decisión del Dev, confirmada por el usuario 2026-11-04. Limitación: un retraso real visto solo en fuente degradada no genera derecho; exige revisión humana. Sin test dedicado |
| D-41 | `STATUS` con valor desconocido → segmento `UNCERTAIN` | APROBADA como supuesto (primera versión) | 2026-11-04 | I-09 | Decisión del Dev, confirmada por el usuario 2026-11-04. Limitación: cualquier estado distinto de ON_TIME/SCHEDULED bloquea la recomendación. Sin test dedicado |
| D-42 | R3 con varias conexiones: `UNKNOWN` si alguna no es `NO_RISK`, `FALSE` si todas lo son | APROBADA como supuesto (primera versión) | 2026-11-04 | I-09 | Decisión del Dev, confirmada por el usuario 2026-11-04. Limitación: sin efecto en el caso base (una sola conexión); sin test con varias conexiones |
| D-43 | Política con `version` en `superseded_versions` → todas las reglas `UNKNOWN` | APROBADA como supuesto (primera versión) | 2026-11-04 | I-09 | Decisión del Dev, confirmada por el usuario 2026-11-04. Limitación: sin test dedicado; el fixture trae la versión vigente |
| D-44 | Opción sin `seats_available` → `NOT_VERIFIABLE` | APROBADA como supuesto (primera versión) | 2026-11-04 | I-09 | Decisión del Dev, confirmada por el usuario 2026-11-04. Limitación: sin test dedicado; el único caso es el reembolso, que ya es NOT_VERIFIABLE por tipo |
| D-45 | Se implementó el controller con 503 (paso 6, stretch) para cumplir `bootRun` + `curl` | APROBADA como supuesto (primera versión) | 2026-11-04 | I-09 | Decisión del Dev, confirmada por el usuario 2026-11-04. Limitación: filas «200 parcial» de AD-8 no implementadas, documentadas como pendientes; no se amplía AD-8 |
| D-46 | `bookingId` distinto de `ABC123` → 404 controlado `BOOKING_NOT_FOUND`, sin evaluación ni datos de reserva | APROBADA | 2026-11-04 | I-10 | Pedido por el usuario. Limitación: solo existe el adaptador de fixtures; AD-8 no se amplía y los escenarios pendientes quedan documentados |

**Pendientes:**
- Tiempo efectivo definitivo (P-14): ver sección 5; lo confirma el usuario al cerrar.
- Gate de revisión de código con subagentes: no ejecutada.
- Validar con el Eng Lead si `create_human_case` deduplica (D-38) y confirmar la versión de Boot cuando haya acceso web.
- **P-14:** tiempo efectivo definitivo; ver sección 5.
- Confirmar que el escenario de 200 min dentro de AC-2 es lo que querías (D-26).
- Validación previa a producción (no bloquea): `analisis-funcional-v0.2.md` §12.

## 3. Clasificación de pasos (borrador)

| Paso | Clasificación | Razón | Riesgo | Control / fallback |
|---|---|---|---|---|
| Análisis funcional inicial | human + AI-assisted | criterio del dominio del usuario con apoyo de ChatGPT | interpretaciones como hechos | contraste con PDF (I-03) |
| Revisión crítica del análisis | AI-assisted | la IA contrasta y cuestiona; el usuario decide | error factual de la IA | revisión humana, correcciones registradas |
| Aprobación de decisiones | human | responsabilidad del usuario | — | tabla de decisiones |
| Evaluación de reglas de derechos (futuro) | determinístico (propuesto) | verificable y auditable | — | tests de frontera |
| Aprobación de R5/compensaciones (futuro) | human | exigido por política [PDF p.3] | — | no ejecutar escrituras |

| Diseño de arquitectura y plan | AI-assisted | la IA propone paradigma, contrato y plan; el usuario decide | supuestos presentados como reglas; versión de Boot sin verificar | supuestos marcados, 5 puntos resueltos y arquitectura aprobada por el usuario (I-07, I-08) |
| Consulta ambigua (diseño, no implementada) | agentic (diseño) | combina fuentes abiertas | afirmar derechos no validados | solo lectura, validador determinístico contra la evaluación, tope de pasos |
| Mensaje al pasajero (diseño, no implementado) | AI-assisted (diseño) | texto natural sobre hechos validados | información no verificada | plantilla con `policy_ref` obligatorio |

| Implementación del backend y pruebas | AI-assisted | la IA escribe código y tests desde el diseño aprobado; el usuario revisa | tests y código de la misma IA comparten malentendidos; decisiones menores no previstas (D-40 a D-45) | cifras contrastadas con el PDF, `curl` real, búsqueda de PII, revisión humana pendiente (I-09) |
| Evaluación de R1–R5, fuentes, opciones (implementado) | determinístico | código puro sin IA ni reloj, verificable con tests de frontera | política cambia de versión | `UNKNOWN` si la condición o versión no coincide |

Detalle de la clasificación en `docs/documentacion/README.md`. El detalle de diseño está en `docs/diseno/03-diseno-complementario.md` §1.

## 4. Datos que nunca se entregarían a un modelo (borrador)

- Nombre, email, teléfono y documento de pasajeros reales; datos de menores.
- Credenciales, tokens y secretos.
- Datos operacionales reales de la aerolínea fuera del ejemplo ficticio del PDF.

Los datos del PDF son ficticios y se usaron como entradas de prueba. Lista final: pendiente de aprobar (P-9). El diseño (AD-9) propone que ningún modelo ni log reciba nombre, email, teléfono, documento ni nivel de fidelidad.

## 5. Tiempo efectivo

El PDF limita el trabajo a 3 horas efectivas, incluyendo análisis, implementación, validaciones y documentación. Cada tramo se cuenta una sola vez.

| Tramo | Tiempo | Fuente |
|---|---|---|
| Lectura y análisis personal del enunciado | 30 min | Estimación del usuario |
| Organización e iteración inicial con ChatGPT | 10 min | Estimación del usuario. Es previa a las 20:15 (el v0.1 figura a las 20:16, I-02) y no se vuelve a contar en el tramo siguiente |
| **Subtotal inicial** | **40 min** | |
| Trabajo con Claude (Analyst, Architect, Dev) desde las 20:15 | Hasta las 21:50 del 2026-11-04 (reloj del sistema al registrar): 95 min de reloj | Inicio indicado por el usuario. Tiempo efectivo: **No registrado** (se desconocen las pausas) |
| **Referencia total al 2026-11-04 21:50** | **≤ 135 min** (40 + 95) | Cota superior: no descuenta pausas. Quedarían ≥ 45 min de los 180. Lo que se trabaje después se suma al cerrar |

Pendiente (P-14): que el usuario confirme el total efectivo definitivo al cerrar.
