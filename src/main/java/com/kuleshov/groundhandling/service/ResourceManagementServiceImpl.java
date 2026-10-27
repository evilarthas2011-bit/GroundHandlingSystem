package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.Resource;
import com.kuleshov.groundhandling.repository.ResourceRepository;

public class ResourceManagementServiceImpl implements ResourceManagementService {

    private final ResourceRepository resourceRepository;

    public ResourceManagementServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public OperationResult freeResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) return OperationResult.error("Ресурс не найден");

        if (resource.getAssignedFlight() == null) {
            return OperationResult.error("Ресурс уже свободен");
        }

        try {
            resource.setAssignedFlight(null);
            resourceRepository.update(resource);
            return OperationResult.success("Ресурс освобожден");
        } catch (Exception e) {
            return OperationResult.error("Ошибка освобождения ресурса: " + e.getMessage());
        }
    }

    @Override
    public OperationResult deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) return OperationResult.error("Ресурс не найден");

        if (resource.getAssignedFlight() != null) {
            return OperationResult.error("Нельзя удалить занятый ресурс. Сначала освободите его.");
        }

        try {
            resourceRepository.delete(resource);
            return OperationResult.success("Ресурс удален");
        } catch (Exception e) {
            return OperationResult.error("Ошибка удаления ресурса: " + e.getMessage());
        }
    }
}