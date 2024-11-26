package polytech.ig5.bivouapp.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class GatewayConfig {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${microservices.reservation}")
    private String reservationServiceUrl;

    @Value("${microservices.bivouac}")
    private String bivouacServiceUrl;

    @Value("${microservices.user}")
    private String userServiceUrl;

    @Value("${microservices.address}")
    private String addressServiceUrl;

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

    @RequestMapping(value = "/bivouacs/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> forwardToBivouacs(@RequestHeader Map<String, String> headers, @RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request) {
        return forwardToMicroservice(bivouacServiceUrl, headers, body, method, request);
    }

    @RequestMapping(value = "/equipments/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> forwardToEquipments(@RequestHeader Map<String, String> headers, @RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request) {
        return forwardToMicroservice(bivouacServiceUrl, headers, body, method, request);
    }
    
    @RequestMapping(value = "/users/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> forwardToUsers(@RequestHeader Map<String, String> headers, @RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request) {
        return forwardToMicroservice(userServiceUrl, headers, body, method, request);
    }

    @RequestMapping(value = "/addresses/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> forwardToAddresses(@RequestHeader Map<String, String> headers, @RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request) {
        return forwardToMicroservice(addressServiceUrl, headers, body, method, request);
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

        // Configure the request headers
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);

        // Ensure Content-Type is set to JSON for POST/PUT requests
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        // Remove Content-Encoding header if present
        httpHeaders.remove("Content-Encoding");

        String jsonBody = null;
        try {
            if (body != null) {
                // Serialize the body to JSON
                jsonBody = objectMapper.writeValueAsString(body);
                System.out.println("Serialized JSON Body: " + jsonBody);
            }
        } catch (Exception e) {
            System.err.println("Error serializing request body: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error serializing request body: " + e.getMessage());
        }

        if (jsonBody != null) {
            httpHeaders.setContentLength(jsonBody.getBytes(StandardCharsets.UTF_8).length);
        }

        HttpEntity<Object> entity = new HttpEntity<>(jsonBody, httpHeaders);

        System.out.println("Sending request to: " + fullUrl);
        System.out.println("Request Headers: " + httpHeaders);
        System.out.println("Request Body: " + jsonBody);
        System.out.println("Entity: " + entity);

        try {
            // Call the microservice
            ResponseEntity<byte[]> response = restTemplate.exchange(fullUrl, method, entity, byte[].class);
    
            // Log response details
            System.out.println("Response Headers: " + response.getHeaders());
            System.out.println("Response Status Code: " + response.getStatusCode());
    
            // Set Content-Encoding header to gzip (if applicable)
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(response.getHeaders());
    
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