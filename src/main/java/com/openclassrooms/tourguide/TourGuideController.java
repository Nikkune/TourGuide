package com.openclassrooms.tourguide;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.dto.AttractionDTO;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

	@Autowired
	GpsUtil gpsUtil;

	@Autowired
	RewardsService rewardsService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation") 
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }

    @RequestMapping("/getNearbyAttractions") 
    public List<AttractionDTO> getNearbyAttractions(@RequestParam String userName) {
        User user = getUser(userName);
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        Location userLocation = visitedLocation.location;

        // Get all attractions
        List<Attraction> attractions = gpsUtil.getAttractions();

        // Calculate distance for each attraction and sort by distance
        List<Attraction> sortedAttractions = attractions.stream()
            .sorted(Comparator.comparingDouble(attraction -> 
                rewardsService.getDistance(attraction, userLocation)))
            .toList();

        // Take the 5 closest attractions
        List<Attraction> closestAttractions = sortedAttractions.stream()
            .limit(5)
            .toList();

        // Create DTOs with all required information
        List<AttractionDTO> attractionDTOs = new ArrayList<>();
        for (Attraction attraction : closestAttractions) {
            double distance = rewardsService.getDistance(attraction, userLocation);
            int rewardPoints = rewardsService.calculateRewardPoints(attraction, user);

            AttractionDTO dto = new AttractionDTO(
                attraction.attractionName,
                attraction.latitude,
                attraction.longitude,
                userLocation.latitude,
                userLocation.longitude,
                distance,
                rewardPoints
            );

            attractionDTOs.add(dto);
        }

        return attractionDTOs;
    }

    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }

    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }

    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }


}
