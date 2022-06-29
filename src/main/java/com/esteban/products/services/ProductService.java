package com.esteban.products.services;

import com.esteban.products.entities.Product;
import com.esteban.products.model.ProductDto;
import com.esteban.products.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductDto create(ProductDto productDto) {
        Product product = productRepository.save(toEntity(productDto));
        return toDto(product);
    }

    public void delete(Long id) {
        try {
            productRepository.deleteById(id);
        }catch(EmptyResultDataAccessException ex) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to find product with id " + id, ex);
        }
    }

    public ProductDto update(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow( () -> new ResponseStatusException(NOT_FOUND, "Unable to find product with id " + id));
        product.setStock(productDto.getStock());
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product = productRepository.save(product);
        return toDto(product);
    }

    private Product toEntity(ProductDto productDto) {
        return Product.builder()
                .name(productDto.getName())
                .price(productDto.getPrice())
                .stock(productDto.getStock())
                .build();
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }

}
