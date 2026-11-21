package cl.planb.planes;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController
@RequestMapping("/api/planes")
public class PlanController {
    private final PlanService service;
    public PlanController(PlanService service) { this.service = service; }
    @GetMapping public List<Plan> listar() { return service.listar(); }
    @GetMapping("/{id}") public Plan buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Plan crear(@Valid @RequestBody Plan value) { return service.guardar(null, value); }
    @PutMapping("/{id}") public Plan editar(@PathVariable Long id, @Valid @RequestBody Plan value) { return service.guardar(id, value); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
