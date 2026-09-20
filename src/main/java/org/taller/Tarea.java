package org.taller;

public class Tarea {

  private final String id;
  private final String titulo;
  private boolean completada;

  public Tarea(String id, String titulo) {
    this.id = id;
    this.titulo = titulo;
    this.completada = false;
  }

  public String getId() {
    return id;
  }

  public String getTitulo() {
    return titulo;
  }

  public boolean isCompletada() {
    return completada;
  }

  public void marcarCompletada() {
    this.completada = true;
  }
}
