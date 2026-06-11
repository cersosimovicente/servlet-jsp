package com.techstore.controlador;


import com.techstore.modelo.Producto;
import com.techstore.modelo.ProductoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


// Mapeo mediante anotación (alternativa a web.xml)
@WebServlet("/productos")
public class ProductoServlet extends HttpServlet {


    private ProductoDAO dao = new ProductoDAO();


    // ── GET: mostrar lista o formulario ──────────────────────────
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {


        String accion = req.getParameter("accion");
        if (accion == null) accion = "listar";


        switch (accion) {
            case "listar":
                listar(req, resp);
                break;
            case "nuevo":
                req.getRequestDispatcher(
                    "/WEB-INF/vistas/formulario.jsp")
                   .forward(req, resp);
                break;
            case "eliminar":
                eliminar(req, resp);
                break;
            default:
                listar(req, resp);
        }
    }
        // ── POST: procesar formulario ─────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {
        // Patrón PRG: Post → Redirect → Get
        req.setCharacterEncoding("UTF-8");
        String nombre    = req.getParameter("nombre");
        String categoria = req.getParameter("categoria");
        double precio    = Double.parseDouble(req.getParameter("precio"));
        int    stock     = Integer.parseInt(req.getParameter("stock"));
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setPrecio(precio);
        p.setStock(stock);
        dao.agregar(p);
        // Redirect evita reenvío del formulario (PRG)
        resp.sendRedirect(req.getContextPath() + "/productos");
    }
    // ── Métodos privados auxiliares ───────────────────────────────
    private void listar(HttpServletRequest req,
                        HttpServletResponse resp)
            throws ServletException, IOException {
        List<Producto> lista = dao.obtenerTodos();
        req.setAttribute("productos", lista);
        req.getRequestDispatcher("/WEB-INF/vistas/lista.jsp")
           .forward(req, resp);
    }
    private void eliminar(HttpServletRequest req,
                          HttpServletResponse resp)
            throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        dao.eliminar(id);
        resp.sendRedirect(req.getContextPath() + "/productos");
    }
}