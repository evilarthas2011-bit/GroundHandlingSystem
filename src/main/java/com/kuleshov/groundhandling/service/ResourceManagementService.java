package com.kuleshov.groundhandling.service;

import com.kuleshov.groundhandling.domain.Resource;

public interface ResourceManagementService {
    OperationResult freeResource(Long resourceId);
    OperationResult deleteResource(Long resourceId);
}