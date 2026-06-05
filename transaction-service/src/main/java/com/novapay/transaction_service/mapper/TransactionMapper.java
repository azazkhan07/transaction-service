package com.novapay.transaction_service.mapper;


import com.novapay.transaction_service.dto.response.TransactionResponse;
import com.novapay.transaction_service.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponse toTransactionResponse(Transaction transaction);
}
