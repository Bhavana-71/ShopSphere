package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.ProductRequest;
import com.shopsphere.shopsphere.entity.Category;
import com.shopsphere.shopsphere.entity.Product;
import com.shopsphere.shopsphere.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_savesAndReturnsProduct() {
        Category category = Category.builder().id(1L).name("Electronics").build();
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        Product savedProduct = Product.builder()
                .id(1L).name("iPhone").price(999.0).stockQuantity(10).unitsSold(0).category(category)
                .build();
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductRequest request = new ProductRequest();
        request.setName("iPhone");
        request.setPrice(999.0);
        request.setStockQuantity(10);
        request.setCategoryId(1L);

        var response = productService.createProduct(request);

        assertEquals("iPhone", response.getName());
        assertEquals("Electronics", response.getCategoryName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getProductById_throwsExceptionWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(99L));
    }
}