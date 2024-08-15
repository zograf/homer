package org.placeholder.homerback.controllers;

import org.placeholder.homerback.dtos.CountryDTO;
import org.placeholder.homerback.services.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "api/country")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @GetMapping
    public ResponseEntity<List<CountryDTO>> getAll() {
        return new ResponseEntity<>(countryService.getWithCities(), HttpStatus.OK);
    }
}
