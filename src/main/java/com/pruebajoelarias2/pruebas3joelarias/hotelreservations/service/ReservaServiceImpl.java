package com.pruebajoelarias2.pruebas3joelarias.hotelreservations.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.pruebajoelarias2.pruebas3joelarias.hotelreservations.dto.ReservaDTO;
import com.pruebajoelarias2.pruebas3joelarias.hotelreservations.model.Habitacion;
import com.pruebajoelarias2.pruebas3joelarias.hotelreservations.model.Reserva;
import com.pruebajoelarias2.pruebas3joelarias.hotelreservations.repository.HabitacionRepository;
import com.pruebajoelarias2.pruebas3joelarias.hotelreservations.repository.ReservaRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    // Utils
    private ReservaDTO convertToDTO(Reserva reserva) {
        return new ReservaDTO(reserva.getId(), reserva.getNombreCliente(), reserva.getHabitacion().getId());
    }



    // GET ALL RESERVAS
    @Override
    public List<ReservaDTO> getAllReservas() {
        List<ReservaDTO> reservas = reservaRepository.findAll()
        .stream()
        .map(this::convertToDTO) 
        .collect(Collectors.toList());

        if (reservas.isEmpty()) {
            throw new EntityNotFoundException("No se encontraron reservas.");
        }
        return reservas;
    }

    // GET BY ID RESERVA
    @Override
    public Optional<ReservaDTO> getReservaById(Long id) {
        return reservaRepository.findById(id)
                .map(this::convertToDTO);
    }

    // POST
    @Override
    public ReservaDTO saveReserva(ReservaDTO reservaDTO) {
        Habitacion habitacion = habitacionRepository.findById(reservaDTO.getHabitacionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habitación no encontrada"));
    
        if (!habitacion.isDisponible()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La habitación no está disponible.");
        }
    
        // Crear nueva reserva
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setNombreCliente(reservaDTO.getNombreCliente());
        nuevaReserva.setHabitacion(habitacion);
    
        // Cambiar el estado de la habitación a no disponible
        habitacion.setDisponible(false);
        habitacionRepository.save(habitacion);
    
        // Guardar la reserva
        Reserva reservaGuardada = reservaRepository.save(nuevaReserva);
    
        // Utiliza el método de conversión
        return convertToDTO(reservaGuardada);
    }
    
    // DELETE
    @Transactional
    @Override
    public void deleteReserva(Long id) {
        // Verificar si la reserva existe
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    
        // Obtener la habitación asociada a la reserva
        Habitacion habitacion = reserva.getHabitacion();
    
        // Marcar la habitación como disponible
        habitacion.setDisponible(true);
        habitacionRepository.save(habitacion);
    
        // Eliminar la reserva
        reservaRepository.deleteById(id);
    }
    
    // PUT
    @Override
    public ReservaDTO updateReserva(Long id, ReservaDTO reservaDTO) {
        Reserva reservaExistente = reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    
        if (reservaDTO.getNombreCliente() != null && !reservaDTO.getNombreCliente().isEmpty()) {
            reservaExistente.setNombreCliente(reservaDTO.getNombreCliente());
        }
    
        if (reservaDTO.getHabitacionId() != null && !reservaDTO.getHabitacionId().equals(reservaExistente.getHabitacion().getId())) {
            Habitacion nuevaHabitacion = habitacionRepository.findById(reservaDTO.getHabitacionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habitación no encontrada"));
    
            if (!nuevaHabitacion.isDisponible()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva habitación no está disponible.");
            }
    
            Habitacion habitacionAnterior = reservaExistente.getHabitacion();
            habitacionAnterior.setDisponible(true);
            habitacionRepository.save(habitacionAnterior);
    
            reservaExistente.setHabitacion(nuevaHabitacion);
            nuevaHabitacion.setDisponible(false);
            habitacionRepository.save(nuevaHabitacion);
        }
    
        Reserva reservaActualizada = reservaRepository.save(reservaExistente);
    
        return convertToDTO(reservaActualizada);
    }
    
}