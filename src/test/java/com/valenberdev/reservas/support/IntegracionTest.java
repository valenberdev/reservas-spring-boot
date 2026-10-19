package com.valenberdev.reservas.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.util.TimeZone;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegracionTest {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}