package me.danieluss.tournaments.controller;

import me.danieluss.tournaments.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping(value = "/images/{id}/{imageNo}", produces = "image/jpeg")
    public byte[] image(@PathVariable Long id, @PathVariable Integer imageNo) {
        return resourceService.image(id, imageNo);
    }
}
