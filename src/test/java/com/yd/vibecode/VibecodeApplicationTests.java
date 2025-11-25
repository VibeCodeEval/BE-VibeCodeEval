package com.yd.vibecode;

import com.yd.vibecode.config.TestConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Disabled("Not a real test - just checks if context loads")
class VibecodeApplicationTests {

	@Test
	void contextLoads() {
	}

}
