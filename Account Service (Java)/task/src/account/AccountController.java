package account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

@RestController
public class AccountController {
    public record UserDTO(String name, String lastname, String email) {}

    @PostMapping("/api/auth/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody User user) {
        String[] jsonFields = {user.getName(), user.getLastname(), user.getEmail(), user.getPassword()};
        if (Arrays.stream(jsonFields).anyMatch(field -> field == null || field.isEmpty())) {
            return ResponseEntity.badRequest().build();
        } else if (!user.getEmail().endsWith("@acme.com")) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok().body(new UserDTO(user.getName(), user.getLastname(), user.getEmail()));
        }
    }
}
