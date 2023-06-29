package com.banalytics.box.web.pub;

import com.banalytics.box.service.security.SingleUserDetailsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public/authentication")
public class AuthenticationRest {

    private final SingleUserDetailsService userDetailsService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(@RequestBody LoginReq req) {
        Authentication auth = userDetailsService.authenticate(
                new UsernamePasswordAuthenticationToken(
                        SingleUserDetailsService.DEFAULT_USER_NAME,
                        req.getPin()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @PostMapping("/set-pin")
    @ResponseStatus(HttpStatus.OK)
    public void updatePin(@RequestBody UpdatePinReq req) {
        userDetailsService.changePassword(req.getOldPin(), req.getNewPin());
    }

    @GetMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpSession httpSession, HttpServletResponse res) throws IOException {
        httpSession.invalidate();
        SecurityContextHolder.getContext().setAuthentication(null);
        res.sendRedirect("/index.html");
    }

    @Getter
    @Setter
    public static class LoginReq {
        String pin;
    }

    @Getter
    @Setter
    public static class UpdatePinReq {
        String oldPin;
        String newPin;
    }
}
