package com.example.demo.integrationtest;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerIT
{
    @LocalServerPort
    private int port;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    @BeforeAll
    public static void init()
    {
        restTemplate=new RestTemplate();
    }

    public void setUp()
    {
        baseUrl=baseUrl.concat(":").concat(port+"").concat("/book");
    }
}
