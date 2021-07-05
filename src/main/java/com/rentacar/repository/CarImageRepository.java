package com.rentacar.repository;

import com.rentacar.domain.CarImage;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the CarImage entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CarImageRepository extends JpaRepository<CarImage, Long>, JpaSpecificationExecutor<CarImage> {}
