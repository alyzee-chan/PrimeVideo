package com.primevideo.repository;

import com.primevideo.entity.Profile;
import com.primevideo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findAllByUser(User user);
    long countByUser(User user);
}
