package com.primevideo.repository;

import com.primevideo.entity.Device;
import com.primevideo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findAllByUser(User user);
    Optional<Device> findByUserIdAndDeviceId(Long userId, String deviceId);
    void deleteByUserIdAndDeviceId(Long userId, String deviceId);
}
