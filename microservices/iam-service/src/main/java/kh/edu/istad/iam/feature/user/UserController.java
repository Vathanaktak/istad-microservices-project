//package kh.edu.istad.iam.feature.user;
//
//import kh.edu.istad.iam.dto.UserRequest;
//import kh.edu.istad.iam.dto.UserResponse;
//import kh.edu.istad.iam.mapper.UserMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("api/users")
//@RequiredArgsConstructor
//public class UserController {
//
//    private final UserService userService;
//
//    @PostMapping
//    public UserResponse createUser(
//            @RequestBody UserRequest userRequest
//            ){
//
//        return userService.createUser(userRequest)
//                .orElseThrow(() -> new RuntimeException("Failed to create user"));
//
//    }
//
//}
