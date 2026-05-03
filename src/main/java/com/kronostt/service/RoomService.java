package com.kronostt.service;

import com.kronostt.persistence.entity.BatchEntity;
import com.kronostt.persistence.entity.RoomEntity;
import com.kronostt.persistence.repository.BatchRepository;
import com.kronostt.persistence.repository.RoomRepository;
import com.kronostt.service.mapper.RoomMapper;
import com.kronostt.web.dto.RoomDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BatchRepository batchRepository; // Needed to validate the fixedBatchId
    private final RoomMapper roomMapper;

    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDto)
                .toList();
    }

    public RoomDto getRoomById(Long id) {
        RoomEntity entity = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));
        return roomMapper.toDto(entity);
    }

    @Transactional
    public RoomDto createRoom(RoomDto dto) {
        RoomEntity entity = roomMapper.toEntity(dto);
        assignFixedBatchIfPresent(dto.getFixedBatchId(), entity);

        RoomEntity saved = roomRepository.save(entity);
        return roomMapper.toDto(saved);
    }

    @Transactional
    public RoomDto updateRoom(Long id, RoomDto dto) {
        RoomEntity existing = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));

        existing.setName(dto.getName());
        existing.setCapacity(dto.getCapacity());
        assignFixedBatchIfPresent(dto.getFixedBatchId(), existing);

        return roomMapper.toDto(roomRepository.save(existing));
    }

    @Transactional
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    // Helper method to safely attach the Batch entity based on the DTO's flat ID
    private void assignFixedBatchIfPresent(Long batchId, RoomEntity roomEntity) {
        if (batchId != null) {
            BatchEntity batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> new RuntimeException("Fixed Batch not found with ID: " + batchId));
            roomEntity.setFixedBatch(batch);
        } else {
            roomEntity.setFixedBatch(null);
        }
    }
}