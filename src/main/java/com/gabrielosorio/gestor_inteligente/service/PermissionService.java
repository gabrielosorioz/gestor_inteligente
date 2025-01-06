package com.gabrielosorio.gestor_inteligente.service;

import com.gabrielosorio.gestor_inteligente.model.enums.PermissionType;
import com.gabrielosorio.gestor_inteligente.model.User;

public interface PermissionService {
    boolean hasPermission(User user, PermissionType permissionType);
}
