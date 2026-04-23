/*
 * package com.swadhyaydata.app;
 * 
 * import org.springframework.context.annotation.Configuration; import
 * org.springframework.web.servlet.config.annotation.CorsRegistry; import
 * org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 * 
 * @Configuration public class WebConfig implements WebMvcConfigurer {
 * 
 * @Override public void addCorsMappings(CorsRegistry registry) {
 * registry.addMapping("/**") // Allow all paths
 * .allowedOrigins("http://123.124.166.102") // Allow all origins
 * .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") //
 * Allowed HTTP methods .allowedHeaders("*") // Allow all headers
 * .allowCredentials(true) // Allow credentials .maxAge(3600); // Cache
 * pre-flight requests for 1 hour } }
 */