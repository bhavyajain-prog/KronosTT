package com.kronostt.web.dto;

import lombok.Data;

@Data
public class RoomDto {
    private Long id;
    private String name;
    private int capacity;
    private Long fixedBatchId;
}
