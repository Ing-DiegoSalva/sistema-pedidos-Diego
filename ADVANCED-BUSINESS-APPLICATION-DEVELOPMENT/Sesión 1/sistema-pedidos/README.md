\# Sistema de Pedidos



Aplicación web desarrollada con Java, Maven, Jakarta Servlet, EJB, JPA/Hibernate, H2 y WildFly.



\## Tecnologías



\* Java 21

\* Maven

\* Jakarta Servlet 6.1

\* EJB

\* JPA / Hibernate

\* H2

\* WildFly 41.0.0.Final



\## Arquitectura



```text

Navegador

&#x20;  ↓

PedidoServlet

&#x20;  ↓

PedidoService (EJB)

&#x20;  ↓

JPA / Hibernate

&#x20;  ↓

Base de datos H2

```



\## Funcionalidades



Actualmente el sistema permite:



\* Registrar pedidos.

\* Listar pedidos.

\* Editar pedidos.

\* Eliminar pedidos.

\* Reponer stock al eliminar un pedido.

\* Actualizar pedidos mediante HTTP PUT.

\* Mostrar mensajes de éxito y error.



\## Cómo ejecutar el proyecto



\### 1. Compilar



Desde la carpeta raíz del proyecto:



```powershell

mvn clean package

```



El archivo generado es:



```text

target\\sistema-pedidos.war

```



\### 2. Ejecutar WildFly



El proyecto fue trabajado con WildFly 41.0.0.Final.



La instancia utilizada durante las pruebas trabajó con:



```text

HTTP: 8081

HTTPS: 8443

Administración: 9990

```



La ruta habitual de la aplicación es:



```text

http://localhost:8081/sistema-pedidos/pedidos

```



\### 3. Uso



Desde la página principal se puede:



\* Registrar un nuevo pedido.

\* Ver los pedidos registrados.

\* Seleccionar \*\*Editar\*\* para modificar un pedido.

\* Seleccionar \*\*Eliminar\*\* para eliminar un pedido.



\## Estado actual del proyecto



\### Completado



\* Proyecto Maven compilando correctamente.

\* `PedidoServlet` funcional.

\* Registro de pedidos mediante POST.

\* Eliminación de pedidos mediante POST.

\* Edición de pedidos.

\* Implementación de `doPut()` en `PedidoServlet`.

\* Prueba manual del endpoint PUT mediante `curl`.

\* Prueba exitosa de actualización mediante PUT con respuesta:



```text

HTTP/1.1 200 OK

```



Respuesta obtenida:



```json

{

&#x20; "id": 1,

&#x20; "mensaje": "Pedido actualizado correctamente."

}

```



\* Formulario de edición preparado para enviar la actualización mediante `fetch()` utilizando HTTP PUT.

\* Compilación final comprobada:



```text

BUILD SUCCESS

```



\## Pendiente



\* Volver a levantar WildFly después de la última compilación.

\* Verificar que la nueva versión del WAR se despliegue correctamente.

\* Probar desde el navegador el flujo completo:



```text

Editar → modificar datos → Guardar cambios → fetch() → PUT → actualización en pantalla

```



\* Verificar que el cambio realizado mediante PUT quede reflejado correctamente en la tabla.

\* Revisar el entorno de WildFly, ya que `mvn clean` eliminó la carpeta `target` que contenía la instalación utilizada anteriormente.

\* Realizar una prueba final de todas las funcionalidades antes de considerar el proyecto terminado.



\## Nota de la última sesión



El proyecto fue compilado correctamente con Maven.



La última compilación terminó con:



```text

BUILD SUCCESS

```



Sin embargo, todavía no se realizó la prueba final de la interfaz después de integrar `fetch()` con `PUT`, porque la instalación de WildFly que estaba dentro de `target\\server` ya no estaba disponible después de ejecutar `mvn clean`.



\## Último punto conocido



El backend PUT ya fue probado anteriormente con éxito mediante:



```powershell

curl.exe -i -X PUT "http://localhost:8081/sistema-pedidos/pedidos?pedidoId=1\&cliente=Cliente%20Prueba\&productoId=1\&cantidad=2"

```



Resultado:



```text

HTTP/1.1 200 OK

```



Por lo tanto, el siguiente trabajo debe centrarse en recuperar/ubicar WildFly y realizar la prueba final desde el navegador.



