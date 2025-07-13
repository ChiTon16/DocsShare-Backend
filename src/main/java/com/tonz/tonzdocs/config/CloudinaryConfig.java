package com.tonz.tonzdocs.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "duwdx2tgu",
                "api_key", "646743949231237",
                "api_secret", "jbac0w3FuckWA57tHsMH45ljksA"
        ));
    }
}
