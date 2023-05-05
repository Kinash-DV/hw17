package dv.kinash.hw15.cats;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@SpringBootTest()
class CatServiceTest {
    @DynamicPropertySource
    static void setCatFactProperty(DynamicPropertyRegistry registry) {
        String port = "8099";
        registry.add("wiremock.server.port", () -> port);
        registry.add("catfact.url", () -> "http://localhost:"+port+"/fact");
    }

    @Value("${wiremock.server.port}")
    private int wiremockPort;

    private WireMockServer wireMockServer;
    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(wiremockPort);
        wireMockServer.start();
        WireMock.configureFor("localhost", wiremockPort);
    }

    @Autowired
    CatService service;

    @Test
    void getCatFact() throws IOException {
        String fact = "{\"fact\":\"On average, a cat will sleep for 16 hours a day.\",\"length\":48}";
        WireMock.stubFor(WireMock.get(urlEqualTo("/fact")).willReturn(WireMock.aResponse().withBody(fact)));

        final String catFact = service.getCatFact();
        Assertions.assertNotNull(catFact);
        System.out.println(catFact);
    }
}