package com.rentacar.cucumber;

import com.rentacar.RentalCarApp;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = RentalCarApp.class)
@WebAppConfiguration
public class CucumberTestContextConfiguration {}
