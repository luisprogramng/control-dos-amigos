# Control Dos Amigos

App Android nativa (Kotlin + Jetpack Compose + Room) para llevar el control de productos de una cafetería. Sin usuarios, con categorías, productos y movimientos (entradas/salidas) que ajustan el stock automáticamente.

## Pasos desde Termux (móvil, sin computadora)

1. Descomprime este ZIP en tu almacenamiento y muévelo a la carpeta donde trabajas con Termux, por ejemplo:
   ```
   unzip control-dos-amigos.zip -d ~/storage/shared/proyectos/
   cd ~/storage/shared/proyectos/control-dos-amigos
   ```
   (o donde prefieras dentro del entorno de Termux)

2. Crea el repositorio nuevo en GitHub llamado `control-dos-amigos` (vacío, sin README).

3. Inicializa y sube el proyecto:
   ```
   git init
   git add .
   git commit -m "Primera versión de Control Dos Amigos"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/control-dos-amigos.git
   git push -u origin main
   ```

4. Entra a la pestaña **Actions** de tu repo en GitHub (desde el navegador del teléfono). El workflow se dispara solo al hacer push y compila el APK en la nube.

5. Cuando termine (ícono verde ✅), entra al run finalizado y descarga el artifact **control-dos-amigos-apk**: ahí está tu `app-debug.apk` listo para instalar.
