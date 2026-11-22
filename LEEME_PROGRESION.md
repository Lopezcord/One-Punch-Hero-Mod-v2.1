# One Punch Hero — progresión V1 (Forge 1.20.1)

## Qué incluye
- Fragmento de héroe: posibilidad de obtenerlo al matar monstruos (1/12); Boros suelta 8.
- Núcleo de héroe: receta con 8 fragmentos y una Nether Star.
- Limitador Roto: receta especial, uso único por personaje; activa la progresión.
- Niveles 1–100, experiencia al matar criaturas, mejoras mediante efectos y desbloqueos de los seis objetos de poder.
- Datos guardados en NBT persistente, también al morir.
- En creativo se permite probar los poderes sin desbloquearlos.

## Limitaciones importantes
- **Esta versión NO usa aún la API de Palladium para registrar poderes ni su interfaz de habilidades.** Palladium y GeckoLib siguen declarados como dependencias, pero esta etapa implementa la progresión en Forge.
- No hay modelos 3D ni animaciones GeckoLib nuevas.
- No se ha compilado ni probado aquí: el build de GitHub puede detectar errores que habrá que corregir.
- El aumento de vida se aplica mediante efecto Health Boost, no modificadores permanentes de atributo.

## Instalación de la actualización
1. Conserva una copia del repositorio actual.
2. Descomprime el ZIP y sube su contenido a la raíz del repositorio, sustituyendo los archivos correspondientes.
3. Verifica que `.github/workflows/build.yml` siga siendo el que compila correctamente en tu repositorio.
4. Comprueba Actions; si compila, descarga el artefacto y usa el JAR principal.
5. Prueba primero en un mundo de copia de seguridad.

## Recetas
Núcleo: FFF / FNF / FFF (F=fragmento, N=Nether Star).
Limitador: NSN / DCD / BTB (N=netherita, S=Nether Star, D=diamante, C=núcleo, B=vara de blaze, T=tótem).

## Próxima fase
Crear una implementación real de poderes en Palladium y sincronizarla con el progreso del jugador.
