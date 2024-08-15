package org.placeholder.homerback.services;

import org.placeholder.homerback.dtos.CountryDTO;
import org.placeholder.homerback.repositories.ICountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryService {

    @Autowired
    private ICountryRepository countryRepo;

    public List<CountryDTO> getWithCities() {
        return countryRepo.findAll().stream().map(CountryDTO::new).toList();
    }
}