package org.placeholder.homerback.controllers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.placeholder.homerback.dtos.*;
import org.placeholder.homerback.entities.*;
import org.placeholder.homerback.repositories.IUserRepository;
import org.placeholder.homerback.services.UserService;
import org.placeholder.homerback.utils.OnRegistrationCompleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "api/admin")
public class AdminController {
    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @PostMapping(value="register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> registerAdmin(@RequestParam(required=true, value="image") MultipartFile image,
                                                @RequestParam(required=true, value="name") String name,
                                                @RequestParam(required=true, value="email") String email,
                                                @RequestParam(required=true, value="password") String password) {
        logger.info("Registering new user with email {}...", email);

        RegisterDTO dto = new RegisterDTO();
        dto.setName(name);
        dto.setEmail(email);
        dto.setPassword(password);

        User user = userService.register(dto, image, false);
        logger.info("Successfully registered Admin {}", user.getEmail());

        logger.info("Returning status 200!");
        return new ResponseEntity<>(new UserDTO(user), HttpStatus.OK);
    }
}
