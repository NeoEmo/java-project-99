package hexlet.code.app.api.controller;

import hexlet.code.app.dto.UserAuthDTO;
import hexlet.code.app.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager manager;

    @PostMapping("/login")
    public String login(@RequestBody UserAuthDTO data) {
        var auth = new UsernamePasswordAuthenticationToken(
                data.getEmail(), data.getPassword());

        manager.authenticate(auth);
        return jwtUtils.generateToken(data.getEmail());
    }
}
