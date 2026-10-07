package com.example.franquicias.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException franchise(Long id) {
        return new ResourceNotFoundException("Franchise not found with id: " + id);
    }

    public static ResourceNotFoundException branch(Long id) {
        return new ResourceNotFoundException("Branch not found with id: " + id);
    }

    public static ResourceNotFoundException branchInFranchise(Long branchId, Long franchiseId) {
        return new ResourceNotFoundException(
                "Branch with id: " + branchId + " does not belong to franchise with id: " + franchiseId);
    }

    public static ResourceNotFoundException product(Long id) {
        return new ResourceNotFoundException("Product not found with id: " + id);
    }

    public static ResourceNotFoundException productInBranch(Long productId, Long branchId) {
        return new ResourceNotFoundException(
                "Product with id: " + productId + " does not belong to branch with id: " + branchId);
    }
}
