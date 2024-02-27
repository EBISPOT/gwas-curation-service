package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.spot.gwas.deposition.config.RabbitMQConfigProperties;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.MetadataYmlUpdate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MetaDataYmlConfiguration {

    @Autowired
    RabbitMQConfigProperties rabbitMQConfigProperties;

    @Bean
    Queue metadataymlqueue(){
        return new Queue(rabbitMQConfigProperties.getSumstatsQueueName(), true);
    }

    @Bean
    DirectExchange metadataymlexchange(){
        return new DirectExchange(rabbitMQConfigProperties.getSumstatsExchangeName());
    }

    @Bean
    Binding metadataymlbinding(Queue metadataymlqueue, DirectExchange metadataymlexchange) {
        return BindingBuilder.bind(metadataymlqueue).to(metadataymlexchange).with(rabbitMQConfigProperties.getSumstatsRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
        jsonConverter.setClassMapper(classMapper());
        return jsonConverter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("metadataYmlUpdate", MetadataYmlUpdate.class);
        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }


}
