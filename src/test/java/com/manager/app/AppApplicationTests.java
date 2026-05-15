package com.manager.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "JWT_SECRET_KEY=test-secret-key-12345678901234567890",
    "spring.data.mongodb.uri=mongodb://localhost:27017/test",
    "MS_AUTH_SERVICE=http://localhost:8001"
})
class AppApplicationTests {

    @Test
    void contextLoads() {
    }

}
