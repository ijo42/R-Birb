package ru.ijo42.rbirb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ijo42.rbirb.model.StagingModel;

@Repository
public interface StagingRepository extends JpaRepository<StagingModel, Long> {
}
