package inventory.ne.manage.service;

import inventory.ne.manage.dto.request.RegisterRequest;
import inventory.ne.manage.model.User;

public interface UserService {
    User registerUser(RegisterRequest registerRequest);

    User findByEmail(String email);

    void verifyEmail(String email, String code);

    void sendForgotPasswordCode(String email);

    void resetPassword(String email, String code, String newPassword);
}
