# One Punch Hero — etapa de dependencias

Esta etapa **solo declara** Palladium y GeckoLib como dependencias de compilación y ejecución. NO convierte todavía los objetos de poder en habilidades Palladium, NO registra modelos GeckoLib y NO añade animaciones. El comportamiento de combate previo permanece sin cambios.

Versiones objetivo: Minecraft Forge 1.20.1, Java 17, Palladium 4.5.8+1.20.1-forge (Modrinth TQrmq6gr), GeckoLib Forge 1.20.1 4.8.5.

1. Haz copia de tu repositorio y mundo.
2. Sube build.gradle y src/main/resources/META-INF/mods.toml a GitHub respetando sus rutas. El resto de archivos no cambian.
3. Ejecuta Actions. Si falla, envía el registro del primer paso fallido. **No está compilado ni probado aquí.**
4. Si compila, instala los dos mods Forge en el mismo perfil y prueba la carga en un mundo de pruebas.

NOTA: el identificador interno de Palladium en mods.toml se ha supuesto `palladium` y debe verificarse contra el JAR concreto antes de declarar la integración terminada. La versión y dependencias deben fijarse después de validar la compilación.

Siguiente fase real: migrar una habilidad Normal Punch a la definición data-driven de Palladium y a un callback de servidor para reutilizar el daño existente; después modelar Boros y traje con archivos .geo.json/.animation.json y renderizadores GeckoLib.
