package cl.planb.lugares;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity
public class Lugar {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Size(max=80)
    private String nombre;
    @NotBlank @Size(max=300)
    private String descripcion;
    @Min(0) @Max(1000000)
    private int costo;
    @Min(5) @Max(1440)
    private int minutos;
    
    private boolean interior;
    @DecimalMin("-90") @DecimalMax("90")
    private double latitud;
    @DecimalMin("-180") @DecimalMax("180")
    private double longitud;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { this.nombre = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { this.descripcion = value; }
    public int getCosto() { return costo; }
    public void setCosto(int value) { this.costo = value; }
    public int getMinutos() { return minutos; }
    public void setMinutos(int value) { this.minutos = value; }
    public boolean getInterior() { return interior; }
    public void setInterior(boolean value) { this.interior = value; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double value) { this.latitud = value; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double value) { this.longitud = value; }
}
