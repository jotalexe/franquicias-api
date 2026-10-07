package com.example.franquicias.mapper;

import com.example.franquicias.dto.BranchResponse;
import com.example.franquicias.entity.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    @Mapping(target = "franchiseId", source = "franchise.id")
    BranchResponse toResponse(Branch branch);
}
