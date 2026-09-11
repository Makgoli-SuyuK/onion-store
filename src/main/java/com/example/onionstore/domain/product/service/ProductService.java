package com.example.onionstore.domain.product.service;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.category.service.CategoryService;
import com.example.onionstore.domain.product.dto.ProductCreateRequest;
import com.example.onionstore.domain.product.dto.ProductEditRequest;
import com.example.onionstore.domain.product.dto.ProductResponse;
import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductStatus;
import com.example.onionstore.domain.product.repository.ProductRepository;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    public void createProduct(ProductCreateRequest createRequest, Category category) {
        if (createRequest.price() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }

        if (createRequest.stock() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK);
        }

        Product newProduct = Product.create(
                category,
                createRequest.productName(),
                createRequest.description(),
                createRequest.price(),
                createRequest.stock()
        );

        productRepository.save(newProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductSimpleResponse> searchWithConditions(ProductSearchConditions conditions, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        return productRepository.searchWithConditions(conditions, pageable);
    }

    /** 테스트용 가짜 메서드 **/
    public boolean existsByCategoryId(Long categoryId) {
        return false;
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = findProductForUpdate(productId);
        product.decreaseStock(quantity);
    }

    @Transactional
    public void restoreStock(Long productId, int quantity) {
        Product product = findProductForUpdate(productId);
        product.restoreStock(quantity);
    }

    @Transactional
    public ProductResponse editProduct(Long productId, ProductEditRequest editRequest) {
        Product product = findProductForUpdate(productId);

        product.changeName(editRequest.name());
        product.changeDescription(editRequest.description());
        product.changePrice(editRequest.price());
        product.changeStock(editRequest.stock());

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Product getProductForCart(Long productId,int quantity) { // 존재하고 판매 중인 상품만 장바구니 도메인에 전달한다.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleted() || product.getStatus() != ProductStatus.SELLING) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_SELLING);
        }
        if (product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
        return product;
    }

    private Product findProductForUpdate(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
