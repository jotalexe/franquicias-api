package com.example.franquicias.mapper;

import com.example.franquicias.dto.FranchiseResponse;
import com.example.franquicias.entity.Franchise;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FranchiseMapper {

    FranchiseResponse toResponse(Franchise franchise);
}
