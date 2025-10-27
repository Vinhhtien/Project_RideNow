package service;

import model.Motorbike;
import model.BikeType;
import model.Partner;
import java.util.List;

public interface IMotorbikeAdminService {
    // Motorbike operations for admin
    List<Motorbike> getAllMotorbikes();
    Motorbike getMotorbikeById(int bikeId);
    boolean addMotorbike(Motorbike motorbike);
    boolean updateMotorbike(Motorbike motorbike);
    boolean deleteMotorbike(int bikeId);
    
    // Helper data operations for admin
    List<BikeType> getAllBikeTypes();
    List<Partner> getAllPartners();
    
    // Filter operations for admin
    List<Motorbike> getMotorbikesByOwner(String ownerType);
    List<Motorbike> getMotorbikesByStatus(String status);
}