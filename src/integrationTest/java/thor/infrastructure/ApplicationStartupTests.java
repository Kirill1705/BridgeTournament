package thor.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import thor.bridge_tournament.BridgeTournamentApplication;
import thor.bridge_tournament.presentation.telegram.TelegramBotMainClass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = BridgeTournamentApplication.class, properties = {
        "telegrambots.enabled=false",
        "bot.token=test-token"
})
@ActiveProfiles("test")
class ApplicationStartupTests {
    @Autowired
    private WebApplicationContext context;

    // Prevent the constructor from changing commands of a real Telegram bot.
    @MockitoBean
    private TelegramBotMainClass telegramBot;

    @Test
    void applicationStartsWithDatabaseAndServesOpenApi() throws Exception {
        webAppContextSetup(context).build()
                .perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }
}
