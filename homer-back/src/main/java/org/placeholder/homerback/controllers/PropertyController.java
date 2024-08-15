package org.placeholder.homerback.controllers;

import org.placeholder.homerback.dtos.GraphDatetimePointDTO;
import org.placeholder.homerback.dtos.DenyPropertyDTO;
import org.placeholder.homerback.dtos.NewPropertyDTO;
import org.placeholder.homerback.dtos.PropertyDTO;
import org.placeholder.homerback.entities.ERole;
import org.placeholder.homerback.entities.User;
import org.placeholder.homerback.services.InfluxDBService;
import org.placeholder.homerback.services.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "api/property")
public class PropertyController
{

    @Autowired private PropertyService propertyService;
    @Autowired private InfluxDBService influxDBService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyDTO> request(
            @AuthenticationPrincipal User user,
            // ???????
            @RequestParam(value = "name") String name,
            @RequestParam(value = "floors") Integer floors,
            @RequestParam(value = "area") Integer area,
            @RequestParam(value = "displayAddress") String street,
            @RequestParam(value = "countryId") Integer countryId,
            @RequestParam(value = "cityId") Integer cityId,
            @RequestParam(value = "lat") Double lat,
            @RequestParam(value = "lon") Double lon,
            // ???????
            @RequestParam(required=true, value="image") MultipartFile image
    ) {
        NewPropertyDTO dto = new NewPropertyDTO(name, user.getId(), floors, area, countryId, cityId, street, lon, lat);
        return new ResponseEntity<>(new PropertyDTO(propertyService.request(dto, image)), HttpStatus.OK);
    }

    @GetMapping(value = "/requests")
    public ResponseEntity<List<PropertyDTO>> getRequests() {
        return new ResponseEntity<>(propertyService.getRequests(), HttpStatus.OK);
    }

    @GetMapping(value = "/request/approve/{id}")
    public ResponseEntity<Void> approve(@PathVariable Integer id) {
        propertyService.approve(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/request/deny")
    public ResponseEntity<Void> deny(@RequestBody DenyPropertyDTO denyDTO) {
        propertyService.deny(denyDTO.getId(), denyDTO.getReason());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "")
    public ResponseEntity<List<PropertyDTO>> getAllForUser(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(propertyService.getVisibleByUser(user.getId()), HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<PropertyDTO>> getAllProperties(@AuthenticationPrincipal User user) {
        if (user.getRole() != ERole.ROLE_ADMIN) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        return ResponseEntity.ok(propertyService.getAll());
    }

    @GetMapping(value = "/accepted")
    public ResponseEntity<List<PropertyDTO>> getAllAcceptedForUser(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(propertyService.getAcceptedVisibleByUser(user.getId()), HttpStatus.OK);
    }

    @PostMapping(value = "/{propertyId}/consumption")
    public ResponseEntity<List<GraphDatetimePointDTO>> getConsumption(@PathVariable Integer propertyId,
                                                                      @RequestParam(value = "live") Boolean live,
                                                                      @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                      @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                      @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getConsumption(propertyId, null, null, live, lastXTimeUnits, start, end, false));
    }

    @PostMapping(value = "/{propertyId}/delta")
    public ResponseEntity<List<GraphDatetimePointDTO>> getDelta(@PathVariable Integer propertyId,
                                                                      @RequestParam(value = "live") Boolean live,
                                                                      @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                      @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                      @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getConsumption(propertyId, null, null, live, lastXTimeUnits, start, end, true));
    }

    @PostMapping(value = "/consumption")
    public ResponseEntity<List<GraphDatetimePointDTO>> getConsumptionForCity(@RequestParam(value = "cityId") Integer cityId,
                                                                             @RequestParam(value = "live") Boolean live,
                                                                             @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                             @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                             @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getConsumption(null, cityId, null, live, lastXTimeUnits, start, end, false));
    }

    @PostMapping(value = "/delta")
    public ResponseEntity<List<GraphDatetimePointDTO>> getDeltaForCity(@RequestParam(value = "cityId") Integer cityId,
                                                                             @RequestParam(value = "live") Boolean live,
                                                                             @RequestParam(value = "lastXTimeUnits", required = false) String lastXTimeUnits,
                                                                             @RequestParam(value = "start", required = false) LocalDateTime start,
                                                                             @RequestParam(value = "end", required = false) LocalDateTime end) {
        return ResponseEntity.ok(influxDBService.getConsumption(null, cityId, null, live, lastXTimeUnits, start, end, true));
    }
}
