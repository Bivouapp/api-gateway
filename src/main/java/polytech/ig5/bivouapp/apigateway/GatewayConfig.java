package polytech.ig5.bivouapp.apigateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1")
public class GatewayConfig {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/reservations")
    public Object getReservations() {
        String url = "http://ms-reservation.cluster-ig5.igpolytech.fr:8080/api/v1/reservations";
        return restTemplate.getForObject(url, Object.class);
    }
}
