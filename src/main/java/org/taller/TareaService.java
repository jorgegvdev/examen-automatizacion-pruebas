package org.taller;

import java.util.List;
import java.util.NoSuchElementException;

public class TareaService {

  private final TareaRepository repositorio;

  public TareaService(TareaRepository repositorio) {
    this.repositorio = repositorio;
  }

  public Tarea crearTarea(String id, String titulo) {
    if (titulo == null || titulo.isBlank()) {
      throw new IllegalArgumentException("El título de la tarea no puede estar vacío");
    }
    Tarea tarea = new Tarea(id, titulo);
    return repositorio.guardar(tarea);
  }

  public List<Tarea> listarTareas() {
    return repositorio.listarTodas();
  }

  public Tarea completarTarea(String id) {
    Tarea tarea = repositorio.buscarPorId(id)
        .orElseThrow(() -> new NoSuchElementException("Tarea no encontrada: " + id));
    tarea.marcarCompletada();
    repositorio.guardar(tarea);
    return tarea;
  }

  public void eliminarTarea(String id) {
    repositorio.eliminar(id);
  }
}
