package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.TransactionLogs;


public interface TransactionLogsRepositoryPort {

    void save(TransactionLogs log);
}