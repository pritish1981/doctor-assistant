package com.superclinic.doctorassistant.ai.config;

import com.superclinic.doctorassistant.ai.tools.AvailabilityTool;
import com.superclinic.doctorassistant.ai.tools.BookingTool;
import com.superclinic.doctorassistant.ai.tools.DoctorSearchTool;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    @Bean
    @Primary
    ClientHttpRequestFactory openAiClientHttpRequestFactory(OpenAiProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(properties.getConnectTimeout())
                .withReadTimeout(properties.getReadTimeout());
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }

    @Bean
    @Primary
    RestClient.Builder openAiRestClientBuilder(
            ClientHttpRequestFactory requestFactory,
            OpenAiResponseErrorHandler errorHandler,
            OpenAiHttpLoggingInterceptor loggingInterceptor,
            @Value("${spring.ai.openai.api-key}") String apiKey) {
        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestInterceptor(loggingInterceptor)
                .defaultStatusHandler(errorHandler);
    }

    @Bean
    @Primary
    WebClient.Builder openAiWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Primary
    OpenAiApi openAiApi(
            RestClient.Builder restClientBuilder,
            WebClient.Builder webClientBuilder,
            OpenAiResponseErrorHandler errorHandler,
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url:https://api.openai.com}") String baseUrl) {
        log.info("Configuring OpenAiApi: baseUrl={}", baseUrl);
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .responseErrorHandler(errorHandler)
                .build();
    }

    @Bean
    @Primary
    OpenAiChatModel openAiChatModel(
            OpenAiApi openAiApi,
            ObservationRegistry observationRegistry,
            @Value("${spring.ai.openai.chat.options.model:gpt-4.1-mini}") String model,
            @Value("${spring.ai.openai.chat.options.temperature:0.3}") Double temperature) {
        log.info("Configuring OpenAiChatModel: model={}, temperature={}", model, temperature);
        OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(defaultOptions)
                .observationRegistry(observationRegistry)
                .build();
    }

    @Bean
    @Primary
    ChatClient chatClient(
            OpenAiChatModel chatModel,
            OpenAiProperties properties,
            DoctorSearchTool doctorSearchTool,
            AvailabilityTool availabilityTool,
            BookingTool bookingTool) {
        log.info("Configuring ChatClient with default system prompt and AI tools");
        return ChatClient.builder(chatModel)
                .defaultSystem(properties.getDefaultSystemPrompt())
                .defaultToolCallbacks(AgentToolCallbackFactory.create(
                        doctorSearchTool, availabilityTool, bookingTool))
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    ChatClient.Builder chatClientBuilder(
            OpenAiChatModel chatModel,
            OpenAiProperties properties,
            DoctorSearchTool doctorSearchTool,
            AvailabilityTool availabilityTool,
            BookingTool bookingTool) {
        return ChatClient.builder(chatModel)
                .defaultSystem(properties.getDefaultSystemPrompt())
                .defaultToolCallbacks(AgentToolCallbackFactory.create(
                        doctorSearchTool, availabilityTool, bookingTool))
                .defaultAdvisors(new SimpleLoggerAdvisor());
    }
}
