# **ANEXO — ESCALABILIDAD: DE DAO EN MEMORIA A DAO CON MYSQL**

| Objetivo del Anexo Refactorizar la capa Modelo para que el acceso a datos dependa de una **interfaz** y no de una implementación concreta, permitiendo escalar el proyecto de una lista en memoria a una base de datos real (MySQL) sin modificar el Controlador ni las Vistas. |
| :---- |

## **A.1 Marco Teórico — El Problema de Acoplarse a una Implementación**

En la Fase 1, `ProductoServlet` instancia directamente la clase concreta:

```java
private ProductoDAO dao = new ProductoDAO();
```

Esto funciona, pero **acopla el Controlador a una implementación específica** (la lista en memoria). Si TechStore S.A. decide pasar a producción con una base de datos real, habría que modificar el Servlet, lo cual viola el Principio de Inversión de Dependencias (la "D" de SOLID): los módulos de alto nivel (Controller) no deberían depender de detalles de bajo nivel (cómo se guardan los datos), sino de abstracciones.

La solución es introducir una **interfaz** `IProductoDAO` que declare *qué* operaciones existen, sin decir *cómo* se implementan. Así, el proyecto puede tener múltiples implementaciones intercambiables:

| Implementación | Propósito | Cuándo se usa |
| ----- | ----- | ----- |
| `ProductoDAOMemoria` | Prototipado rápido, pruebas unitarias, demos sin BD | Desarrollo inicial (Fase 1 del lab) |
| `ProductoDAOMySQL` | Persistencia real en base de datos relacional | Ambiente de producción / entrega final |

Este es exactamente el mismo principio que usan frameworks como Spring (a través de la inyección de dependencias): el código que *consume* el DAO nunca debería enterarse de cuál implementación está usando.

## **A.2 Actividad 6 — Definir la Interfaz `IProductoDAO`**

Crear la interfaz en el paquete `com.techstore.modelo`:

```java
package com.techstore.modelo;

import java.util.List;

public interface IProductoDAO {

    List<Producto> obtenerTodos();

    Producto obtenerPorId(int id);

    void agregar(Producto p);

    boolean eliminar(int id);
}
```

| Nota importante La interfaz no cambia respecto a los métodos que ya existían en el ProductoDAO original. Esto es intencional: el objetivo de este anexo es mostrar que se puede escalar el almacenamiento de datos sin romper el contrato que el resto de la aplicación espera. |
| :---- |

## **A.3 Actividad 7 — Adaptar la Implementación en Memoria**

Renombrar la clase original `ProductoDAO.java` a `ProductoDAOMemoria.java` y hacer que implemente la interfaz:

```java
package com.techstore.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductoDAOMemoria implements IProductoDAO {

    private static List<Producto> inventario = new ArrayList<>();
    private static AtomicInteger contadorId = new AtomicInteger(1);

    static {
        inventario.add(new Producto(contadorId.getAndIncrement(),
            "Laptop Dell XPS 15", "Computadoras", 1299.99, 15));
        inventario.add(new Producto(contadorId.getAndIncrement(),
            "Mouse Logitech MX Master 3", "Periféricos", 89.99, 42));
        inventario.add(new Producto(contadorId.getAndIncrement(),
            "Monitor Samsung 27\"", "Monitores", 349.99, 8));
        inventario.add(new Producto(contadorId.getAndIncrement(),
            "Teclado Mecánico Keychron K2", "Periféricos", 119.99, 25));
    }

    @Override
    public List<Producto> obtenerTodos() {
        return new ArrayList<>(inventario);
    }

    @Override
    public Producto obtenerPorId(int id) {
        return inventario.stream()
            .filter(p -> p.getId() == id)
            .findFirst().orElse(null);
    }

    @Override
    public void agregar(Producto p) {
        p.setId(contadorId.getAndIncrement());
        inventario.add(p);
    }

    @Override
    public boolean eliminar(int id) {
        return inventario.removeIf(p -> p.getId() == id);
    }
}
```

## **A.4 Actividad 8 — Configurar MySQL para TechStore**

Crear la base de datos y la tabla que respaldará el inventario:

```sql
CREATE DATABASE IF NOT EXISTS techstore_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE techstore_db;

CREATE TABLE producto (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(100)   NOT NULL,
    categoria  VARCHAR(50)    NOT NULL,
    precio     DECIMAL(10,2)  NOT NULL,
    stock      INT            NOT NULL DEFAULT 0
);

INSERT INTO producto (nombre, categoria, precio, stock) VALUES
    ('Laptop Dell XPS 15', 'Computadoras', 1299.99, 15),
    ('Mouse Logitech MX Master 3', 'Periféricos', 89.99, 42),
    ('Monitor Samsung 27"', 'Monitores', 349.99, 8),
    ('Teclado Mecánico Keychron K2', 'Periféricos', 119.99, 25);
```

Agregar el conector JDBC de MySQL al `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.4.0</version>
</dependency>
```

## **A.5 Actividad 9 — Clase de Conexión: `ConexionBD.java`**

