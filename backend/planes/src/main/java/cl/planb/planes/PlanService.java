package cl.planb.planes;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
@Service
public class PlanService {
    private final PlanRepository repository;
    public PlanService(PlanRepository repository) { this.repository = repository; }
    public List<Plan> listar() { return repository.findAll(Sort.by("id").descending()); }
    public Plan buscar(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado")); }
    @Transactional
    public Plan guardar(Long id, Plan value) {
        if (id != null) buscar(id);

        try { java.time.LocalDate.parse(value.getFecha()); }
        catch (java.time.format.DateTimeParseException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha inválida"); }
        if (!value.getEstado().equals("REALIZADO") && value.getValoracion() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes valorar un plan realizado");
        }

        value.setId(id);
        return repository.save(value);
    }
    @Transactional
    public void eliminar(Long id) { repository.delete(buscar(id)); }
}
