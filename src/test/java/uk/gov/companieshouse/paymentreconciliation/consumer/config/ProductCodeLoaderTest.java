package uk.gov.companieshouse.paymentreconciliation.consumer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import uk.gov.companieshouse.paymentreconciliation.consumer.exception.ProductCodesNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductCodeLoaderTest {

    @Mock
    private Resource resource;

    @InjectMocks
    private ProductCodeLoader productCodeLoader;

    @BeforeEach
    void setUp() throws Exception {
        productCodeLoader = new ProductCodeLoader();
        // Use reflection to inject the mock Resource
        Field resourceField = ProductCodeLoader.class.getDeclaredField("resource");
        resourceField.setAccessible(true);
        resourceField.set(productCodeLoader, resource);
    }

    @Test
    void testInitLoadsProductCodesSuccessfully() throws IOException {
        String yamlContent = "product_code:\n  CODE1: 1\n  CODE2: 2\n";
        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes());
        when(resource.getInputStream()).thenReturn(inputStream);

        productCodeLoader.init();
        Map<String, Integer> codes = productCodeLoader.getProductCodes();
        assertNotNull(codes);
        assertEquals(2, codes.size());
        assertEquals(1, codes.get("CODE1"));
        assertEquals(2, codes.get("CODE2"));
    }

    @Test
    void testInitThrowsExceptionOnIOException() throws IOException {
        when(resource.getInputStream()).thenThrow(new IOException("File not found"));
        Exception exception = assertThrows(ProductCodesNotFoundException.class, () -> productCodeLoader.init());
        assertTrue(exception.getMessage().contains("Failed to load product codes"));
    }
}
