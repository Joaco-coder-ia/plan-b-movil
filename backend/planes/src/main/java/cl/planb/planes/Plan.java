package cl.planb.planes;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity
public class Plan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Size(max=80)
    private String titulo;
    @NotBlank @Size(max=80)
    private String lugarNombre;
    @NotBlank @Pattern(regexp="[0-9]{4}-[0-9]{2}-[0-9]{2}")
    private String fecha;
    @NotBlank @Pattern(regexp="PENDIENTE|REALIZADO|CANCELADO")
    private String estado;
    @Size(max=300)
    private String notas;
    @Min(0) @Max(5)
    private int valoracion;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String value) { this.titulo = value; }
    public String getLugarNombre() { return lugarNombre; }
    public void setLugarNombre(String value) { this.lugarNombre = value; }
    public String getFecha() { return fecha; }
    public void setFecha(String value) { this.fecha = value; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { this.estado = value; }
    public String getNotas() { return notas; }
    public void setNotas(String value) { this.notas = value; }
    public int getValoracion() { return valoracion; }
    public void setValoracion(int value) { this.valoracion = value; }
}
