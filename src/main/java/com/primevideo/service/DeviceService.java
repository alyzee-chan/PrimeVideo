package com.primevideo.service;

import com.primevideo.entity.Device;
import com.primevideo.entity.User;
import com.primevideo.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public List<Device> getUserDevices(User user) {
        return deviceRepository.findAllByUser(user);
    }

    @Transactional
    public void unregisterDevice(Long userId, String deviceId) {
        deviceRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    }
}
