package dompoo.cafekiosk.spring.api.service.product;

import dompoo.cafekiosk.spring.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductNumberFactory {
	
	private final ProductRepository productRepository;
	
	public String createNextProductNumber() {
		String latestProductNumber = productRepository.findLatestProductNumber();
		if (latestProductNumber == null) {
			return "001";
		} else {
			int latestProductNumberInt = Integer.parseInt(latestProductNumber);
			int nextProductNumber = latestProductNumberInt + 1;
			
			return String.format("%03d", nextProductNumber);
		}
	}
}
