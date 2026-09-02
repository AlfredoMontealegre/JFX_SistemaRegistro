package ni.uam.edu.distribuidoragueguense.Interfaces;

import java.util.List;

public interface CRUD<T> {
    void agregar(T entidad);
    void actualizar(T entidad);
    void eliminar(T entidad);
    List<T> obtenerRegistros();
}