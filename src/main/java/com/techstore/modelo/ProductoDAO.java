package com.techstore.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductoDAO {

    // Simula una base de datos en memoria
    private static List<Producto> inventario = new ArrayList<>();
    private static AtomicInteger contadorId = new AtomicInteger(1);

    // Datos iniciales de TechStore
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

    // CRUD: Obtener todos los productos
    public List<Producto> obtenerTodos() {
        return new ArrayList<>(inventario);
    }

    // CRUD: Buscar por ID
    public Producto obtenerPorId(int id) {
        return inventario.stream()
                .filter(p -> p.getId() == id)
                .findFirst().orElse(null);
    }

    // CRUD: Agregar producto
    public void agregar(Producto p) {
        p.setId(contadorId.getAndIncrement());
        inventario.add(p);
    }

    // CRUD: Eliminar por ID
    public boolean eliminar(int id) {
        return inventario.removeIf(p -> p.getId() == id);
    }
    
    

}
