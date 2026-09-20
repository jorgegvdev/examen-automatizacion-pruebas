package org.taller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryTareaRepository implements TareaRepository {

  private final Map<String, Tarea> almacen = new LinkedHashMap<>();

  @Override
  public Tarea guardar(Tarea tarea) {
    almacen.put(tarea.getId(), tarea);
    return tarea;
  }

  @Override
  public Optional<Tarea> buscarPorId(String id) {
    return Optional.ofNullable(almacen.get(id));
  }

  @Override
  public List<Tarea> listarTodas() {
    return new ArrayList<>(almacen.values());
  }

  @Override
  public void eliminar(String id) {
    almacen.remove(id);
  }
}
