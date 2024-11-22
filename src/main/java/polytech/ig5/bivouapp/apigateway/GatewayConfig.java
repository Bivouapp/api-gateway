package polytech.ig5.bivouapp.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GatewayConfig {

    private final RestTemplate restTemplate;

    @Value("${microservices.reservation}")
    private String reservationServiceUrl;

    public GatewayConfig(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/reservations/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> forwardToReservations(@RequestHeader Map<String, String> headers, @RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request) {
        return forwardToMicroservice(reservationServiceUrl, headers, body, method, request);
    }

    @RequestMapping(value = "/disponibilities/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> forwardToDisponibilities(@RequestHeader Map<String, String> headers, @RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request) {
        return forwardToMicroservice(reservationServiceUrl, headers, body, method, request);
    }

    @RequestMapping(value = "/reviews/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> forwardToReviews(@RequestHeader Map<String, String> headers, @RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request) {
        return forwardToMicroservice(reservationServiceUrl, headers, body, method, request);
    }

    private ResponseEntity<Object> forwardToMicroservice(String baseUrl, Map<String, String> headers, Object body, HttpMethod method, HttpServletRequest request) {
        // Extract the remaining path from the request URI
        String requestUri = request.getRequestURI(); // Full URI after the base path

        // Remove the slash at the end of the requestUri if it exists
        if (requestUri.endsWith("/")) {
            requestUri = requestUri.substring(0, requestUri.length() - 1);
        }
        
        // Construct the full URL to the microservice
        String fullUrl = baseUrl + requestUri;

        System.out.println("Full URL: " + fullUrl);

        // Configure the request headers
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);

        HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);

        try {
            // Call the microservice
            ResponseEntity<byte[]> response = restTemplate.exchange(fullUrl, method, entity, byte[].class);
    
            // Log response details
            System.out.println("Response Headers: " + response.getHeaders());
            System.out.println("Response Status Code: " + response.getStatusCode());
    
            // Set Content-Encoding header to gzip (if applicable)
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(response.getHeaders());
            responseHeaders.set("Content-Encoding", "gzip");
    
            // Return response with updated headers
            return ResponseEntity.status(response.getStatusCode())
                .headers(responseHeaders)
                .body(response.getBody());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error forwarding request: " + e.getMessage());
        }
    }
}