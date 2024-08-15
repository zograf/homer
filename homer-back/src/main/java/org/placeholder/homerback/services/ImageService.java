package org.placeholder.homerback.services;

import jakarta.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.parser.MediaType;
import org.placeholder.homerback.dtos.CountryDTO;
import org.placeholder.homerback.entities.EImageType;
import org.placeholder.homerback.repositories.ICountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${imagePath}")
    private String imagePath;

    @Autowired
    ServletContext context;

    public ByteArrayResource getImage(Integer id, EImageType type) {
        //logger.info("Looking for image id " + id);
        String name = getName(id, type);
        File image = null;
        try {
            File imageFile = new File(imagePath);
            for (File f : imageFile.listFiles()) {
                if (f.getName().contains(name)) {
                    image = f;
                    break;
                }
            }

            if (image == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            return new ByteArrayResource(Files.readAllBytes(Paths.get(image.getPath())));
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }
    public String getName(Integer id, EImageType type) {
        return switch (type) {
            case USER_IMAGE -> "user-" + id;
            case PROPERTY_IMAGE -> "property-" + id;
            case DEVICE_IMAGE -> "device-" + id;
        };
    }
}