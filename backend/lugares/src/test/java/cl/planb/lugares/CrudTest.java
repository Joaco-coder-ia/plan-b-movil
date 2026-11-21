package cl.planb.lugares;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class CrudTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    String payload = "{\"nombre\": \"Biblioteca de ejemplo\", \"descripcion\": \"Lugar ficticio para estudiar\", \"costo\": 0, \"minutos\": 30, \"interior\": true, \"latitud\": -33.45, \"longitud\": -70.66}";
    @Test void cicloCompleto() throws Exception {
        String response = mvc.perform(post("/api/lugares").contentType("application/json").content(payload)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).get("id").asLong();
        mvc.perform(get("/api/lugares/"+id)).andExpect(status().isOk());
        mvc.perform(put("/api/lugares/"+id).contentType("application/json").content(payload)).andExpect(status().isOk());
        mvc.perform(get("/api/lugares")).andExpect(status().isOk());
        mvc.perform(delete("/api/lugares/"+id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/lugares/"+id)).andExpect(status().isNotFound());
    }
    @Test void rechazaDatosInvalidos() throws Exception {
        mvc.perform(post("/api/lugares").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
    }
    @Test void noActualizaUnRegistroInexistente() throws Exception {
        mvc.perform(put("/api/lugares/999999").contentType("application/json").content(payload)).andExpect(status().isNotFound());
    }
}
