package com.farmatodo.ecommerce;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabled for CI/CD builds due to full context load")
class EcommerceApplicationTests {

	@Test
	void contextLoads() {
	}

}
