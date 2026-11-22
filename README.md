# One Punch Hero — Forge 1.20.1

**Estado:** código fuente experimental, compilación y ejecución **no verificadas**.

## Poderes
- Normal Punch: daño y empuje.
- Serious Punch: daño masivo, onda de choque y destrucción de bloques.
- Consecutive Normal Punches: golpes rápidos y daño de área.
- Serious Table Flip: destrucción de terreno y lanzamiento de enemigos.
- Serious Sneeze: cono de daño y destrucción.
- Serious Sideways Jumps: impulso rápido y choque.
- Traje completo: efectos de velocidad, fuerza, salto y resistencia.
- Invocador de Boros: jefe hostil con segunda fase y recompensa.

Los poderes se activan usando sus objetos con clic derecho. Para probarlos, búscalos en el inventario creativo o usa `/give @s saitamamod:serious_punch` y equivalentes. El mod **no** incluye aún modelos 3D personalizados de Boros ni animaciones complejas; las texturas de objetos son originales y provisionales. La destrucción requiere `mobGriefing=true` y está limitada por ataque; **haz copia de seguridad del mundo**.

## Compilación automática sin instalar Java
1. Crea un repositorio GitHub y sube el contenido de esta carpeta a la rama `main`.
2. Abre la pestaña **Actions** y ejecuta **Build Forge mod** si no se ejecutó automáticamente.
3. Si la compilación termina correctamente, descarga el artefacto `one-punch-hero-forge-1.20.1`.
4. Extrae el `.jar` de `build/libs` del artefacto y colócalo en `.minecraft/mods` con Forge 1.20.1.

La compilación puede fallar por incompatibilidades de API aún no comprobadas. Este proyecto no es un `.jar` listo para instalar.
