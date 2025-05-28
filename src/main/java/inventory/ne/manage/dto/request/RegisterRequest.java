package inventory.ne.manage.dto.request;

import java.util.Set;

import inventory.ne.manage.enums.ERole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String fullname;
    private String email;
    private String password;
    private Set<ERole> roles;
}
