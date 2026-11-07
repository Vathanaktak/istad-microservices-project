package kh.edu.istad.platform.iam.rest.controller;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.usecase.*; import kh.edu.istad.platform.iam.application.result.PageResult;
import kh.edu.istad.platform.iam.rest.dto.*; import kh.edu.istad.platform.iam.rest.dto.Common.PageResponse; import kh.edu.istad.platform.iam.rest.dto.UserDtos.*; import kh.edu.istad.platform.iam.rest.dto.RoleDtos.*; import kh.edu.istad.platform.iam.rest.dto.PermissionDtos.*;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final RegisterUserUseCase register;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest r) {
        var result = register.execute(new RegisterUserCommand(r.username(), r.email(), r.password(), r.givenName(), r.familyName(), r.locale()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.user(result));
    }
}
