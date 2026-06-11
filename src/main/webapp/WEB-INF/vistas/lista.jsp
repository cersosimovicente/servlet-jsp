<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>TechStore — Inventario</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<header>
    <h1>TechStore S.A. — Gestión de Inventario</h1>
</header>
<main>
    <div class="acciones">
        <a href="${pageContext.request.contextPath}/productos?accion=nuevo" class="btn-nuevo">+ Agregar Producto</a>
    </div>

    <!-- Mensaje si el inventario está vacío -->
    <c:if test="${empty productos}">
        <p class="aviso">No hay productos registrados en el sistema.</p>
    </c:if>

    <!-- Tabla de productos -->
    <c:if test="${not empty productos}">
        <table class="tabla-inventario">
            <thead>
                <tr>
                    <th>ID</th><th>Nombre</th><th>Categoría</th>
                    <th>Precio</th><th>Stock</th><th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="prod" items="${productos}">
                    <tr class="${prod.stock < 10 ? 'stock-bajo' : ''}">
                        <td>${prod.id}</td>
                        <td>${prod.nombre}</td>
                        <td>${prod.categoria}</td>
                        <!-- Formateo limpio como moneda según región -->
                        <td><fmt:formatNumber value="${prod.precio}" type="currency"/></td>
                        <td>${prod.stock}</td>
                        <td>
                            <!-- Espacio corregido antes de onclick -->
                            <a href="${pageContext.request.contextPath}/productos?accion=eliminar&id=${prod.id}" 
                               onclick="return confirm('¿Eliminar ${prod.nombre}?')" 
                               class="btn-eliminar">Eliminar</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:if>
</main>
</body>
</html>
