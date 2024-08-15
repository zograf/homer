package org.placeholder.homerback.services;

import jakarta.servlet.ServletContext;
import org.placeholder.homerback.dtos.*;
import org.placeholder.homerback.entities.EImageType;
import org.placeholder.homerback.entities.ERole;
import org.placeholder.homerback.entities.User;
import org.placeholder.homerback.repositories.*;
import org.placeholder.homerback.security.JwtUtils;
import org.placeholder.homerback.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITokenRepository tokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ImageService imageService;

    @Autowired
    ServletContext context;

    @Value("${imagePath}")
    private String imagePath;

    public TokenDTO login(LoginDTO loginDto) {
        logger.info("Attempting login...");

        Optional<User> userOpt = userRepository.findByEmail(loginDto.getEmail());

        if (userOpt.isEmpty()) {
            logger.error("Can't find user with that email!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message: Incorrect credentials!");
        }

        if (!userOpt.get().getEnabled()) {
            logger.error("User is not enabled!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message: Incorrect credentials!");
        }

        logger.info("Checking password...");

        String encodedPassword = encodePassword(loginDto.getPassword());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), encodedPassword));

        logger.info("Password is correct!");

        String jwt;
        User user = (User) authentication.getPrincipal();
        logger.info("Generating JWT token...");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        jwt = jwtUtils.generateJwtToken(authentication);

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken(jwt);
        tokenDTO.setRefreshToken(""); // Not used at the moment
        tokenDTO.setUserId(user.getId());
        tokenDTO.setUserRole(user.getRole());
        tokenDTO.setEmail(user.getEmail());

        return tokenDTO;
    }

    public User register(RegisterDTO dto, MultipartFile image, Boolean isUser) {
        logger.info("Registering new user...");

        Optional<User> userCheck = userRepository.findByEmail(dto.getEmail());
        if (userCheck.isPresent()) {
            logger.error("User with that email already exists!");
            String value = "message: Account with that email already exists!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, value);
        }

        if (dto.getEmail() == null || dto.getName() == null) {
            logger.error("Null values found in register DTO!");
            String value = "message: Bad input data!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, value);
        }

        logger.info("Saving user to database...");
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(encodePassword(dto.getPassword()));
        user.setRole(isUser ? ERole.ROLE_USER : ERole.ROLE_ADMIN);
        user.setEnabled(!isUser);
        user = userRepository.saveAndFlush(user);

        logger.info("Handling image...");
        saveImage(image, user.getId());

        logger.info("Registered new user!");
        return user;
    }

    public void saveImage(MultipartFile image, Integer id) {
        String[] imageSlice = image.getOriginalFilename().split("\\.");
        String extension = imageSlice[imageSlice.length - 1];
        String name = imageService.getName(id, EImageType.USER_IMAGE);

        File file = new File(imagePath, name + "." + extension);

        try {
            image.transferTo(file);
        } catch (Exception ex){
            logger.error(ex.toString());
        }
        logger.info("Registered new user!");
    }

    public void createVerificationToken(User user, String token) {
        logger.info("Creating verification token for user {}...", user.getId());
        VerificationToken myToken = new VerificationToken(user, token);
        tokenRepository.save(myToken);
        logger.info("Verification token created!");
    }

    public void verify(String token) {
        logger.info("Verifying user...");

        Optional<VerificationToken> tokenCheck = tokenRepository.findByToken(token);
        if (tokenCheck.isEmpty()) {
            logger.error("Invalid verification token!");
            String value = "message: Verification token not found!";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, value);
        }
        VerificationToken verificationToken = tokenCheck.get();
        LocalDate now = LocalDate.now();
        if (verificationToken.getExpiryDate().isBefore(now)) {
            logger.error("Verification token expired!");
            String value = "message: Token expired!";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, value);
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        logger.info("Verified user {}!", user.getId());
        save(user);
    }

    public void sendAdminPassword() {
        Optional<User> checkAdmin = userRepository.findByEmail("admin");
        if (checkAdmin.isPresent()) return;

        logger.info("Creating superadmin");
        String password = generateRandomPassword();
        logger.info("Generated password: " + password);

        User admin = new User();
        admin.setEmail("admin");
        admin.setName("admin");
        admin.setRole(ERole.ROLE_SUPER_ADMIN);
        admin.setPassword(encodePassword(password));
        admin.setEnabled(true);
        save(admin);

        eventPublisher.publishEvent(new OnAdminCreatedEvent(password));
    }

    public String generateRandomPassword() {
        String upperCaseLetters = RandomStringUtils.random(6, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(6, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(6);
        String specialChar = RandomStringUtils.random(6, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(6);

        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers)
                .concat(specialChar)
                .concat(totalChars);

        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);

        String password = pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        return password;
    }


    public User save(User user) {
        return userRepository.save(user);
    }

    private TokenDTO generateToken(User user) {
        String jwt = jwtUtils.generateJwtToken(user);

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken(jwt);
        tokenDTO.setRefreshToken(""); // Not used at the moment
        tokenDTO.setUserId(user.getId());
        tokenDTO.setUserRole(user.getRole());
        tokenDTO.setEmail(user.getEmail());

        return tokenDTO;
    }

    public String encodePassword(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10, new SecureRandom(){
            @Override
            public void nextBytes(byte[] bytes){}
        });
        String encodedPassword = passwordEncoder.encode(password);
        final int passwordLength = 31;
        return encodedPassword.substring(encodedPassword.length()-passwordLength);
    }
}
