package ru.ijo42.rbirb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ijo42.rbirb.model.PhotoModel;

@Repository
public interface PhotoRepository extends JpaRepository<PhotoModel, Long> {
}
