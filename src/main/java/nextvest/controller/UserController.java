package nextvest.controller;

import nextvest.dto.LoginRequest;
import nextvest.dto.LoginResponse;
import nextvest.dto.UserResponse;
import nextvest.exception.NotFoundException;
import nextvest.model.User;
import nextvest.security.AccessGuard;
import nextvest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AccessGuard accessGuard;

    public UserController(UserService userService, AccessGuard accessGuard) {
        this.userService = userService;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.toResponse(userService.register(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Integer id,
                                                 Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(id, authentication);
        User user = userService.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return ResponseEntity.ok(userService.toResponse(user));
    }
}