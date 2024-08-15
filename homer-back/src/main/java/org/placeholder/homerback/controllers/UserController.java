package org.placeholder.homerback.controllers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.placeholder.homerback.dtos.*;
import org.placeholder.homerback.entities.*;
import org.placeholder.homerback.repositories.IUserRepository;
import org.placeholder.homerback.services.PermissionService;
import org.placeholder.homerback.services.UserService;
import org.placeholder.homerback.utils.OnRegistrationCompleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "api/user")
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PermissionService permissionService;

    @PostMapping(value="register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> registerUser(@RequestParam(required=true, value="image") MultipartFile image,
                                            @RequestParam(required=true, value="name") String name,
                                            @RequestParam(required=true, value="email") String email,
                                            @RequestParam(required=true, value="password") String password) {
        logger.info("Registering new user with email {}...", email);

        RegisterDTO dto = new RegisterDTO();
        dto.setName(name);
        dto.setEmail(email);
        dto.setPassword(password);

        User user = userService.register(dto, image, true);
        logger.info("Successfully registered user {}", user.getEmail());

        logger.info("Sending verification email...");
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));

        logger.info("Returning status 200!");
        return new ResponseEntity<>(new UserDTO(user), HttpStatus.OK);
    }

    @GetMapping(value="/activate/{activationId}")
    public ResponseEntity<UserDTO> activate(@Size(min=36, max=36) @PathVariable String activationId) {
        userService.verify(activationId);

        logger.info("Returning status 200!");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "login", consumes = "application/json")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO loginDto, HttpServletRequest request) {
        logger.info("Trying login with email {}...", loginDto.getEmail());
        return ResponseEntity.ok(userService.login(loginDto));
    }

    @GetMapping(value = "{id}/getPermissions")
    public ResponseEntity<UserPermissionsDTO> getUserPermissions(@PathVariable Integer id) {
        return ResponseEntity.ok(permissionService.getUserPermissions(id));
    }

    @GetMapping(value = "{id}/getOwnerPermissions")
    public ResponseEntity<List<OwnerPermissionsDTO>> getOwnerPermissions(@PathVariable Integer id) {
        return ResponseEntity.ok(permissionService.getOwnerPermissions(id));
    }

    @PostMapping(value = "{id}/devicePermission")
    public ResponseEntity<Object> addDevicePermission(@RequestBody PermissionDTO permissionDTO, @PathVariable Integer id){
        permissionService.addDevicePermission(permissionDTO.getOwnerEmail(), permissionDTO.getUserEmail(), permissionDTO.getDeviceId());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "{id}/propertyPermission")
    public ResponseEntity<Object> addPropertyPermission(@RequestBody PermissionDTO permissionDTO, @PathVariable Integer id){
        permissionService.addPropertyPermission(permissionDTO.getOwnerEmail(), permissionDTO.getUserEmail(), permissionDTO.getPropertyId());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "{id}/removePropertyPermission")
    public ResponseEntity<Object> removePropertyPermission(@RequestBody PermissionDTO permissionDTO, @PathVariable Integer id){
        permissionService.removePropertyPermission(permissionDTO.getOwnerEmail(), permissionDTO.getUserEmail(), permissionDTO.getPropertyId());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "{id}/removeDevicePermission")
    public ResponseEntity<Object> removeDevicePermission(@RequestBody PermissionDTO permissionDTO, @PathVariable Integer id){
        permissionService.removeDevicePermission(permissionDTO.getOwnerEmail(), permissionDTO.getUserEmail(), permissionDTO.getDeviceId());
        return ResponseEntity.ok().build();
    }
}
