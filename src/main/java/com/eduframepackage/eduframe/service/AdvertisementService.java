package com.eduframepackage.eduframe.service;

import com.eduframepackage.eduframe.model.Advertisement;
import com.eduframepackage.eduframe.repository.AdvertisementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    public AdvertisementService(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    // CREATE
    public Advertisement createAdvertisement(Advertisement advertisement) {
        validateAdvertisementDates(advertisement);
        return advertisementRepository.save(advertisement);
    }

    // READ ALL
    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    // READ BY ID
    public Optional<Advertisement> getAdvertisementById(Long id) {
        return advertisementRepository.findById(id);
    }

    // UPDATE
    public Advertisement updateAdvertisement(Long id, Advertisement advertisement) {
        Advertisement existingAdvertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        advertisement.setId(existingAdvertisement.getId());
        validateAdvertisementDates(advertisement);

        return advertisementRepository.save(advertisement);
    }

    // DELETE
    public void deleteAdvertisement(Long id) {
        if (!advertisementRepository.existsById(id)) {
            throw new RuntimeException("Advertisement not found");
        }
        advertisementRepository.deleteById(id);
    }

    private void validateAdvertisementDates(Advertisement advertisement) {
        if (advertisement.getStartDate() != null &&
                advertisement.getEndDate() != null &&
                advertisement.getEndDate().isBefore(advertisement.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }
}
