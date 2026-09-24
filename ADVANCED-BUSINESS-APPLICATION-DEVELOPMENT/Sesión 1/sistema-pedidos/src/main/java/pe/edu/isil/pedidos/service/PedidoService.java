package pe.edu.isil.pedidos.service;

import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio EJB que maneja la lógica de negocio relacionada con los pedidos.
 */
@Stateless
public class PedidoService {

  @PersistenceContext(
      unitName = "PedidosPU"
  )
  private EntityManager entityManager;

  @TransactionAttribute(
      TransactionAttributeType.REQUIRED
  )
  public Pedido registrarPedido(String cliente, Long productoId, int cantidad) {
    if (cliente == null ||
        cliente.isBlank()) {
      throw new IllegalArgumentException("El cliente es obligatorio.");
    }

    if (productoId == null) {
      throw new IllegalArgumentException("Debe seleccionar un producto.");
    }

    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }

    Producto producto = entityManager.find(Producto.class, productoId);

    if (producto == null) {
      throw new IllegalArgumentException("El producto no existe.");
    }

    producto.descontarStock(cantidad);

    BigDecimal total = producto.getPrecio()
        .multiply(BigDecimal.valueOf(cantidad));

    Pedido pedido = new Pedido(
        cliente.trim(),
        producto,
        cantidad,
        total
    );

    entityManager.persist(pedido);

    return pedido;
  }

  /**
   * Busca un pedido por su ID.
   *
   * @param pedidoId ID del pedido.
   * @return El pedido encontrado o null si no existe.
   */
  @TransactionAttribute(
      TransactionAttributeType.SUPPORTS
  )
  public Pedido buscarPedido(Long pedidoId) {
    if (pedidoId == null) {
      return null;
    }

    return entityManager.find(Pedido.class, pedidoId);
  }

  /**
   * Actualiza un pedido y mantiene la consistencia del stock.
   *
   * @param pedidoId ID del pedido a actualizar.
   * @param cliente Nuevo cliente.
   * @param productoId Nuevo producto.
   * @param cantidad Nueva cantidad.
   * @return El pedido actualizado.
   */
  @TransactionAttribute(
      TransactionAttributeType.REQUIRED
  )
  public Pedido actualizarPedido(
      Long pedidoId,
      String cliente,
      Long productoId,
      int cantidad) {

    if (pedidoId == null) {
      throw new IllegalArgumentException("El pedido es obligatorio.");
    }

    if (cliente == null ||
        cliente.isBlank()) {
      throw new IllegalArgumentException("El cliente es obligatorio.");
    }

    if (productoId == null) {
      throw new IllegalArgumentException("Debe seleccionar un producto.");
    }

    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }

    Pedido pedido =
        entityManager.find(Pedido.class, pedidoId);

    if (pedido == null) {
      throw new IllegalArgumentException("El pedido no existe.");
    }

    Producto productoAnterior =
        pedido.getProducto();

    int cantidadAnterior =
        pedido.getCantidad();

    Producto productoNuevo =
        entityManager.find(Producto.class, productoId);

    if (productoNuevo == null) {
      throw new IllegalArgumentException("El producto no existe.");
    }

    if (productoAnterior.getId().equals(productoNuevo.getId())) {

      productoAnterior.reponerStock(cantidadAnterior);
      productoAnterior.descontarStock(cantidad);

    } else {

      productoAnterior.reponerStock(cantidadAnterior);
      productoNuevo.descontarStock(cantidad);
    }

    BigDecimal total =
        productoNuevo.getPrecio()
            .multiply(BigDecimal.valueOf(cantidad));

    pedido.actualizar(
        cliente.trim(),
        productoNuevo,
        cantidad,
        total
    );

    return pedido;
  }

  /**
   * Elimina un pedido y repone la cantidad al stock.
   *
   * @param pedidoId ID del pedido a eliminar.
   */
  @TransactionAttribute(
      TransactionAttributeType.REQUIRED
  )
  public void eliminarPedido(Long pedidoId) {

    if (pedidoId == null) {
      throw new IllegalArgumentException(
          "El pedido es obligatorio."
      );
    }

    Pedido pedido =
        entityManager.find(Pedido.class, pedidoId);

    if (pedido == null) {
      throw new IllegalArgumentException(
          "El pedido no existe."
      );
    }

    Producto producto =
        pedido.getProducto();

    producto.reponerStock(
        pedido.getCantidad()
    );

    entityManager.remove(pedido);
  }
  @TransactionAttribute(
      TransactionAttributeType.REQUIRED
  )
  public List<Producto> listarProductos() {
    inicializarProductosSiEsNecesario();

    return entityManager
        .createQuery(
            """
            select p
            from Producto p
            order by p.id
            """,
            Producto.class
        )
        .getResultList();
  }

  @TransactionAttribute(
      TransactionAttributeType.SUPPORTS
  )
  public List<Pedido> listarPedidos() {
    return entityManager
        .createQuery(
            """
            select p
            from Pedido p
            join fetch p.producto
            order by p.id desc
            """,
            Pedido.class
        )
        .getResultList();
  }

  private void inicializarProductosSiEsNecesario() {
    Long cantidad =
        entityManager
            .createQuery(
                """
                select count(p)
                from Producto p
                """,
                Long.class
            )
            .getSingleResult();

    if (cantidad == 0) {
      entityManager.persist(
          new Producto(
              "Laptop",
              new BigDecimal("2500.00"),
              5
          )
      );

      entityManager.persist(
          new Producto(
              "Monitor",
              new BigDecimal("850.00"),
              8
          )
      );

      entityManager.persist(
          new Producto(
              "Teclado",
              new BigDecimal("120.00"),
              15
          )
      );
    }
  }

}