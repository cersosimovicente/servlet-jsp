<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>TechStore — Nuevo Producto</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
    </head>
    <body>
        <header><h1>Agregar Nuevo Producto</h1></header>
        <main>
            <form action="${pageContext.request.contextPath}/productos" method="post" class="formulario">
                <div class="campo">
                    <label for="nombre">Nombre del Producto:</label>
                    <input type="text" id="nombre" name="nombre" required maxlength="100" placeholder="Ej: Laptop Dell XPS">
                </div>
                <div class="campo">
                    <label for="categoria">Categoría:</label>
                    <select id="categoria" name="categoria" required>
                        <option value="">-- Seleccionar --</option>
                        <option value="Computadoras">Computadoras</option>
                        <option value="Monitores">Monitores</option>
                        <option value="Periféricos">Periféricos</option>
                        <option value="Almacenamiento">Almacenamiento</option>
                        <option value="Redes">Redes</option>
                    </select>
                </div>
                <div class="campo">
                    <label for="precio">Precio (USD):</label>
                    <input type="number" id="precio" name="precio" step="0.01" min="0.01" required placeholder="0.00">
                </div>
                <div class="campo">
                    <label for="stock">Cantidad en Stock:</label>
                    <input type="number" id="stock" name="stock" min="0" required placeholder="0">
                </div>
                <div class="botones">
                    <button type="submit" class="btn-guardar">Guardar Producto</button>
                    <a href="${pageContext.request.contextPath}/productos" class="btn-cancelar">Cancelar</a>
                </div>
            </form>
        </main>
    </body>
</html>