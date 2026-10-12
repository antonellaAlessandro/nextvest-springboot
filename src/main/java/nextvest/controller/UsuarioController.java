package nextvest.controller;

import nextvest.dto.LoginRequest;
import nextvest.dto.LoginResponse;
import nextvest.dto.UsuarioResponse;
import nextvest.model.Usuario;
import nextvest.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevo = usuarioService.registrar(usuario);
            return ResponseEntity.ok(usuarioService.toResponse(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(usuarioService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(usuarioService.toResponse(u)))
                .orElse(ResponseEntity.notFound().build());
    }
}