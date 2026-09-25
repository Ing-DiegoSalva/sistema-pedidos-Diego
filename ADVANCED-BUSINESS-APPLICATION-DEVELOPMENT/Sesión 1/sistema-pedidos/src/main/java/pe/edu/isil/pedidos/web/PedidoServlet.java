package pe.edu.isil.pedidos.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;
import pe.edu.isil.pedidos.service.PedidoService;

/**
 * Servlet que maneja las solicitudes relacionadas con los pedidos.
 */
@WebServlet("/pedidos")
public class PedidoServlet extends HttpServlet {

  @EJB
  private PedidoService pedidoService;

  /**
   * Maneja las solicitudes GET.
   *
   * GET /pedidos
   * GET /pedidos?editar=ID
   */
  @Override
  protected void doGet(
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

    String editar = request.getParameter("editar");

    if (editar != null && !editar.isBlank()) {

      try {

        Long pedidoId = Long.valueOf(editar);

        Pedido pedido =
            pedidoService.buscarPedido(pedidoId);

        if (pedido == null) {

          response.setStatus(
              HttpServletResponse.SC_NOT_FOUND
          );

          renderizarPagina(
              response,
              "El pedido no existe.",
              null,
              null,
              null,
              null
          );

          return;
        }

        renderizarPagina(
            response,
            null,
            null,
            null,
            null,
            pedido
        );

      } catch (NumberFormatException e) {

        response.setStatus(
            HttpServletResponse.SC_BAD_REQUEST
        );

        renderizarPagina(
            response,
            "ID de pedido inválido.",
            null,
            null,
            null,
            null
        );
      }

      return;
    }

    renderizarPagina(
        response,
        null,
        request.getParameter("creado"),
        request.getParameter("actualizado"),
        request.getParameter("eliminado"),
        null
    );
  }

  /**
   * Maneja las solicitudes POST para registrar,
   * actualizar y eliminar pedidos.
   */
  @Override
  protected void doPost(
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

    request.setCharacterEncoding(
        StandardCharsets.UTF_8.name()
    );

    String action = request.getParameter("action");

    try {

      if ("eliminar".equals(action)) {

        Long pedidoId =
            Long.valueOf(request.getParameter("pedidoId"));

        pedidoService.eliminarPedido(pedidoId);

        response.sendRedirect(
            request.getContextPath()
                + "/pedidos?eliminado="
                + pedidoId
        );

        return;
      }

      String cliente =
          request.getParameter("cliente");

      Long productoId =
          Long.valueOf(request.getParameter("productoId"));

      int cantidad =
          Integer.parseInt(request.getParameter("cantidad"));

      if ("actualizar".equals(action)) {

        Long pedidoId =
            Long.valueOf(request.getParameter("pedidoId"));

        Pedido pedido =
            pedidoService.actualizarPedido(
                pedidoId,
                cliente,
                productoId,
                cantidad
            );

        response.sendRedirect(
            request.getContextPath()
                + "/pedidos?actualizado="
                + pedido.getId()
        );

      } else {

        Pedido pedido =
            pedidoService.registrarPedido(
                cliente,
                productoId,
                cantidad
            );

        response.sendRedirect(
            request.getContextPath()
                + "/pedidos?creado="
                + pedido.getId()
        );
      }

    } catch (NumberFormatException e) {

      response.setStatus(
          HttpServletResponse.SC_BAD_REQUEST
      );

      renderizarPagina(
          response,
          "Producto, cantidad o ID inválido.",
          null,
          null,
          null,
          null
      );

    } catch (IllegalArgumentException
             | IllegalStateException e) {

      response.setStatus(
          HttpServletResponse.SC_BAD_REQUEST
      );

      renderizarPagina(
          response,
          e.getMessage(),
          null,
          null,
          null,
          null
      );
    }
  }

  /**
   * Maneja las solicitudes PUT para actualizar pedidos.
   *
   * PUT /pedidos
   */
  @Override
  protected void doPut(
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

    request.setCharacterEncoding(
        StandardCharsets.UTF_8.name()
    );

    try {

      Long pedidoId =
          Long.valueOf(request.getParameter("pedidoId"));

      String cliente =
          request.getParameter("cliente");

      Long productoId =
          Long.valueOf(request.getParameter("productoId"));

      int cantidad =
          Integer.parseInt(request.getParameter("cantidad"));

      Pedido pedido =
          pedidoService.actualizarPedido(
              pedidoId,
              cliente,
              productoId,
              cantidad
          );

      response.setContentType(
          "application/json;charset=UTF-8"
      );

      response.setStatus(
          HttpServletResponse.SC_OK
      );

      response.getWriter().printf(
          "{\"id\":%d,\"mensaje\":\"Pedido actualizado correctamente.\"}",
          pedido.getId()
      );

    } catch (NumberFormatException e) {

      response.setContentType(
          "application/json;charset=UTF-8"
      );

      response.setStatus(
          HttpServletResponse.SC_BAD_REQUEST
      );

      response.getWriter().print(
          "{\"error\":\"Datos numéricos inválidos.\"}"
      );

    } catch (IllegalArgumentException
             | IllegalStateException e) {

      response.setContentType(
          "application/json;charset=UTF-8"
      );

      response.setStatus(
          HttpServletResponse.SC_BAD_REQUEST
      );

      response.getWriter().printf(
          "{\"error\":\"%s\"}",
          escapeHtml(e.getMessage())
      );
    }
  }

