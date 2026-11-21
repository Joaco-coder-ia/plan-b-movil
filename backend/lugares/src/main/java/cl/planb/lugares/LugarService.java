package cl.planb.lugares;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
@Service
public class LugarService {
    private final LugarRepository repository;
    public LugarService(LugarRepository repository) { this.repository = repository; }
    public List<Lugar> listar() { return repository.findAll(Sort.by("id").descending()); }
    public Lugar buscar(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado")); }
    @Transactional
    public Lugar guardar(Long id, Lugar value) {
        if (id != null) buscar(id);

        value.setId(id);
        return repository.save(value);
    }
    @Transactional
    public void eliminar(Long id) { repository.delete(buscar(id)); }
}
