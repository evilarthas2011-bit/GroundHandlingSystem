package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.HibernateUtil;
import com.kuleshov.groundhandling.domain.*;
import com.kuleshov.groundhandling.repository.ResourceRepository;


public class ResourceManagementServiceImpl implements ResourceManagementService {

    private final ResourceRepository resourceRepository;

    public ResourceManagementServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public OperationResult addEquipment(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return OperationResult.error("Введите модель техники");
        }
        return HibernateUtil.inTransaction(session -> {
            Equipment equipment = new Equipment();
            equipment.setName(modelName);
            equipment.setModelName(modelName);
            equipment.setCapacity(0.0);
            resourceRepository.save(session, equipment);
            return OperationResult.success("Добавлена техника: " + modelName);
        });
    }

    @Override
    public OperationResult addBrigade(String specialization, int size) {
        if (specialization == null || specialization.isBlank()) {
            return OperationResult.error("Введите специализацию");
        }
        if (size < 1) {
            return OperationResult.error("Размер бригады должен быть не менее одного человека");
        }
        return HibernateUtil.inTransaction(session -> {
            Brigade brigade = new Brigade();
            brigade.setName(specialization);
            brigade.setSpecialization(specialization);
            brigade.setSize(size);
            resourceRepository.save(session, brigade);
            return OperationResult.success("Добавлена бригада: " + specialization + " (" + size + " чел.)");
        });
    }

    @Override
    public OperationResult freeResource(Long resourceId) {
        return HibernateUtil.inTransaction(session -> {
            Resource resource = resourceRepository.findById(session, resourceId);
            if (resource == null) {
                return OperationResult.error("Ресурс не найден");
            }
            if (resource.isFree()) {
                return OperationResult.error("Ресурс уже свободен");
            }
            resource.setAssignedFlight(null);
            resourceRepository.save(session, resource);
            return OperationResult.success("Освобождён ресурс: " + resource.getName());
        });
    }

    @Override
    public OperationResult deleteResource(Long resourceId) {
        return HibernateUtil.inTransaction(session -> {
            Resource resource = resourceRepository.findById(session, resourceId);
            if (resource == null) {
                return OperationResult.error("Ресурс не найден");
            }
            if (!resource.isFree()) {
                return OperationResult.error("Нельзя удалить занятый ресурс. Сначала освободите его.");
            }
            resourceRepository.delete(session, resource);
            return OperationResult.success("Удалён ресурс: " + resource.getName());
        });
    }
}
