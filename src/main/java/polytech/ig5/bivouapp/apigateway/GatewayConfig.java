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

    // Endpoints for forwarding requests to microservices
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

    // Main method to handle forwarding logic
    private ResponseEntity<Object> forwardToMicroservice(String baseUrl, Map<String, String> headers, Object body, HttpMethod method, HttpServletRequest request) {
        String requestUri = extractRequestUri(request);
        String fullUrl = baseUrl + requestUri;

        HttpHeaders httpHeaders = prepareHeaders(headers, method);
        String jsonBody = prepareRequestBody(body);

        if (jsonBody != null) {
            httpHeaders.setContentLength(jsonBody.getBytes(StandardCharsets.UTF_8).length);
        }

        HttpEntity<Object> entity = new HttpEntity<>(jsonBody, httpHeaders);

        logRequestDetails(fullUrl, httpHeaders, jsonBody);

        return executeMicroserviceRequest(fullUrl, method, entity);
    }

    // Extract the remaining path from the request URI
    private String extractRequestUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri.endsWith("/")) {
            requestUri = requestUri.substring(0, requestUri.length() - 1);
        }
        return requestUri;
    }

    // Prepare HTTP headers for the forwarded request
    private HttpHeaders prepareHeaders(Map<String, String> headers, HttpMethod method) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        httpHeaders.remove("Content-Encoding");
        return httpHeaders;
    }

    // Serialize the body to JSON
    private String prepareRequestBody(Object body) {
        if (body == null) return null;
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            System.err.println("Error serializing request body: " + e.getMessage());
            throw new RuntimeException("Error serializing request body: " + e.getMessage());
        }
    }

    // Log request details for debugging
    private void logRequestDetails(String fullUrl, HttpHeaders httpHeaders, String jsonBody) {
        System.out.println("Sending request to: " + fullUrl);
        System.out.println("Request Headers: " + httpHeaders);
        System.out.println("Request Body: " + jsonBody);
    }

    // Execute the request to the microservice and handle the response
    private ResponseEntity<Object> executeMicroserviceRequest(String fullUrl, HttpMethod method, HttpEntity<Object> entity) {
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(fullUrl, method, entity, byte[].class);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(response.getHeaders());

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
