package nextvest.controller;

import nextvest.dto.LoginRequest;
import nextvest.dto.LoginResponse;
import nextvest.dto.UserResponse;
import nextvest.model.User;
import nextvest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User created = userService.register(user);
            return ResponseEntity.ok(userService.toResponse(created));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("mensaje", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(userService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Integer id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(userService.toResponse(u)))
                .orElse(ResponseEntity.notFound().build());
    }
}