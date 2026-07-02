package com.yashwanth.bookNstay.controller;

import com.yashwanth.bookNstay.dto.RoomDto;
import com.yashwanth.bookNstay.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/hotel/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomAdminController {

    private final RoomService roomService;

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable(name = "roomId") Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomInHotel(@PathVariable(name = "hotelId") Long id) {
        return ResponseEntity.ok(roomService.getAllRoomInHotel(id));
    }

    @PostMapping
    public ResponseEntity<RoomDto> createRoomById(@PathVariable(name = "hotelId") Long id, @RequestBody RoomDto roomDto) {
        return new ResponseEntity<>(roomService.createNewRoom(id, roomDto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<RoomDto> updateRoomById(@PathVariable(name = "hotelId") Long id, @RequestBody RoomDto roomDto) {
        return ResponseEntity.ok(roomService.updateRoomById(id, roomDto));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable(name = "roomId") Long id) {
        roomService.deleteRoomById(id);
        return ResponseEntity.noContent().build();
    }


}
