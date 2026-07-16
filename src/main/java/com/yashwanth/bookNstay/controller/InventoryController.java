package com.yashwanth.bookNstay.controller;

import com.yashwanth.bookNstay.dto.InventoryDto;
import com.yashwanth.bookNstay.dto.UpdateInventoryRequestDto;
import com.yashwanth.bookNstay.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDto>> getAllInventory(@PathVariable(name = "roomId") Long roomId){
        return ResponseEntity.ok(inventoryService.getAllInventory(roomId));
    }

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<Void> updateInventory(@PathVariable(name = "roomId") Long roomId,
                                                @RequestBody UpdateInventoryRequestDto updateInventoryRequestDto){
        inventoryService.updateInventory(roomId, updateInventoryRequestDto);
        return ResponseEntity.noContent().build();
    }
}
