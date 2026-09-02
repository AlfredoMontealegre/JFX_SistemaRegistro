package ni.uam.edu.distribuidoragueguense.Dao;

import ni.uam.edu.distribuidoragueguense.Interfaces.CRUD;
import ni.uam.edu.distribuidoragueguense.Modelo.Trabajador;

import java.util.ArrayList;
import java.util.List;

public class DistribuidoraDao implements CRUD<Trabajador> {
    private final List<Trabajador> trabajadores;

    public DistribuidoraDao() {
        this.trabajadores = new ArrayList<>();
    }

    @Override
    public void agregar(Trabajador entidad) {
        trabajadores.add(entidad);
    }

    @Override
    public void actualizar(Trabajador entidad) {
        for (int i = 0; i < trabajadores.size(); i++) {
            if (trabajadores.get(i).getUsuario().equalsIgnoreCase(entidad.getUsuario())) {
                trabajadores.set(i, entidad);
                return;
            }
        }
    }

    @Override
    public void eliminar(Trabajador entidad) {
        trabajadores.remove(entidad);
    }

    @Override
    public List<Trabajador> obtenerRegistros() {
        return trabajadores;
    }

    public boolean existeUsuario(String usuario) {
        return trabajadores.stream()
                .anyMatch(t -> t.getUsuario().equalsIgnoreCase(usuario));
    }
}