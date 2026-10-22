package com.monideal.monideal.repository;

import com.monideal.monideal.model.HistorialVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialVentaRepository extends JpaRepository<HistorialVenta, Long> {

    boolean existsByPedidoIdPedido(Long idPedido);
}