  /**
   * Renderiza la página principal del sistema.
   */
  private void renderizarPagina(
      HttpServletResponse response,
      String error,
      String creado,
      String actualizado,
      String eliminado,
      Pedido pedidoEditar)
      throws IOException {

    List<Producto> productos =
        pedidoService.listarProductos();

    List<Pedido> pedidos =
        pedidoService.listarPedidos();

    response.setContentType(
        "text/html;charset=UTF-8"
    );

    try (PrintWriter out = response.getWriter()) {

      out.println("""
          <!doctype html>
          <html lang="es">
          <head>
            <meta charset="UTF-8">
            <meta name="viewport"
                  content="width=device-width, initial-scale=1">

            <title>Sistema de Pedidos - ISIL</title>

            <style>
              body {
                font-family: Arial, sans-serif;
                max-width: 1100px;
                margin: 32px auto;
                padding: 0 16px;
              }

              form {
                display: grid;
                grid-template-columns: 2fr 2fr 1fr auto;
                gap: 12px;
                align-items: end;
              }

              label {
                display: flex;
                flex-direction: column;
                gap: 6px;
                font-weight: 600;
              }

              input,
              select,
              button {
                padding: 10px;
                font-size: 14px;
              }

              button {
                cursor: pointer;
              }

              table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 24px;
              }

              th,
              td {
                border: 1px solid #ccc;
                padding: 9px;
                text-align: left;
              }

              th {
                background: #f2f2f2;
              }

              .error {
                background: #ffe7e7;
                border: 1px solid #d33;
                padding: 10px;
                margin: 16px 0;
              }

              .success {
                background: #e7ffe7;
                border: 1px solid #3a3;
                padding: 10px;
                margin: 16px 0;
              }

              .nota {
                background: #f5f5f5;
                padding: 10px;
                margin: 16px 0;
              }

              .acciones {
                display: flex;
                gap: 8px;
                align-items: center;
              }

              .acciones a {
                display: inline-block;
                padding: 7px 10px;
                background: #eee;
                color: #000;
                text-decoration: none;
                border: 1px solid #bbb;
              }

              .acciones form {
                display: inline;
              }

              .acciones button {
                padding: 7px 10px;
                background: #eee;
                border: 1px solid #bbb;
              }
            </style>
          </head>

          <body>

            <h1>Sistema de Pedidos</h1>

            <p class="nota">
              Flujo: Navegador → PedidoServlet → PedidoService
              (EJB) → JPA → H2.
            </p>
          """);

      if (error != null) {

        out.printf(
            "<div class=\"error\">%s</div>%n",
            escapeHtml(error)
        );
      }

      if (creado != null && !creado.isBlank()) {

        out.printf(
            "<div class=\"success\">Pedido #%s registrado correctamente.</div>%n",
            escapeHtml(creado)
        );
      }

      if (actualizado != null && !actualizado.isBlank()) {

        out.printf(
            "<div class=\"success\">Pedido #%s actualizado correctamente.</div>%n",
            escapeHtml(actualizado)
        );
      }

      if (eliminado != null && !eliminado.isBlank()) {

        out.printf(
            "<div class=\"success\">Pedido #%s eliminado correctamente y stock repuesto.</div>%n",
            escapeHtml(eliminado)
        );
      }

      /*
       * FORMULARIO DE EDICIÓN
       *
       * El formulario mantiene method="post" como respaldo,
       * pero JavaScript intercepta el envío y realiza un PUT.
       */
      if (pedidoEditar != null) {

        out.println("""
            <h2>Editar pedido</h2>

            <form id="pedidoEditForm" method="post">

              <input
                type="hidden"
                name="action"
                value="actualizar">

              <input
                type="hidden"
                name="pedidoId"
            """);

        out.printf(
            "    value=\"%d\">%n",
            pedidoEditar.getId()
        );

        out.println("""
              <label>
                Cliente
                <input
                  name="cliente"
                  required
                  maxlength="120"
            """);

        out.printf(
            "      value=\"%s\">%n",
            escapeHtml(pedidoEditar.getCliente())
        );

        out.println("""
              </label>

              <label>
                Producto
                <select name="productoId" required>
            """);

        for (Producto producto : productos) {

          String selected =
              producto.getId().equals(
                  pedidoEditar.getProducto().getId()
              )
                  ? " selected"
                  : "";

          out.printf(
              "<option value=\"%d\"%s>%s - S/ %s - stock: %d</option>%n",
              producto.getId(),
              selected,
              escapeHtml(producto.getNombre()),
              producto.getPrecio().toPlainString(),
              producto.getStock()
          );
        }

        out.println("""
                </select>
              </label>

              <label>
                Cantidad
                <input
                  name="cantidad"
                  type="number"
                  min="1"
            """);

        out.printf(
            "      value=\"%d\" required>%n",
            pedidoEditar.getCantidad()
        );

        out.println("""
              </label>

              <div>
                <button type="submit">
                  Guardar cambios
                </button>
              </div>

            </form>

            <p>
              <a href="pedidos">
                Cancelar edición
              </a>
            </p>

            <script>
              const formularioEdicion =
                  document.getElementById("pedidoEditForm");

              formularioEdicion.addEventListener(
                  "submit",
                  async function(event) {

                    event.preventDefault();

                    const datos =
                        new URLSearchParams(
                            new FormData(formularioEdicion)
                        );

                    try {

                      const respuesta =
                          await fetch(
                              window.location.pathname,
                              {
                                method: "PUT",
                                headers: {
                                  "Content-Type":
                                      "application/x-www-form-urlencoded;charset=UTF-8"
                                },
                                body: datos
                              }
                          );

                      const texto =
                          await respuesta.text();

                      if (!respuesta.ok) {
                        throw new Error(
                            texto ||
                            "No se pudo actualizar el pedido."
                        );
                      }

                      const resultado =
                          JSON.parse(texto);

                      window.location.href =
                          window.location.pathname
                          + "?actualizado="
                          + encodeURIComponent(resultado.id);

                    } catch (error) {

                      alert(
                          "Error al actualizar el pedido: "
                          + error.message
                      );
                    }
                  }
              );
            </script>
            """);

      } else {

        /*
         * FORMULARIO DE REGISTRO
         */
        out.println("""
            <h2>Registrar pedido</h2>

            <form method="post">

              <input
                type="hidden"
                name="action"
                value="registrar">

              <label>
                Cliente
                <input
                  name="cliente"
                  required
                  maxlength="120"
                  placeholder="Ej. Ana Torres">
              </label>

              <label>
                Producto
                <select name="productoId" required>
            """);

        for (Producto producto : productos) {

          out.printf(
              "<option value=\"%d\">%s - S/ %s - stock: %d</option>%n",
              producto.getId(),
              escapeHtml(producto.getNombre()),
              producto.getPrecio().toPlainString(),
              producto.getStock()
          );
        }

        out.println("""
                </select>
              </label>

              <label>
                Cantidad
                <input
                  name="cantidad"
                  type="number"
                  min="1"
                  value="1"
                  required>
              </label>

              <button type="submit">
                Registrar
              </button>

            </form>
            """);
      }

      /*
       * TABLA DE PEDIDOS
       */
      out.println("""
          <h2>Pedidos registrados</h2>

          <table>

            <thead>
              <tr>
                <th>ID</th>
                <th>Cliente</th>
                <th>Producto</th>
                <th>Cantidad</th>
                <th>Total</th>
                <th>Fecha</th>
                <th>Acciones</th>
              </tr>
            </thead>

            <tbody>
          """);

      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern(
              "dd/MM/yyyy HH:mm:ss"
          );

      for (Pedido pedido : pedidos) {

        out.printf(
            """
            <tr>

              <td>%d</td>

              <td>%s</td>

              <td>%s</td>

              <td>%d</td>

              <td>S/ %s</td>

              <td>%s</td>

              <td>
                <div class="acciones">

                  <a href="pedidos?editar=%d">
                    Editar
                  </a>

                  <form
                    method="post"
                    onsubmit="return confirm('¿Está seguro de eliminar el pedido #%d?');">

                    <input
                      type="hidden"
                      name="action"
                      value="eliminar">

                    <input
                      type="hidden"
                      name="pedidoId"
                      value="%d">

                    <button type="submit">
                      Eliminar
                    </button>

                  </form>

                </div>
              </td>

            </tr>
            """,

            pedido.getId(),

            escapeHtml(
                pedido.getCliente()
            ),

            escapeHtml(
                pedido.getProducto().getNombre()
            ),

            pedido.getCantidad(),

            pedido.getTotal().toPlainString(),

            pedido.getFecha().format(formatter),

            pedido.getId(),

            pedido.getId(),

            pedido.getId()
        );
      }

      if (pedidos.isEmpty()) {

        out.println(
            "<tr><td colspan=\"7\">"
                + "Aún no hay pedidos."
                + "</td></tr>"
        );
      }

      out.println("""
            </tbody>

          </table>

          </body>
          </html>
          """);
    }
  }

  /**
   * Escapa caracteres especiales para HTML.
   */
  private String escapeHtml(String value) {

    if (value == null) {
      return "";
    }

    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}