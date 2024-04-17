package com.senior.assessment.rest;

import com.senior.assessment.config.mapper.ModelMapperService;
import com.senior.assessment.domain.dto.item.ItemCreateDto;
import com.senior.assessment.domain.dto.item.ItemDetailDto;
import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("items")
public class ItemController {
    private final ItemService itemService;
    private final ModelMapperService modelMapperService;

    @PostMapping
    public ResponseEntity<ItemDetailDto> create(@Valid @RequestBody ItemCreateDto itemCreate) {
        var item = itemService.saveItem(modelMapperService.toObject(Item.class, itemCreate));
        return ResponseEntity.ok(modelMapperService.toObject(ItemDetailDto.class, item));
    }

//    @PutMapping("/{parkingId}")
//    public ResponseEntity<ParkingDetailDto> update(
//            @PathVariable(name = "parkingId") long parkingId,
//            @Valid @RequestBody ParkingCreateUpdateDto parkingUpdateDto) {
//        var parking = modelMapperService.toObject(Parking.class, parkingUpdateDto)
//                .toBuilder()
//                .id(parkingId)
//                .build();
//        parking = parkingService.updateParking(parking);
//        return ResponseEntity.ok(modelMapperService.toObject(ParkingDetailDto.class, parking));
//    }
//
//    @GetMapping("/{parkingId}")
//    public ResponseEntity<ParkingDetailDto> getById(@PathVariable(name = "parkingId") long parkingId) {
//        return ResponseEntity
//                .ok(modelMapperService.toObject(ParkingDetailDto.class, parkingService.getParkingById(parkingId)));
//    }

}
