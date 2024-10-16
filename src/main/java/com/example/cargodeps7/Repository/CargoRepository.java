package com.example.cargodeps7.Repository;

import com.example.cargodeps7.Models.Cargo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {
    @Query("SELECT c FROM Cargo c " +
            "WHERE (:cargoName IS NULL OR LOWER(c.cargoName) LIKE LOWER(CONCAT('%', :cargoName, '%'))) " +
            "AND (:departureCity IS NULL OR LOWER(c.departureCity) LIKE LOWER(CONCAT('%', :departureCity, '%'))) " +
            "AND (:arrivalCity IS NULL OR LOWER(c.arrivalCity) LIKE LOWER(CONCAT('%', :arrivalCity, '%'))) " +
            "AND (:startDate IS NULL OR c.departureDate >= :startDate) " +
            "AND (:endDate IS NULL OR c.arrivalDate <= :endDate)")
    List<Cargo> findByParams(
            @Param("cargoName") String cargoName,
            @Param("departureCity") String departureCity,
            @Param("arrivalCity") String arrivalCity,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Sort sort);

    @Query("SELECT DATE(c.departureDate), COUNT(c) FROM Cargo c GROUP BY DATE(c.departureDate)")
    List<Object[]> findCargoStats();
}
