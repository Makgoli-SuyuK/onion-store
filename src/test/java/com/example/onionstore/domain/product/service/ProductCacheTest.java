package com.example.onionstore.domain.product.service;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.dto.ProductDto;
import com.example.onionstore.domain.product.dto.ProductEditRequest;
import com.example.onionstore.domain.product.dto.ProductResponse;
import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.domain.product.entity.ProductStatus;
import com.example.onionstore.domain.product.repository.*;
import com.example.onionstore.domain.product.repository.cache.ProductInfoCache;
import com.example.onionstore.domain.product.repository.cache.ProductLikeCache;
import com.example.onionstore.domain.product.repository.cache.ProductPopularCache;
import com.example.onionstore.domain.product.repository.cache.ProductStockCache;
import com.example.onionstore.global.config.CacheConfig;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(CacheConfig.class)
public class ProductCacheTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductInfoCache productInfoCache;
    @Mock
    private ProductStockCache productStockCache;
    @Mock
    private ProductLikeCache productLikeCache;
    @Mock
    private ProductPopularCache productPopularCache;
    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("모든 캐시가 존재하면 DB를 조회하지 않는다")
    void 모든_캐시가_존재하면_DB를_조회하지_않는다() {
        //given
        ProductDto dto = Mockito.mock(ProductDto.class);

        given(productInfoCache.get(anyLong())).willReturn(dto);
        given(productStockCache.get(anyLong())).willReturn(10);
        given(productLikeCache.get(anyLong())).willReturn(10L);

        //when
        ProductResponse res = productService.findById(1L);

        //then
        verify(productRepository, never()).findById(anyLong());

        verify(productInfoCache, never()).put(anyLong(), any());
        verify(productLikeCache, never()).put(anyLong(), any());
        verify(productStockCache, never()).put(anyLong(), any());

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("상품정보 캐시가 없으면 DB에서 조회하고 상품정보 캐시만 저장한다")
    void 상품정보_캐시가_없다면_DB에서_조회해_상품정보_캐시만_저장한다() {
        //given
        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productInfoCache.get(anyLong())).willReturn(null);
        given(productStockCache.get(anyLong())).willReturn(10);
        given(productLikeCache.get(anyLong())).willReturn(10L);

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        //when
        ProductResponse res = productService.findById(1L);

        //then
        verify(productRepository, times(1)).findById(anyLong());

        verify(productInfoCache, times(1)).put(anyLong(), any());
        verify(productLikeCache, never()).put(anyLong(), any());
        verify(productStockCache, never()).put(anyLong(), any());

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("상품 재고 캐시가 없다면 DB에서 조회하고 상품 재고 캐시만 저장한다")
    void 상품재고_캐시가_없다면_DB에서_조회하고_상품재고_캐시만_저장한다() {
        //given
        ProductDto dto = Mockito.mock(ProductDto.class);

        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productInfoCache.get(anyLong())).willReturn(dto);
        given(productStockCache.get(anyLong())).willReturn(null);
        given(productLikeCache.get(anyLong())).willReturn(10L);

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        //when
        ProductResponse res = productService.findById(1L);

        //then
        verify(productRepository, times(1)).findById(anyLong());

        verify(productInfoCache, never()).put(anyLong(), any());
        verify(productLikeCache, never()).put(anyLong(), any());
        verify(productStockCache, times(1)).put(anyLong(), any());

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("상품 좋아요 캐시가 없다면 DB에서 조회하고 상품 좋아요 캐시만 저장한다")
    void 상품_좋아요_캐시가_없다면_DB에서_조회하고_상품_좋아요_캐시만_저장한다() {
        //given
        ProductDto dto = Mockito.mock(ProductDto.class);

        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productInfoCache.get(anyLong())).willReturn(dto);
        given(productStockCache.get(anyLong())).willReturn(10);
        given(productLikeCache.get(anyLong())).willReturn(null);

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        //when
        ProductResponse res = productService.findById(1L);

        //then
        verify(productRepository, times(1)).findById(anyLong());

        verify(productInfoCache, never()).put(anyLong(), any());
        verify(productLikeCache, times(1)).put(anyLong(), any());
        verify(productStockCache, never()).put(anyLong(), any());

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 캐시에 저장하지 않는다.")
    void 상품이_존재하지_않으면_캐시에_저장하지_않는다() {
        //given
        given(productInfoCache.get(anyLong())).willReturn(null);
        given(productStockCache.get(anyLong())).willReturn(null);
        given(productLikeCache.get(anyLong())).willReturn(null);

        given(productRepository.findById(anyLong())).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() -> productService.findById(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PRODUCT_NOT_FOUND.getMessage());

        verify(productInfoCache, never()).put(anyLong(), any());
        verify(productLikeCache, never()).put(anyLong(), any());
        verify(productStockCache, never()).put(anyLong(), any());
    }

    @Test
    @DisplayName("재고 감소 시 캐시를 무효화한다")
    void 재고_감소시_캐시를_무효화한다() {
        //given
        given(productRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(mock(Product.class)));

        //when
        productService.decreaseStock(1L, 2);

        //then
        verify(productStockCache).evict(anyLong());
        verify(productInfoCache).evict(anyLong());
    }

    @Test
    @DisplayName("재고 복원 시 캐시를 무효화한다")
    void 재고_복원시_캐시를_무효화한다() {
        //given
        given(productRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(mock(Product.class)));

        //when
        productService.restoreStock(1L, 2);

        //then
        verify(productStockCache).evict(anyLong());
        verify(productInfoCache).evict(anyLong());
    }

    @Test
    @DisplayName("좋아요 증가 시 캐시를 무효화한다")
    void 좋아요_증가_시_캐시_무효화() {
        //given
        Product product = mock(Product.class);

        //when
        productService.increaseLikeCount(product);

        //then
        verify(productLikeCache).evict(anyLong());
        verify(productPopularCache).evict();
    }

    @Test
    @DisplayName("좋아요 감소 시 캐시를 무효화한다")
    void 좋아요_감소_시_캐시_무효화() {
        //given
        Product product = mock(Product.class);

        //when
        productService.decreaseLikeCount(product);

        //then
        verify(productLikeCache).evict(anyLong());
        verify(productPopularCache).evict();
    }

    @Test
    @DisplayName("상품 수정 시 상품정보와 재고 캐시를 무효화한다")
    void 상품_수정_시_상품정보와_재고_캐시_무효화() {
        //given
        ProductEditRequest productEditRequest = new ProductEditRequest(
                "newName",
                "newDescription",
                100L,
                30,
                ProductStatus.SELLING.name()
        );

        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(product));
        given(productRepository.save(any())).willReturn(product);

        //when
        productService.editProduct(1L,  productEditRequest);

        //then
        verify(productInfoCache).evict(anyLong());
        verify(productStockCache).evict(anyLong());
    }

    @Test
    @DisplayName("상품 삭제 시 캐시를 모두 무효화한다")
    void 상품_삭제_시_캐시_모두_무효화() {
        //given
        Product product = Product.create(
                new Category("category"),
                "name",
                "description",
                10000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        given(productRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(product));

        //when
        productService.deleteProduct(1L);

        //then
        verify(productInfoCache).evict(anyLong());
        verify(productStockCache).evict(anyLong());
        verify(productLikeCache).evict(anyLong());
        verify(productPopularCache).evict();
    }

    @Test
    @DisplayName("상품 인기 조회 시 캐시에 없다면 캐시 데이터를 반환한다")
    void 인기_상품_조회_시_캐시에_없으면_캐시_데이터_반환() {
        //given
        Category category = new Category("category");
        Product product = Product.create(
                category,
                "name1",
                "description",
                10000L,
                10
        );
        ReflectionTestUtils.setField(product, "id", 1L);

        List<Product> list = List.of(
                product
        );

        given(productPopularCache.get()).willReturn(null);
        given(productRepository.find10OrderByLikeCountDesc(any(Pageable.class)))
                .willReturn(list);

        //when
        List<ProductSimpleResponse> res = productService.find10OrderByLikeCountDesc();

        //then
        verify(productRepository, times(1)).find10OrderByLikeCountDesc(any(Pageable.class));
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("상품 인기 조회 시 캐시에 있다면 캐시 데이터를 반환한다")
    void 인기_상품_조회_시_캐시에_있으면_캐시_데이터_반환() {
        //given
        List<ProductSimpleResponse> list = List.of(
                mock(ProductSimpleResponse.class),
                mock(ProductSimpleResponse.class)
        );

        given(productPopularCache.get()).willReturn(list);

        //when
        List<ProductSimpleResponse> res = productService.find10OrderByLikeCountDesc();

        //then
        verify(productRepository, never()).find10OrderByLikeCountDesc(any(Pageable.class));
        assertThat(res).isNotNull();
    }
}
