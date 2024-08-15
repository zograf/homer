package org.placeholder.homerback.services;

import org.placeholder.homerback.dtos.NewPropertyDTO;
import org.placeholder.homerback.dtos.PropertyDTO;
import org.placeholder.homerback.entities.*;
import org.placeholder.homerback.repositories.ICityRepository;
import org.placeholder.homerback.repositories.ICountryRepository;
import org.placeholder.homerback.repositories.IPropertyRepository;
import org.placeholder.homerback.repositories.IUserRepository;
import org.placeholder.homerback.security.AuthTokenFilter;
import org.placeholder.homerback.utils.OnPropertyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    @Autowired private IPropertyRepository propertyRepo;
    @Autowired private IUserRepository userRepo;
    @Autowired private ICountryRepository countryRepo;
    @Autowired private ICityRepository cityRepo;
    @Autowired private ApplicationEventPublisher eventPublisher;

    @Autowired private ImageService imageService;

    @Value("${imagePath}") private String imagePath;

    private static final Logger logger = LoggerFactory.getLogger(PropertyService.class);

    public Property request(NewPropertyDTO dto, MultipartFile image) {

        Property property = getProperty(dto);
        property = propertyRepo.saveAndFlush(property);

        String name = imageService.getName(property.getId(), EImageType.PROPERTY_IMAGE);
        String[] imageSlice = image.getOriginalFilename().split("\\.");
        String extension = imageSlice[imageSlice.length - 1];

        File file = new File(imagePath, name + "." + extension);

        logger.info("Image {}, size {}", image.getOriginalFilename(), image.getSize());
        try { image.transferTo(file); }
        catch (Exception ex){
            logger.error("Failed to save image to {}", file.getAbsolutePath());
            logger.error(ex.getMessage());
            propertyRepo.delete(property);
        }

        return property;
    }

    private Property getProperty(NewPropertyDTO dto) {
        Property property = new Property();

        Optional<User> user = userRepo.findById(dto.getUserId());
        if(user.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found!");
        Optional<Country> country = countryRepo.findById(dto.getCountryId());
        if(country.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country not found!");
        Optional<City> city = cityRepo.findById(dto.getCityId());
        if(city.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City not found!");

        property.setName(dto.getName());
        property.setUser(user.get());
        property.setFloors(dto.getFloors());
        property.setArea(dto.getArea());
        property.setCountry(country.get());
        property.setCity(city.get());
        property.setStreet(dto.getStreet());
        property.setLon(dto.getLon());
        property.setLat(dto.getLat());
        property.setStatus(EPropertyStatus.REQUESTED);
        property.setStatusDetails("");
        return property;
    }

    public Property save(Property property) {
        return propertyRepo.save(property);
    }

    public List<PropertyDTO> getRequests() {
        return propertyRepo.findAllByStatus(EPropertyStatus.REQUESTED).stream().map(PropertyDTO::new).toList();
    }
    public List<PropertyDTO> getApproved(Integer userId) {
        return propertyRepo.findAllByStatusAndUserId(EPropertyStatus.ACCEPTED, userId).stream().map(PropertyDTO::new).toList();
    }
    public List<PropertyDTO> getVisibleByUser(Integer userId) {
        return propertyRepo.findByStatusIsInAndUserId(Arrays.asList(EPropertyStatus.ACCEPTED, EPropertyStatus.REQUESTED), userId).stream().map(PropertyDTO::new).toList();
    }

    public void approve(Integer id) {
        Optional<Property> propertyOptional = propertyRepo.findById(id);

        // TODO Handle error

        Property property = propertyOptional.get();
        property.setStatus(EPropertyStatus.ACCEPTED);
        save(property);

        eventPublisher.publishEvent(new OnPropertyResponseEvent(
                property.getUser().getEmail(),
                "Your property has been approved",
                "Congrats!",
                "Your property by name '" + property.getName() + "' has been approved by admins!",
                "You can now start adding devices to it!"
        ));
    }
    public void deny(Integer id, String reason) {
        Optional<Property> propertyOptional = propertyRepo.findById(id);

        // TODO Handle error

        Property property = propertyOptional.get();
        property.setStatus(EPropertyStatus.DENIED);
        property.setStatusDetails(reason);
        save(property);

        eventPublisher.publishEvent(new OnPropertyResponseEvent(
                property.getUser().getEmail(),
                "Your property has been denied",
                "Oh no!",
                "Your property by name '" + property.getName() + "' has been denied by admins",
                "Reason: " + reason
        ));
    }

    public List<PropertyDTO> getAcceptedVisibleByUser(Integer userId) {
        return propertyRepo.findByStatusIsInAndUserId(List.of(EPropertyStatus.ACCEPTED), userId).stream().map(PropertyDTO::new).toList();
    }

    public List<PropertyDTO> getAll() {
        return propertyRepo.findByStatusIsIn(List.of(EPropertyStatus.ACCEPTED)).stream().map(PropertyDTO::new).toList();
    }
}
