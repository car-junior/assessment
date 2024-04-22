package com.senior.assessment.domain.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.Map;
import java.util.stream.Stream;

@ContextConfiguration(initializers = PostgreSQLContainerConfig.Initializer.class)
public abstract class PostgreSQLContainerConfig {
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        // Criando um Container em Runtime para a imagem do PostgreSQL
        static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine");

        private static void startContainers() {
            Startables.deepStart(Stream.of(postgresContainer)).join();
        }

        // Definindo configurações de conexão para pegar as configurações do testContainer
        private static Map<String, Object> createConnectionConfigurations() {
            return Map.of(
                    "spring.datasource.url", postgresContainer.getJdbcUrl(),
                    "spring.datasource.username", postgresContainer.getUsername(),
                    "spring.datasource.password", postgresContainer.getPassword()
            );
        }

        // Aqui é chamado a inicialização dos containers
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            startContainers();
            //Obtendo o contexto do spring
            var configurableEnvironment = applicationContext.getEnvironment();
            //Adiciona uma nova propriedade de ambiente
            var testContainers = new MapPropertySource("testcontainers", createConnectionConfigurations());
            configurableEnvironment.getPropertySources().addFirst(testContainers);
        }
    }
}