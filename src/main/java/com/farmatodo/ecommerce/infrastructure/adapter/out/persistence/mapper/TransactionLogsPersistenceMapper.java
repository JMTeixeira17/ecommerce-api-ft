package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.TransactionLogs;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.TransactionLogsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionLogsPersistenceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TransactionLogsEntity toEntity(TransactionLogs domain);
}