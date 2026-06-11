package com.techstore;

import com.techstore.modelo.ProductoDAO;

public class MainTest {
    public static void main(String[] args) {
        ProductoDAO dao = new ProductoDAO();
        
        // Imprimir el tamaño del inventario
        System.out.println("Tamaño del inventario: " + dao.obtenerTodos().size());
        
        // Imprimir el contenido de la lista
        System.out.println("Lista de productos: " + dao.obtenerTodos().toString());
    }
}