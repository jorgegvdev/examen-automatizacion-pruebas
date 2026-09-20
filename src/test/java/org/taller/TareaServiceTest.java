package org.taller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

  @Mock
  private TareaRepository repositorioMock;

  private TareaService tareaService;

  @BeforeEach
  void setUp() {
    tareaService = new TareaService(repositorioMock);
  }

  @Test
  void crearTareaConTituloValidoGuardaLaTareaEnElRepositorio() {
    when(repositorioMock.guardar(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

    Tarea resultado = tareaService.crearTarea("1", "Escribir informe");

    assertEquals("Escribir informe", resultado.getTitulo());
    verify(repositorioMock, times(1)).guardar(any(Tarea.class));
  }

  @Test
  void crearTareaConTituloVacioLanzaExcepcionYNoLlamaAlRepositorio() {
    assertThrows(IllegalArgumentException.class,
        () -> tareaService.crearTarea("2", "   "));

    verify(repositorioMock, never()).guardar(any());
  }

  @Test
  void completarTareaInexistenteLanzaExcepcion() {
    when(repositorioMock.buscarPorId("99")).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class,
        () -> tareaService.completarTarea("99"));
  }

  @Test
  void completarTareaExistenteMarcaComoCompletadaYLaGuarda() {
    Tarea tarea = new Tarea("3", "Revisar código");
    when(repositorioMock.buscarPorId("3")).thenReturn(Optional.of(tarea));

    Tarea resultado = tareaService.completarTarea("3");

    assertTrue(resultado.isCompletada());
    verify(repositorioMock).guardar(tarea);
  }
}