Para evitar repetir la lógica de conexión en cada método, se centraliza en una clase utilitaria dentro de `com.techstore.modelo`:

```java
package com.techstore.modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
        "jdbc:mysql://localhost:3306/techstore_db?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String CLAVE   = "tu_clave_aqui";

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
```

| Atención — Seguridad de Credenciales En un proyecto real, usuario y clave NUNCA deben quedar escritos directamente en el código (hardcoded). Lo correcto es leerlos desde un archivo de configuración externo (por ejemplo, un .properties fuera del control de versiones) o variables de entorno. Aquí se simplifica con fines didácticos. |
| :---- |

## **A.6 Actividad 10 — Implementación: `ProductoDAOMySQL.java`**

Esta es la pieza central del anexo: la misma interfaz `IProductoDAO`, pero respaldada por una base de datos real en lugar de una lista en memoria.

| ProductoDAOMySQL.java — Implementación con JDBC |
| :---: |

```java
package com.techstore.modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOMySQL implements IProductoDAO {

    @Override
    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, categoria, precio, stock FROM producto";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Producto obtenerPorId(int id) {
        String sql = "SELECT id, nombre, categoria, precio, stock " +
                     "FROM producto WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void agregar(Producto p) {
        String sql = "INSERT INTO producto (nombre, categoria, precio, stock) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCategoria());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Método privado auxiliar: mapea una fila del ResultSet a Producto ──
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setCategoria(rs.getString("categoria"));
        p.setPrecio(rs.getDouble("precio"));
        p.setStock(rs.getInt("stock"));
        return p;
    }
}
```

| Punto de verificación — Anexo de Escalabilidad Antes de continuar, verifique: (1) Se usa PreparedStatement en todas las consultas (nunca concatenación de Strings) para prevenir inyección SQL. (2) Los recursos (Connection, PreparedStatement, ResultSet) se abren con try-with-resources para garantizar su cierre automático. (3) Las excepciones SQLException se capturan localmente; en un proyecto real conviene registrarlas con un logger en lugar de printStackTrace(). |
| :---- |

## **A.7 El Único Cambio Necesario en el Controlador**

Aquí se evidencia el beneficio de programar contra una interfaz. En `ProductoServlet.java`, el único cambio es la línea de instanciación:

```java
// ANTES (Fase 1 — acoplado a la implementación en memoria)
private ProductoDAO dao = new ProductoDAO();

// DESPUÉS (Anexo — programando contra la interfaz)
private IProductoDAO dao = new ProductoDAOMySQL();
```

Ningún otro método del Servlet (`doGet`, `doPost`, `listar`, `eliminar`) necesita modificarse. Las JSP (`lista.jsp`, `formulario.jsp`) tampoco cambian, porque siguen recibiendo objetos `Producto` con los mismos getters. **Esto es la prueba de que la arquitectura escala correctamente**: el cambio de almacenamiento fue absorbido completamente por la capa Modelo.

## **A.8 Comparación: Antes y Después de la Refactorización**

| Aspecto | Sin Interfaz (Fase 1\) | Con Interfaz (Anexo) |
| ----- | ----- | ----- |
| Acoplamiento | Servlet depende de la clase concreta `ProductoDAO` | Servlet depende de la abstracción `IProductoDAO` |
| Cambiar de almacenamiento | Requiere modificar el Servlet | Solo se cambia una línea (`new ProductoDAOMySQL()`) |
| Pruebas unitarias | Difícil simular datos sin tocar la lista real | Se puede crear un `ProductoDAOFalso` para tests |
| Persistencia de datos | Se pierde al reiniciar el servidor | Persiste en MySQL entre reinicios |
| Preparado para producción | No | Sí (con manejo adecuado de credenciales) |

## **A.9 Preguntas de Reflexión — Escalabilidad**

1. ¿Qué otras implementaciones de `IProductoDAO` podrían crearse a futuro (por ejemplo, conectando a otra fuente de datos)? Mencione al menos dos.

2. En `ProductoDAOMySQL`, cada método abre y cierra su propia conexión. ¿Qué problema de rendimiento podría generar esto bajo alta concurrencia, y cómo lo resolvería un *connection pool* (por ejemplo, HikariCP)?

3. Si quisiera inyectar la implementación del DAO sin usar `new` directamente en el Servlet (por ejemplo, mediante un archivo de configuración), ¿qué patrón de diseño aplicaría?

4. ¿Por qué `obtenerTodos()` en `ProductoDAOMySQL` no necesita devolver una copia defensiva de la lista, a diferencia de la versión en memoria (ver pregunta 3 de la Sección 4.3)?

## **A.10 Reto de Extensión — Persistencia Configurable**

| Reto Avanzado Cree una clase `DAOFactory` con un método estático `crearProductoDAO(String tipo)` que retorne una instancia de `ProductoDAOMemoria` o `ProductoDAOMySQL` según un parámetro ("memoria" o "mysql"). Modifique `ProductoServlet` para leer este parámetro desde el archivo web.xml (usando \<context-param\>) en lugar de tenerlo hardcodeado, de forma que el tipo de persistencia pueda cambiarse sin recompilar el código. |
