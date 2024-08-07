package dompoo.cafekiosk.spring.api.service.product;

import dompoo.cafekiosk.spring.IntegrationTestSupport;
import dompoo.cafekiosk.spring.api.service.product.dto.request.ProductCreateServiceRequest;
import dompoo.cafekiosk.spring.api.service.product.dto.response.ProductResponse;
import dompoo.cafekiosk.spring.domain.product.Product;
import dompoo.cafekiosk.spring.domain.product.ProductRepository;
import dompoo.cafekiosk.spring.domain.product.ProductSellingStatus;
import dompoo.cafekiosk.spring.domain.product.ProductType;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static dompoo.cafekiosk.spring.domain.product.ProductSellingStatus.*;
import static dompoo.cafekiosk.spring.domain.product.ProductType.BAKERY;
import static dompoo.cafekiosk.spring.domain.product.ProductType.HANDMADE;
import static org.assertj.core.api.Assertions.assertThat;

class ProductServiceTest extends IntegrationTestSupport {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;
    
    @AfterEach
    void tearDown() {
        productRepository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("현재 판매중이거나 판매 보류된 상품을 조회할 수 있어야 한다.")
    void getSellingProducts() {
        //given
        Product product1 = createProduct("001", HANDMADE, SELLING, 4000, "아메리카노");
        Product product2 = createProduct("002", HANDMADE, HOLD, 5000, "카페라떼");
        Product product3 = createProduct("003", BAKERY, STOP_SELLING, 5000, "크루아상");
        productRepository.saveAll(List.of(product1, product2, product3));
        
        //when
        List<ProductResponse> response = productService.getSellingProducts();
        
        //then
        assertThat(response).hasSize(2)
                .extracting("productNumber", "name", "type", "sellingStatus", "price")
            .containsExactlyInAnyOrder(
                Tuple.tuple("001", "아메리카노", HANDMADE, SELLING, 4000),
                Tuple.tuple("002", "카페라떼", HANDMADE, HOLD, 5000)
            );
    }
    
    @Test
    @DisplayName("신규 상품을 등록할 수 있어야 한다.")
    void createProduct() {
        //given
        Product product = createProduct("001", HANDMADE, SELLING, 4000, "아메리카노");
        productRepository.save(product);
        ProductCreateServiceRequest request = ProductCreateServiceRequest.builder()
                .name("밤양갱")
                .price(3000)
                .type(HANDMADE)
                .sellingStatus(SELLING)
                .build();
        
        //when
        ProductResponse response = productService.createProduct(request);
        
        //then
        assertThat(response)
                .extracting("productNumber", "name", "type", "sellingStatus", "price")
                .contains("002", "밤양갱", HANDMADE, SELLING, 3000);
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(2)
                .extracting("productNumber", "name", "type", "sellingStatus", "price")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("001", "아메리카노", HANDMADE, SELLING, 4000),
                        Tuple.tuple("002", "밤양갱", HANDMADE, SELLING, 3000)
                );
    }
    
    private Product createProduct(String productNumber, ProductType type, ProductSellingStatus sellingStatus, int price, String name) {
        return Product.builder()
                .productNumber(productNumber)
                .name(name)
                .price(price)
                .sellingStatus(sellingStatus)
                .type(type)
                .build();
    }
}