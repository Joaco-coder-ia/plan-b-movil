package cl.planb.lugares;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController
@RequestMapping("/api/lugares")
public class LugarController {
    private final LugarService service;
    public LugarController(LugarService service) { this.service = service; }
    @GetMapping public List<Lugar> listar() { return service.listar(); }
    @GetMapping("/{id}") public Lugar buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Lugar crear(@Valid @RequestBody Lugar value) { return service.guardar(null, value); }
    @PutMapping("/{id}") public Lugar editar(@PathVariable Long id, @Valid @RequestBody Lugar value) { return service.guardar(id, value); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
