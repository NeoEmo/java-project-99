package hexlet.code.app.api.controller;

import hexlet.code.app.dto.user.UserAuthDTO;
import hexlet.code.app.repository.UserRepository;
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
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(JWTUtils jwtUtils,
                          AuthenticationManager authenticationManager,
                          UserRepository userRepository) {

        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public String login(@RequestBody UserAuthDTO data) {

        var auth = new UsernamePasswordAuthenticationToken(
                data.getEmail(), data.getPassword());

        authenticationManager.authenticate(auth);
        return jwtUtils.generateToken(data.getEmail());
    }
}
