# KioscoFacil
 
Programa de gestión simple para kioscos, hecho en Java. Pensado para que cualquier kiosco pueda llevar sus ventas sin pagar una suscripción mensual.
 
> **Estado:** en desarrollo inicial.
 
## ¿Por qué KioscoFacil?
 
Muchos programas para kioscos cobran una suscripción mensual, y en los meses de ventas bajas esa cuota pesa. KioscoFacil apunta a otro modelo:
 
- Licencia de pago único, sin mensualidades.
- Mantenimiento aparte, solo cuando se necesita.
- Simple de usar, hecho a partir de lo que piden los dueños de kioscos.
## Características
 
- [ ] Registro de ventas
- [ ] Catálogo de productos
- [ ] Lectura de códigos de barras con escáner
- [ ] Resumen de ventas del día
*(Marca con `[x]` lo que ya funcione.)*
 
## Requisitos
 
- JDK 17 o superior
- Maven 3.8 o superior
- NetBeans (opcional, el proyecto se crea y se abre desde ahí)
- Escáner de códigos de barras (opcional, para cargar productos y vender más rápido)
## Instalación y ejecución
 
```bash
git clone https://github.com/<tu-usuario>/KioscoFacil.git
cd KioscoFacil
mvn clean package
java -jar target/KioscoFacil-1.0-SNAPSHOT.jar
```
 
También puedes abrir la carpeta del proyecto directamente en NetBeans (*File → Open Project*) y ejecutarlo con *Run*.
 
## Estructura del proyecto
 
```
KioscoFacil/
├── pom.xml
└── src/
    ├── main/java/        # código fuente
    └── test/java/        # pruebas
```
 
## Hoja de ruta
 
1. Versión base con ventas y productos.
2. Beta con unos 5 kioscos, mejorando según sus comentarios.
3. Primera versión para vender.
4. A futuro: un programa para dueños con varios kioscos o empleados, que muestre lo vendido en el día, en total o por kiosco.
## Contribuir
 
Por ahora el proyecto lo desarrolla una sola persona. Si probás el programa y tenés ideas o encontrás errores, abrí un *issue*.
 
## Licencia
 
Por definir.
 
