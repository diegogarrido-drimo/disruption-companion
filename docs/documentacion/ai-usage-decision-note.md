# AI Usage & Decision Note

Detalle completo, con fechas y decisiones D-01 a D-46: `docs/documentacion/bitacora-ia.md`. No hay IA en runtime.

## Herramientas y proceso
| Etapa | Herramienta y uso | Qué acepté, modifiqué o descarté | Cómo se validó |
|---|---|---|---|
| Análisis inicial | Yo, sin IA | Borrador propio del enunciado | Sin registro |
| Refinamiento | ChatGPT: mejoró mi borrador (v0.1) | Lo usé como borrador a contrastar; qué cambios conservé: no registrado | Revisión crítica de la etapa siguiente |
| Revisión crítica | Claude, agente Analyst de BMAD: contrastó v0.1 con el PDF y propuso criterios | **Acepté con cambios.** Corregí un error de la IA (las opciones sí traen `confidence`). El reembolso no se descarta por `refundable:false`. «No separar al grupo» queda como supuesto, no como bloqueo. Acoté el alcance a AC-1…AC-6 | Contraste mío con el PDF (detalle no registrado). La IA recalculó 17:10, 20:35 y 11:04:35Z |
| Diseño | Claude, agente Architect: arquitectura (AD-1…AD-11) y plan | **Aprobé** el diseño y resolví 5 puntos abiertos (R1+R5 → ambos booleanos `true`; contradicción de fuente degradada → incierto; opciones vencidas listadas; variante de 200 min sin menor; Boot 4.1.x con plan B). **Descarté** el enum de estados y las escrituras en código | Revisé `docs/diseno/`. La IA comprobó Java, Gradle y el BOM en la caché local. Boot no se verificó en web |
| Implementación | Claude, agente Dev: código, 35 pruebas, documentación | Acepté 6 decisiones menores del Dev como supuestos de la primera versión (D-40 a D-45). **Modifiqué** una salida: un `bookingId` desconocido responde 404. **Descarté** ampliar AD-8 y agregar funcionalidades | Pruebas y `curl` ejecutados por la IA. Yo validé a mano con `curl`: caso base, errores 400 y 404, vigencia de opciones y ausencia de datos personales en la respuesta |
| Documentación final | Claude: revisión de este README y de esta nota contra PDF, código y bitácora | Corrigió una imprecisión (`policy_ref`) | Revisé y aprobé ambos documentos |

## Decisiones que tomé yo
Track Backend; alcance AC-1…AC-6; salida de dos booleanos con motivos; sin escrituras, persistencia ni frontend; versión de Boot y plan B; supuestos de escrituras (reintento seguro de caso humano, misma `idempotency_key` en reintentos, solo diseño); cerrar las pendientes con supuestos explícitos y una lista de validación previa a producción.

## Lo que la IA no verificó
La búsqueda web de la versión de Boot estaba bloqueada. La revisión de código con subagentes no se ejecutó. Código y pruebas salen de la misma IA. El tiempo efectivo exacto no está registrado: estimé 40 min iniciales (lectura y ChatGPT) y trabajé con Claude desde las 20:15 (`bitacora-ia.md` §5).

## Información que nunca entregaría a un modelo
Nombre, email, teléfono, documento y datos de menores de pasajeros reales; credenciales y secretos; datos operacionales reales de la aerolínea. Los datos del PDF son ficticios y se usaron solo como entradas de prueba. El dominio y la salida solo manejan IDs.
