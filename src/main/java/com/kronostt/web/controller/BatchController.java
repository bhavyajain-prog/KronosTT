package com.kronostt.web.controller;

import com.kronostt.service.BatchService;
import com.kronostt.web.dto.BatchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @GetMapping
    public List<BatchDto> getAllBatches() {
        return batchService.getAllBatches();
    }

    @GetMapping("/{id}")
    public BatchDto getBatchById(@PathVariable Long id) {
        return batchService.getBatchById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchDto createBatch(@RequestBody BatchDto dto) {
        return batchService.createBatch(dto);
    }

    @PutMapping("/{id}")
    public BatchDto updateBatch(@PathVariable Long id, @RequestBody BatchDto dto) {
        return batchService.updateBatch(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatch(@PathVariable Long id) {
        batchService.deleteBatch(id);
    }
}