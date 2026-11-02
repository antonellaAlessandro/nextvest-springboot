package nextvest.controller;

import nextvest.dto.AdminUserDetailResponse;
import nextvest.dto.AdminUserResponse;
import nextvest.dto.UpdateAccountStatusRequest;
import nextvest.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<AdminUserResponse> listUsers() {
        return adminService.listUsers();
    }

    @GetMapping("/users/{id}")
    public AdminUserDetailResponse getUser(@PathVariable Integer id) {
        return adminService.getUserDetail(id);
    }

    @PutMapping("/users/{id}/status")
    public AdminUserResponse updateStatus(@PathVariable Integer id,
                                      @RequestBody UpdateAccountStatusRequest request) {
    return adminService.updateAccountStatus(id, request.accountStatus());
}
}