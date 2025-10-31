package com.khoavdse170395.assignmentservice.controller;

import com.khoavdse170395.assignmentservice.config.DemoFlags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
@Tag(name = "Demo", description = "Endpoints để mô phỏng lỗi demo")
public class DemoController {

    private final DemoFlags demoFlags;

    @Operation(summary = "Bật fail reserve")
    @PostMapping("/fail-reserve/on")
    public ResponseEntity<Void> enableFailReserve() {
        demoFlags.setFailReserve(true);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Tắt fail reserve")
    @PostMapping("/fail-reserve/off")
    public ResponseEntity<Void> disableFailReserve() {
        demoFlags.setFailReserve(false);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Xem trạng thái fail reserve")
    @GetMapping("/fail-reserve")
    public ResponseEntity<Boolean> getFailReserve() {
        return ResponseEntity.ok(demoFlags.isFailReserve());
    }
}





