package com.pruebajoelarias2.pruebas3joelarias.petorders.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.pruebajoelarias2.pruebas3joelarias.petorders.controller.OrdenController;
import com.pruebajoelarias2.pruebas3joelarias.petorders.dto.DetalleOrdenDTO;
import com.pruebajoelarias2.pruebas3joelarias.petorders.dto.OrdenDTO;
import com.pruebajoelarias2.pruebas3joelarias.petorders.dto.ProductoDTO;
import com.pruebajoelarias2.pruebas3joelarias.petorders.model.DetalleOrden;
import com.pruebajoelarias2.pruebas3joelarias.petorders.model.Orden;
import com.pruebajoelarias2.pruebas3joelarias.petorders.model.Producto;
import com.pruebajoelarias2.pruebas3joelarias.petorders.repository.OrdenRepository;
import com.pruebajoelarias2.pruebas3joelarias.petorders.repository.ProductoRepository;

import jakarta.transaction.Transactional;

@Service
public class OrdenServiceImpl implements OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    // UTILS
    // Método para convertir Orden a OrdenDTO
    private OrdenDTO convertirAOrdenDTO(Orden orden) {
        OrdenDTO ordenDTO = new OrdenDTO(
                orden.getId(),
                orden.getNombreComprador(),
                orden.getDireccion(),
                orden.getFecha(),
                orden.getEstado(),
                orden.getDetalles().stream()
                    .map(detalle -> new DetalleOrdenDTO(
                            detalle.getId(),
                            new ProductoDTO(
                                    detalle.getProducto().getId(),
                                    detalle.getProducto().getNombre(),
                                    detalle.getProducto().getPrecio()
                            ),
                            detalle.getCantidad(),
                            detalle.getSubtotal()))
                    .collect(Collectors.toList())
        );

        // Agregar enlace a sí mismo
        ordenDTO.add(linkTo(methodOn(OrdenController.class).getOrderById(ordenDTO.getId())).withSelfRel());

        // Agregar enlace para actualizar
        ordenDTO.add(linkTo(methodOn(OrdenController.class).actualizarOrden(ordenDTO.getId(), ordenDTO)).withRel("update"));

        // Agregar enlace para eliminar
        ordenDTO.add(linkTo(methodOn(OrdenController.class).deleteOrderById(ordenDTO.getId())).withRel("delete"));

        return ordenDTO;
    }

    // GET all orders
    @Override
    @Transactional
    public List<OrdenDTO> getAllOrders() {
        return ordenRepository.findAll()
            .stream()
            .map(this::convertirAOrdenDTO) 
            .collect(Collectors.toList());
    }
    
    // GET By ID
    @Override
    @Transactional
    public Optional<OrdenDTO> getOrderById(Long id) {
        return ordenRepository.findById(id)
                .map(this::convertirAOrdenDTO); 
    }

    // DELETE Orden v2
    @Override
    public void deleteOrderById(Long id) {
        if (ordenRepository.existsById(id)) {
            ordenRepository.deleteById(id); 
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada");
        }
    }

    // CREATE Orden
    @Override
    public OrdenDTO createOrder(OrdenDTO ordenDTO) {
        // Crear una nueva instancia de Orden
        Orden nuevaOrden = new Orden();
        nuevaOrden.setNombreComprador(ordenDTO.getNombreComprador());
        nuevaOrden.setDireccion(ordenDTO.getDireccion());
        nuevaOrden.setFecha(ordenDTO.getFecha());
        nuevaOrden.setEstado(ordenDTO.getEstado());

        // Agregar los detalles de la orden
        List<DetalleOrden> detalles = ordenDTO.getDetalles().stream().map(detalleDTO -> {
            Producto producto = productoRepository.findById(detalleDTO.getProducto().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

            DetalleOrden detalleOrden = new DetalleOrden();
            detalleOrden.setProducto(producto);
            detalleOrden.setCantidad(detalleDTO.getCantidad());
            detalleOrden.setSubtotal(detalleDTO.getSubtotal());
            detalleOrden.setOrden(nuevaOrden);

            return detalleOrden;
        }).collect(Collectors.toList());

        nuevaOrden.setDetalles(detalles);

        // Guardar la orden en la base de datos
        Orden ordenGuardada = ordenRepository.save(nuevaOrden);

        // Convertir la entidad Orden a OrdenDTO y devolverla
        return convertirAOrdenDTO(ordenGuardada);
    }
    
    // UPDATE Orden
    @Override
    public OrdenDTO updateOrder(Long id, OrdenDTO ordenDTO) {
        // Buscar la orden 
        Orden ordenExistente = ordenRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden con ID " + id + " no fue encontrada"));

        // Actualizar los campos 
        ordenExistente.setNombreComprador(ordenDTO.getNombreComprador());
        ordenExistente.setDireccion(ordenDTO.getDireccion());
        ordenExistente.setFecha(ordenDTO.getFecha());
        ordenExistente.setEstado(ordenDTO.getEstado());

        // Actualizar los detalles 
        List<DetalleOrden> detallesActualizados = ordenDTO.getDetalles().stream().map(detalleDTO -> {
            Producto producto = productoRepository.findById(detalleDTO.getProducto().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

            DetalleOrden detalleOrden = new DetalleOrden();
            detalleOrden.setProducto(producto);
            detalleOrden.setCantidad(detalleDTO.getCantidad());
            detalleOrden.setSubtotal(detalleDTO.getSubtotal());
            detalleOrden.setOrden(ordenExistente);

            return detalleOrden;
        }).collect(Collectors.toList());

        ordenExistente.setDetalles(detallesActualizados);

        // Guardar la orden actualizada
        Orden ordenGuardada = ordenRepository.save(ordenExistente);

        // Devolver la orden actualizada como DTO
        return convertirAOrdenDTO(ordenGuardada);
    }
}
