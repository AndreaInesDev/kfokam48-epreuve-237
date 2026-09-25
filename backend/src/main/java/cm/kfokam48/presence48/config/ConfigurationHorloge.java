package cm.kfokam48.presence48.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Une horloge injectable, en UTC.
 *
 * <p>Les regles RG1 et RG17 sont des ecarts temporels. Appeler
 * {@code OffsetDateTime.now()} directement dans les services les rendrait
 * intestables autrement qu'en attendant reellement quinze minutes. En UTC parce
 * que la base stocke en UTC : un decalage de fuseau fausserait RG1.
 */
@Configuration
public class ConfigurationHorloge {

    @Bean
    public Clock horloge() {
        return Clock.systemUTC();
    }
}
