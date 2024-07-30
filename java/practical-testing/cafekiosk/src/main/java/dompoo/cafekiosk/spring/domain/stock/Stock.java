package dompoo.cafekiosk.spring.domain.stock;

import dompoo.cafekiosk.spring.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productNumber;
    
    private int quantity;
    
    @Builder
    private Stock(String productNumber, int quantity) {
        this.productNumber = productNumber;
        this.quantity = quantity;
    }
    
    public static Stock create(String productNumber, int quantity) {
        return Stock.builder()
            .productNumber(productNumber)
            .quantity(quantity)
            .build();
    }
    
    public boolean isQuantityLessThan(Long requiedStock) {
        return this.quantity < requiedStock;
    }
    
    public void deductQuantity(Long requiedStock) {
        if (isQuantityLessThan(requiedStock)) {
            throw new IllegalArgumentException("차감할 재고 수량이 없습니다.");
        }
        this.quantity -= requiedStock;
    }
}