# Disruption Companion

Backend de evaluación para el desafío técnico **Caso A · Disruption Companion** (track Backend/Integración). Dado un vuelo con modificaciones, determina qué le corresponde al pasajero según las reglas de política, con motivos y evidencia, y dice cuándo debe intervenir una persona. Solo lee y evalúa: no ejecuta escrituras ni acciones de alto impacto.

## Qué se implementó
- Servicio Spring Boot (Java 21, Gradle) con un endpoint de lectura que evalúa el caso base del PDF (reserva `ABC123`) con reglas R1–R5 explícitas, lógica de tres valores y resolución de fuentes contradictorias.
- Vigencia de opciones (TTL), grupo con menor sin separar, trazabilidad y ausencia de datos personales en la salida.
- Criterios AC-1 a AC-6 con 35 pruebas. Los 4 puertos de lectura usan fixtures del PDF; no hay persistencia, frontend, autenticación ni IA en runtime.

## Cómo ejecutarlo
Requiere Java 21.

```bash
cd disruption-companion
./gradlew test --rerun-tasks     # 35 pruebas
./gradlew bootRun
curl "localhost:8080/api/v1/bookings/ABC123/disruption-evaluation?at=2026-09-15T11:03:00Z"
```

Con ese instante, el caso base devuelve `recommendationSupported=false` y `humanInterventionRequired=true`. La guía de qué esperar y los límites del alcance están en el README de ingeniería.

## Documentos principales
| Documento | Para qué |
|---|---|
| [README de ingeniería](docs/documentacion/README.md) | Decisiones, alcance, supuestos, riesgos y validación |
| [AI Usage & Decision Note](docs/documentacion/ai-usage-decision-note.md) | Uso de IA por etapa y decisiones propias |
| [Análisis funcional v0.2](docs/analisis/analisis-funcional-v0.2.md) | Hechos del PDF, supuestos y criterios de aceptación |
| [Arquitectura](docs/diseno/01-arquitectura-propuesta.md) | Decisiones AD-1 a AD-11 y contrato de salida |
| [Diseño complementario](docs/diseno/03-diseno-complementario.md) | Consulta ambigua, mensaje al pasajero y escrituras (solo diseño) |
| [Notas de implementación](docs/implementacion/notas-construccion.md) | Evidencia de pruebas y limitaciones conocidas |
| [Bitácora de IA](docs/documentacion/bitacora-ia.md) | Registro de interacciones y decisiones |
| [Presentación técnica](docs/presentacion/presentacion-tecnica.html) | Guion visual de 10 min; se abre en el navegador (un solo HTML, sin dependencias) |

El resto de `docs/` (plan de implementación, traspaso) está indexado en [docs/README.md](docs/README.md).

**Notas:** el enunciado original no se publica por ser de uso interno, aunque los documentos lo citan como fuente. Los datos de pasajeros de los fixtures y de las pruebas son ficticios y vienen de ese enunciado. La presentación es un HTML: en GitHub se ve como código; para verla, descárgalo y ábrelo en el navegador.
