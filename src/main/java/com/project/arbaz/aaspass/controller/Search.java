package com.project.arbaz.aaspass.controller;

import com.project.arbaz.aaspass.dto.EventSearchResponse;
import com.project.arbaz.aaspass.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class Search {
    // Searching based on the text
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<EventSearchResponse>> search(
            @RequestParam("query") String query) {

        List<EventSearchResponse> events = searchService.search(query);

        return ResponseEntity.ok(events);
    }
}
