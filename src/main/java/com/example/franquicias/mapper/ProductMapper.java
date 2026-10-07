package com.example.franquicias.mapper;

import com.example.franquicias.dto.ProductResponse;
import com.example.franquicias.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "branchId", source = "branch.id")
    ProductResponse toResponse(Product product);
}
