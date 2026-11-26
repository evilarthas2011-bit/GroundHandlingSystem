package com.kuleshov.groundhandling.service;

public interface ResourceManagementService {
    OperationResult addEquipment(String modelName);
    OperationResult addBrigade(String specialization, int size);
    OperationResult freeResource(Long resourceId);
    OperationResult deleteResource(Long resourceId);
}
