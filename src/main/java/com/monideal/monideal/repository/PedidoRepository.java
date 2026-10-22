package com.monideal.monideal.repository;

import com.monideal.monideal.model.EstadoPedido;
import com.monideal.monideal.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Pedidos de un estado, del más antiguo al más nuevo (el primero en llegar sale primero). */
    List<Pedido> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado);
}
