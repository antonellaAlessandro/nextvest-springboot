package nextvest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Habilita las tareas programadas (@Scheduled), como la actualización de precios.
@Configuration
@EnableScheduling
public class SchedulingConfig {
}