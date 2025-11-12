package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.EmailNotification;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.EmailNotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailNotificationPersistenceMapper {

    EmailNotification toDomain(EmailNotificationEntity entity);

    EmailNotificationEntity toEntity(EmailNotification domain);
}