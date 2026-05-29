package com.schoolmoney.controller;

import com.schoolmoney.dto.ChildRequest;
import com.schoolmoney.model.Child;
import com.schoolmoney.service.ChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @PostMapping
    public ResponseEntity<Child> addChild(@RequestBody ChildRequest request, @RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(childService.addChild(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<Child>> getMyChildren(@RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(childService.getMyChildren(userId));
    }

    @PostMapping("/{childId}/join/{inviteToken}")
    public ResponseEntity<Child> assignToClass(@PathVariable String childId, @PathVariable String inviteToken, @RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(childService.assignToClass(childId, inviteToken, userId));
    }
}