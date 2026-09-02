package ni.uam.edu.distribuidoragueguense.Modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trabajador {
    private String nombres;
    private String apellidos;
    private String usuario;
    private String password;
    private String cargo;
    private String area;
    private LocalDate fechaContratacion;
    private String tipoContrato;
    private String beneficios;

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + cargo + " - " + area + ")";
    }
}