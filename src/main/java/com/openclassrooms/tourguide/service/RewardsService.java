package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public RewardCentral getRewardsCentral() {
        return rewardsCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Calculates the rewards for a given user by identifying attractions near the user's visited
     * locations and awarding reward points if the user has not been rewarded for visiting the attraction.
     *
     * The method checks each visited location of the user against a list of attractions. If an attraction
     * is close to a visited location, and the user has not yet been rewarded for that attraction,
     * reward points are calculated and added to the user's rewards.
     *
     * Thread-safety is ensured for the user's visited locations list by creating a thread-safe copy of
     * the list before iterating. Access to the user's rewards is synchronized to avoid concurrent modifications.
     *
     * @param user the user for whom rewards need to be calculated
     */
    public void calculateRewards(User user) {
        /*
         * Reason: When calculateRewards loop is running, the user's visitedLocations another thread can modify this one.
         * This behavior is named: not thread-safe
         * Solution: Create a copy of the visitedLocations list using CopyOnWriteArrayList who is thread-safe.
         */
        List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions = gpsUtil.getAttractions();

        for (VisitedLocation visitedLocation : userLocations) {
            for (Attraction attraction : attractions) {
                boolean alreadyRewarded = user.getUserRewards().stream()
                        .anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));

                if (!alreadyRewarded && nearAttraction(visitedLocation, attraction)) {
                    int points = getRewardPoints(attraction, user);
                    synchronized (user) {
                        user.addUserReward(new UserReward(visitedLocation, attraction, points));
                    }
                }
            }
        }
    }

    /**
     * Calculates rewards for all users concurrently. This method processes the rewards
     * for a list of given users by executing the reward calculation logic asynchronously
     * for each user and waiting for all computations to complete.
     *
     * @param users the list of users for whom rewards need to be calculated
     */
    public void calculateRewardsForAllUsers(List<User> users) {
        List<CompletableFuture<Void>> futures = users.stream()
                .map(user -> CompletableFuture.runAsync(() -> calculateRewards(user), executorService))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    public int calculateRewardPoints(Attraction attraction, User user) {
        return getRewardPoints(attraction, user);
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
