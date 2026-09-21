package org.taller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class TareaServiceIntegrationTest {

  private TareaService tareaService;

  @BeforeEach
  void setUp() {
    TareaRepository repositorioReal = new InMemoryTareaRepository();
    tareaService = new TareaService(repositorioReal);
  }

  @Test
  void crearYLuegoListarTareasReflejaLaTareaCreada() {
    tareaService.crearTarea("1", "Preparar presentación");

    List<Tarea> tareas = tareaService.listarTareas();

    assertEquals(1, tareas.size());
    assertEquals("Preparar presentación", tareas.getFirst().getTitulo());
  }

  @Test
  void crearCompletarYListarReflejaElEstadoActualizado() {
    tareaService.crearTarea("2", "Corregir bug");

    tareaService.completarTarea("2");

    List<Tarea> tareas = tareaService.listarTareas();
    assertTrue(tareas.getFirst().isCompletada());
  }

  @Test
  void eliminarTareaLaRemueveDelRepositorioReal() {
    tareaService.crearTarea("3", "Actualizar dependencias");

    tareaService.eliminarTarea("3");

    assertTrue(tareaService.listarTareas().isEmpty());
  }

  @Test
  void completarTareaQueNoExisteEnElRepositorioRealLanzaExcepcion() {
    assertThrows(NoSuchElementException.class,
        () -> tareaService.completarTarea("no-existe"));
  }
}