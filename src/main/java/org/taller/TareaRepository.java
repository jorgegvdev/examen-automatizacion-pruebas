package org.taller;

import java.util.List;
import java.util.Optional;

public interface TareaRepository {
  Tarea guardar(Tarea tarea);
  Optional<Tarea> buscarPorId(String id);
  List<Tarea> listarTodas();
  void eliminar(String id);
}
